package com.plusmobileapps.chefmate.profile.impl

import chefmate.client.profile.public.generated.resources.Res
import chefmate.client.profile.public.generated.resources.manage_profile_delete_error
import chefmate.client.profile.public.generated.resources.manage_profile_save_error
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.ProfilePhotoStorage
import com.plusmobileapps.chefmate.auth.usecase.DeleteAccountUseCase
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.profile.ManageProfileBloc.Model
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.util.PickedImage
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.channels.Channel
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
    private val deleteAccountUseCase: DeleteAccountUseCase,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<Model> = _state.asStateFlow()

    /** Emitted when the screen should pop: after a successful save or account deletion. */
    private val _outputs = Channel<Output>(Channel.BUFFERED)
    val outputs: Flow<Output> = _outputs.receiveAsFlow()

    private val pickedImage = MutableStateFlow<PickedImage?>(null)

    private fun initialState(): Model {
        val user = (authenticationRepository.state.value as? AuthState.Authenticated)?.user
        return Model(
            displayName = user?.userName.orEmpty(),
            email = user?.userEmail.orEmpty(),
            photoUrl = user?.userProfileImageUrl,
        )
    }

    fun setDisplayName(displayName: String) {
        _state.update { it.copy(displayName = displayName, saveError = null) }
    }

    fun setPhoto(image: PickedImage) {
        pickedImage.value = image
        _state.update { it.copy(pickedPhoto = image.bytes, saveError = null) }
    }

    fun save() {
        val current = _state.value
        if (!current.canSave) return
        _state.update { it.copy(isSaving = true, saveError = null) }
        scope.launch {
            val avatarUrl =
                pickedImage.value?.let { picked ->
                    runCatching {
                            profilePhotoStorage.uploadPhoto(picked.bytes, picked.fileExtension)
                        }
                        .getOrElse {
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    saveError = Res.string.manage_profile_save_error.asTextData(),
                                )
                            }
                            return@launch
                        }
                }
            authenticationRepository
                .updateProfile(displayName = current.displayName.trim(), avatarUrl = avatarUrl)
                .onSuccess { _outputs.send(Output.Saved) }
                .onFailure {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            saveError = Res.string.manage_profile_save_error.asTextData(),
                        )
                    }
                }
        }
    }

    fun showDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = true, deleteError = null) }
    }

    fun dismissDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteAccount() {
        _state.update { it.copy(showDeleteDialog = false, isDeleting = true, deleteError = null) }
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
}
