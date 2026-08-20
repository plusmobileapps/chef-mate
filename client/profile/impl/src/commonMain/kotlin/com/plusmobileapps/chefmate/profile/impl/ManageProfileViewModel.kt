package com.plusmobileapps.chefmate.profile.impl

import chefmate.client.profile.public.generated.resources.Res
import chefmate.client.profile.public.generated.resources.manage_profile_delete_error
import chefmate.client.profile.public.generated.resources.manage_profile_handle_taken_error
import chefmate.client.profile.public.generated.resources.manage_profile_save_error
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.ProfilePhotoStorage
import com.plusmobileapps.chefmate.auth.usecase.DeleteAccountUseCase
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.profile.ManageProfileBloc.HandleStatus
import com.plusmobileapps.chefmate.profile.ManageProfileBloc.Model
import com.plusmobileapps.chefmate.profile.data.ProfileHandle
import com.plusmobileapps.chefmate.profile.data.ProfileRepository
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.util.PickedImage
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class ManageProfileViewModel(
    @Main mainContext: CoroutineContext,
    private val authenticationRepository: AuthenticationRepository,
    private val profilePhotoStorage: ProfilePhotoStorage,
    private val profileRepository: ProfileRepository,
    private val deleteAccountUseCase: DeleteAccountUseCase,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<Model> = _state.asStateFlow()

    /** Emitted when the screen should pop: after a successful save or account deletion. */
    private val _outputs = Channel<Output>(Channel.BUFFERED)
    val outputs: Flow<Output> = _outputs.receiveAsFlow()

    private val pickedImage = MutableStateFlow<PickedImage?>(null)

    /** In-flight availability check, cancelled on each keystroke to debounce. */
    private var handleCheckJob: Job? = null

    init {
        loadProfile()
    }

    private fun initialState(): Model {
        val user = (authenticationRepository.state.value as? AuthState.Authenticated)?.user
        return Model(
            displayName = user?.userName.orEmpty(),
            email = user?.userEmail.orEmpty(),
            photoUrl = user?.userProfileImageUrl,
        )
    }

    /**
     * Pulls the public profile, if the user has one. A failure is deliberately silent: the account
     * fields are already populated from the auth session, so the screen stays usable and the user
     * simply appears not to have claimed a handle yet. Claiming is idempotent-ish anyway — the
     * server's UNIQUE constraint rejects a second claim.
     */
    private fun loadProfile() {
        scope.launch {
            val profile = profileRepository.getMyProfile().getOrNull() ?: return@launch
            _state.update {
                it.copy(
                    handle = profile.handle,
                    isHandleClaimed = true,
                    bio = profile.bio,
                    // Prefer the account's display name/avatar, which the user may have just
                    // edited; fall back to the profile's copy.
                    displayName = it.displayName.ifBlank { profile.displayName },
                    photoUrl = it.photoUrl ?: profile.avatarUrl,
                )
            }
        }
    }

    fun setDisplayName(displayName: String) {
        _state.update { it.copy(displayName = displayName, saveError = null) }
    }

    fun setHandle(handle: String) {
        // Permanent once claimed, so ignore edits rather than letting the UI drift out of sync with
        // what the server will accept.
        if (_state.value.isHandleClaimed) return
        val normalized = ProfileHandle.normalize(handle)
        _state.update { it.copy(handle = normalized, saveError = null) }
        checkHandleAvailability(normalized)
    }

    private fun checkHandleAvailability(handle: String) {
        handleCheckJob?.cancel()
        if (handle.isBlank()) {
            _state.update { it.copy(handleStatus = null) }
            return
        }
        if (!ProfileHandle.isValidFormat(handle)) {
            // Local rules catch this without a round-trip; they mirror the server's CHECK.
            _state.update { it.copy(handleStatus = HandleStatus.InvalidFormat) }
            return
        }
        _state.update { it.copy(handleStatus = HandleStatus.Checking) }
        handleCheckJob = scope.launch {
            delay(HANDLE_CHECK_DEBOUNCE_MS)
            val available = profileRepository.isHandleAvailable(handle).getOrNull()
            _state.update { current ->
                // A slower earlier check must not stomp a newer keystroke's result.
                if (current.handle != handle) return@update current
                current.copy(
                    handleStatus =
                        when (available) {
                            true -> HandleStatus.Available
                            false -> HandleStatus.Taken
                            // The check is advisory; on failure let the user try to save and
                            // let the UNIQUE constraint be the judge.
                            null -> null
                        }
                )
            }
        }
    }

    fun setBio(bio: String) {
        _state.update { it.copy(bio = bio.take(MAX_BIO_LENGTH), saveError = null) }
    }

    fun setPhoto(image: PickedImage) {
        pickedImage.value = image
        _state.update { it.copy(pickedPhoto = image.bytes, saveError = null) }
    }

    fun save() {
        val current = _state.value
        if (!current.canSave) return
        _state.update { it.copy(isSaving = true, saveError = null) }
        // Capture the avatar that's currently persisted so we can clean it up once a new one is
        // uploaded and saved. Uploads use a fresh randomized path, so without this the old object
        // is orphaned in Supabase storage.
        val previousPhotoUrl = current.photoUrl
        scope.launch {
            val avatarUrl =
                pickedImage.value?.let { picked ->
                    runCatching {
                        profilePhotoStorage.uploadPhoto(picked.bytes, picked.fileExtension)
                    }
                        .getOrElse {
                            failSave()
                            return@launch
                        }
                }
            authenticationRepository
                .updateProfile(displayName = current.displayName.trim(), avatarUrl = avatarUrl)
                .onSuccess {
                    // The public profile mirrors the account, so it's written second and only if
                    // the account write succeeded. A failure here leaves the account updated and
                    // surfaces an error — the user can retry without losing their edit.
                    if (!savePublicProfile(current, avatarUrl)) return@launch
                    // Only after the profile points at the new avatar do we delete the old one,
                    // best-effort, so a delete failure can't strand the user without an avatar.
                    if (
                        avatarUrl != null &&
                            !previousPhotoUrl.isNullOrBlank() &&
                            previousPhotoUrl != avatarUrl
                    ) {
                        profilePhotoStorage.deletePhoto(previousPhotoUrl)
                    }
                    _outputs.send(Output.Saved)
                }
                .onFailure { failSave() }
        }
    }

    /**
     * Writes the public `profiles` row, claiming the handle on first save. Returns false when the
     * save should be treated as failed (state already updated with the error).
     *
     * Saving with no handle is a no-op rather than an error: a public profile is opt-in, so a user
     * who only wanted to change their display name never gets one.
     */
    private suspend fun savePublicProfile(current: Model, avatarUrl: String?): Boolean {
        val result =
            when {
                current.isHandleClaimed ->
                    profileRepository.updateProfile(
                        displayName = current.displayName.trim(),
                        bio = current.bio,
                        avatarUrl = avatarUrl,
                    )
                current.handle.isBlank() -> return true
                else ->
                    profileRepository.claimHandle(
                        handle = current.handle,
                        displayName = current.displayName.trim(),
                        bio = current.bio,
                        avatarUrl = avatarUrl,
                    )
            }
        return result.fold(
            onSuccess = { profile ->
                _state.update { it.copy(handle = profile.handle, isHandleClaimed = true) }
                true
            },
            onFailure = { error ->
                // Losing the race for a handle is the one failure with a specific, actionable
                // message — everything else is the generic save error.
                val isHandleConflict =
                    error is ProfileRepository.HandleTaken ||
                        error is ProfileRepository.HandleRejected
                _state.update {
                    it.copy(
                        isSaving = false,
                        handleStatus =
                            if (isHandleConflict) HandleStatus.Taken else it.handleStatus,
                        saveError =
                            if (isHandleConflict) {
                                Res.string.manage_profile_handle_taken_error.asTextData()
                            } else {
                                Res.string.manage_profile_save_error.asTextData()
                            },
                    )
                }
                false
            },
        )
    }

    private fun failSave() {
        _state.update {
            it.copy(isSaving = false, saveError = Res.string.manage_profile_save_error.asTextData())
        }
    }

    fun showDeleteDialog() {
        _state.update {
            it.copy(showDeleteDialog = true, deleteConfirmation = "", deleteError = null)
        }
    }

    fun setDeleteConfirmation(confirmation: String) {
        _state.update { it.copy(deleteConfirmation = confirmation) }
    }

    fun dismissDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false, deleteConfirmation = "") }
    }

    fun deleteAccount() {
        // Guard against deletion unless the typed email matches — mirrors the disabled confirm
        // button so a stray call (e.g. an IME action) can't bypass the confirmation gate.
        if (!_state.value.canConfirmDelete) return
        _state.update {
            it.copy(
                showDeleteDialog = false,
                deleteConfirmation = "",
                isDeleting = true,
                deleteError = null,
            )
        }
        scope.launch {
            deleteAccountUseCase()
                .onSuccess { _outputs.send(Output.Deleted) }
                .onFailure {
                    _state.update {
                        it.copy(
                            isDeleting = false,
                            deleteError = Res.string.manage_profile_delete_error.asTextData(),
                        )
                    }
                }
        }
    }

    sealed interface Output {
        data object Saved : Output

        data object Deleted : Output
    }

    private companion object {
        /** Matches the `profiles_bio_length` CHECK constraint. */
        const val MAX_BIO_LENGTH = 500

        const val HANDLE_CHECK_DEBOUNCE_MS = 400L
    }
}
