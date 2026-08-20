package com.plusmobileapps.chefmate.family.core.impl

import chefmate.client.family.core.public.generated.resources.Res
import chefmate.client.family.core.public.generated.resources.family_action_error
import chefmate.client.family.core.public.generated.resources.family_already_in_family
import chefmate.client.family.core.public.generated.resources.family_create_error
import chefmate.client.family.core.public.generated.resources.family_invite_error
import chefmate.client.family.core.public.generated.resources.family_rename_error
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.family.core.FamilyBloc.FamilyAction
import com.plusmobileapps.chefmate.family.core.FamilyBloc.Model
import com.plusmobileapps.chefmate.family.data.AlreadyInFamilyException
import com.plusmobileapps.chefmate.family.data.FamilyRepository
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.text.asTextData
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class FamilyViewModel(
    @Main mainContext: CoroutineContext,
    private val repository: FamilyRepository,
    authenticationRepository: AuthenticationRepository,
) : ViewModel(mainContext) {

    /**
     * Everything the user is typing or confirming. Kept separate from the repository's state so a
     * realtime member update doesn't wipe a half-typed invite address.
     */
    private data class UiState(
        val newFamilyName: String = "",
        val isCreating: Boolean = false,
        val createError: TextData? = null,
        val editingName: String? = null,
        val isRenaming: Boolean = false,
        val renameError: TextData? = null,
        val inviteEmail: String = "",
        val isInviting: Boolean = false,
        val inviteError: TextData? = null,
        val removingMemberId: String? = null,
        val pendingFamilyAction: FamilyAction? = null,
        val isRemovingFamily: Boolean = false,
        val familyActionError: TextData? = null,
    )

    private val uiState = MutableStateFlow(UiState())

    val state: StateFlow<Model> =
        combine(
                repository.family,
                repository.members(),
                authenticationRepository.state,
                uiState,
            ) { family, members, authState, ui ->
                Model(
                    isLoading = false,
                    isSignedIn =
                        (authState as? AuthState.Authenticated)?.user?.isAnonymous == false,
                    family = family,
                    members = members.toImmutableList(),
                    isOwner = family?.isOwnedByCurrentUser == true,
                    newFamilyName = ui.newFamilyName,
                    isCreating = ui.isCreating,
                    createError = ui.createError,
                    editingName = ui.editingName,
                    isRenaming = ui.isRenaming,
                    renameError = ui.renameError,
                    inviteEmail = ui.inviteEmail,
                    isInviting = ui.isInviting,
                    inviteError = ui.inviteError,
                    // Resolve the id against the live member list so a member removed elsewhere
                    // takes the dialog down with them instead of stranding it. Guard the null id
                    // explicitly — the owner's synthesized row also has a null id, so an unguarded
                    // `it.id == removingMemberId` would match the owner whenever no removal is
                    // pending and pop the dialog open by itself.
                    removingMember =
                        ui.removingMemberId?.let { id -> members.firstOrNull { it.id == id } },
                    pendingFamilyAction = ui.pendingFamilyAction,
                    isRemovingFamily = ui.isRemovingFamily,
                    familyActionError = ui.familyActionError,
                )
            }
            .stateIn(
                scope,
                SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                Model(members = persistentListOf()),
            )

    init {
        // The cache may have gone stale since the last realtime emission (app backgrounded, invite
        // accepted elsewhere), so pull once on entry.
        scope.launch { runCatching { repository.refresh() } }
    }

    fun onNewFamilyNameChanged(name: String) {
        uiState.update { it.copy(newFamilyName = name, createError = null) }
    }

    fun createFamily() {
        val name = uiState.value.newFamilyName.trim()
        if (name.isBlank() || uiState.value.isCreating) return
        uiState.update { it.copy(isCreating = true, createError = null) }
        scope.launch {
            runCatching { repository.createFamily(name) }
                .onSuccess { uiState.update { it.copy(isCreating = false, newFamilyName = "") } }
                .onFailure { error ->
                    uiState.update {
                        it.copy(isCreating = false, createError = error.createMessage())
                    }
                }
        }
    }

    fun startRename() {
        uiState.update {
            it.copy(editingName = repository.family.value?.name.orEmpty(), renameError = null)
        }
    }

    fun onEditingNameChanged(name: String) {
        uiState.update { it.copy(editingName = name, renameError = null) }
    }

    fun cancelRename() {
        uiState.update { it.copy(editingName = null, isRenaming = false, renameError = null) }
    }

    fun confirmRename() {
        val name = uiState.value.editingName?.trim().orEmpty()
        if (name.isBlank() || uiState.value.isRenaming) return
        uiState.update { it.copy(isRenaming = true) }
        scope.launch {
            runCatching { repository.renameFamily(name) }
                .onSuccess {
                    uiState.update {
                        it.copy(isRenaming = false, editingName = null, renameError = null)
                    }
                }
                .onFailure {
                    // Keep the field open with what they typed so the retry doesn't lose it.
                    uiState.update {
                        it.copy(
                            isRenaming = false,
                            renameError = Res.string.family_rename_error.asTextData(),
                        )
                    }
                }
        }
    }

    fun onInviteEmailChanged(email: String) {
        uiState.update { it.copy(inviteEmail = email, inviteError = null) }
    }

    fun invite() {
        val email = uiState.value.inviteEmail.trim()
        if (email.isBlank() || uiState.value.isInviting) return
        uiState.update { it.copy(isInviting = true, inviteError = null) }
        scope.launch {
            runCatching { repository.invite(email) }
                .onSuccess { uiState.update { it.copy(isInviting = false, inviteEmail = "") } }
                .onFailure {
                    uiState.update {
                        it.copy(
                            isInviting = false,
                            inviteError = Res.string.family_invite_error.asTextData(),
                        )
                    }
                }
        }
    }

    fun startRemoveMember(memberId: String) {
        uiState.update { it.copy(removingMemberId = memberId) }
    }

    fun dismissRemoveMember() {
        uiState.update { it.copy(removingMemberId = null) }
    }

    fun confirmRemoveMember() {
        val memberId = uiState.value.removingMemberId ?: return
        uiState.update { it.copy(removingMemberId = null) }
        scope.launch {
            runCatching { repository.removeMember(memberId) }
                .onFailure {
                    uiState.update {
                        it.copy(familyActionError = Res.string.family_action_error.asTextData())
                    }
                }
        }
    }

    fun startFamilyAction(action: FamilyAction) {
        uiState.update { it.copy(pendingFamilyAction = action, familyActionError = null) }
    }

    fun dismissFamilyAction() {
        uiState.update { it.copy(pendingFamilyAction = null) }
    }

    fun confirmFamilyAction() {
        val action = uiState.value.pendingFamilyAction ?: return
        uiState.update { it.copy(pendingFamilyAction = null, isRemovingFamily = true) }
        scope.launch {
            runCatching {
                when (action) {
                    FamilyAction.DELETE -> repository.deleteFamily()
                    FamilyAction.LEAVE -> repository.leaveFamily()
                }
            }
                .onSuccess { uiState.update { it.copy(isRemovingFamily = false) } }
                .onFailure {
                    uiState.update {
                        it.copy(
                            isRemovingFamily = false,
                            familyActionError = Res.string.family_action_error.asTextData(),
                        )
                    }
                }
        }
    }

    private fun Throwable.createMessage() =
        if (this is AlreadyInFamilyException) {
            Res.string.family_already_in_family.asTextData()
        } else {
            Res.string.family_create_error.asTextData()
        }

    private companion object {
        const val STOP_TIMEOUT_MS = 5000L
    }
}
