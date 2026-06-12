package com.plusmobileapps.chefmate.grocery.core.impl.edit

import chefmate.client.grocery.core.public.generated.resources.Res
import chefmate.client.grocery.core.public.generated.resources.grocery_invite_error
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.grocery.data.ListCollaborator
import com.plusmobileapps.chefmate.grocery.data.ListRole
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.TextData
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditGroceryListViewModel(
    private val listId: Long,
    mainContext: CoroutineContext,
    private val repository: GroceryRepository,
    authRepository: AuthenticationRepository,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(State())
    private var nameInitialized = false

    val state: StateFlow<State> = _state.asStateFlow()

    private val output = Channel<Output>(Channel.BUFFERED)
    val outputs: Flow<Output> = output.receiveAsFlow()

    init {
        scope.launch {
            repository.getGroceryLists().collect { lists ->
                val list = lists.firstOrNull { it.id == listId } ?: return@collect
                _state.update {
                    it.copy(
                        // Seed the editable name once so live edits aren't clobbered by sync.
                        name = if (nameInitialized) it.name else list.name,
                        isOwner = list.role == ListRole.OWNER,
                    )
                }
                nameInitialized = true
            }
        }

        scope.launch {
            repository.getListCollaborators(listId).collect { collaborators ->
                _state.update { it.copy(collaborators = collaborators) }
            }
        }

        scope.launch {
            authRepository.state.collect { authState ->
                val authenticated =
                    authState is AuthState.Authenticated && !authState.user.isAnonymous
                _state.update { it.copy(isAuthenticated = authenticated) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        output.close()
    }

    fun onNameChanged(name: String) {
        nameInitialized = true
        _state.update { it.copy(name = name) }
    }

    fun onRenameSubmitted() {
        val name = _state.value.name.trim()
        if (name.isBlank()) return
        scope.launch { runCatching { repository.renameGroceryList(listId, name) } }
    }

    fun onDeleteClicked() {
        _state.update { it.copy(showDeleteConfirm = true) }
    }

    fun onDeleteDismissed() {
        _state.update { it.copy(showDeleteConfirm = false) }
    }

    fun onDeleteConfirmed() {
        _state.update { it.copy(showDeleteConfirm = false) }
        scope.launch {
            // Only navigate away once the delete actually succeeds; a backend failure shouldn't
            // crash the app or leave the user on a screen for a list that's already gone.
            val deleted = runCatching { repository.deleteGroceryList(listId) }.isSuccess
            if (deleted) output.send(Output.Finished)
        }
    }

    fun onInviteEmailChanged(email: String) {
        _state.update { it.copy(inviteEmail = email, inviteError = null) }
    }

    fun onInviteRoleChanged(role: ListRole) {
        _state.update { it.copy(inviteRole = role) }
    }

    fun onInviteClicked() {
        val current = _state.value
        if (current.isInviting) return
        // Normalise so the address always matches the invitee's (lowercased) account email used by
        // the RLS / pending-invite trigger comparisons.
        val email = current.inviteEmail.trim().lowercase()
        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            _state.update { it.copy(inviteError = INVITE_ERROR) }
            return
        }
        _state.update { it.copy(isInviting = true, inviteError = null) }
        scope.launch {
            val result = runCatching {
                repository.inviteCollaborator(listId, email, current.inviteRole)
            }
            _state.update {
                if (result.isSuccess) {
                    it.copy(isInviting = false, inviteEmail = "", inviteError = null)
                } else {
                    it.copy(isInviting = false, inviteError = INVITE_ERROR)
                }
            }
        }
    }

    fun onRemoveCollaboratorClicked(collaborator: ListCollaborator) {
        _state.update { it.copy(collaboratorPendingRemoval = collaborator) }
    }

    fun onDismissRemoveCollaborator() {
        _state.update { it.copy(collaboratorPendingRemoval = null) }
    }

    fun onConfirmRemoveCollaborator() {
        val collaborator = _state.value.collaboratorPendingRemoval ?: return
        _state.update { it.copy(collaboratorPendingRemoval = null) }
        scope.launch { runCatching { repository.removeCollaborator(listId, collaborator.id) } }
    }

    data class State(
        val name: String = "",
        val isAuthenticated: Boolean = false,
        val isOwner: Boolean = true,
        val collaborators: List<ListCollaborator> = emptyList(),
        val showDeleteConfirm: Boolean = false,
        val collaboratorPendingRemoval: ListCollaborator? = null,
        val inviteEmail: String = "",
        val inviteRole: ListRole = ListRole.EDITOR,
        val isInviting: Boolean = false,
        val inviteError: TextData? = null,
    )

    sealed class Output {
        data object Finished : Output()
    }

    private companion object {
        val INVITE_ERROR: TextData = ResourceString(Res.string.grocery_invite_error)
    }
}
