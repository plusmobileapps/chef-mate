package com.plusmobileapps.chefmate.grocery.core.impl.edit

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.grocery.data.ListCollaborator
import com.plusmobileapps.chefmate.grocery.data.ListRole
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
        scope.launch { repository.renameGroceryList(listId, name) }
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
            repository.deleteGroceryList(listId)
            output.send(Output.Finished)
        }
    }

    fun onInviteCollaborator(email: String, role: ListRole) {
        val trimmed = email.trim()
        if (trimmed.isBlank()) return
        scope.launch { repository.inviteCollaborator(listId, trimmed, role) }
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
        scope.launch { repository.removeCollaborator(listId, collaborator.id) }
    }

    data class State(
        val name: String = "",
        val isAuthenticated: Boolean = false,
        val isOwner: Boolean = true,
        val collaborators: List<ListCollaborator> = emptyList(),
        val showDeleteConfirm: Boolean = false,
        val collaboratorPendingRemoval: ListCollaborator? = null,
    )

    sealed class Output {
        data object Finished : Output()
    }
}
