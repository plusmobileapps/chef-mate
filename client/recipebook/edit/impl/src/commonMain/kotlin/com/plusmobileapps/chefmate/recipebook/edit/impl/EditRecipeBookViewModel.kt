package com.plusmobileapps.chefmate.recipebook.edit.impl

import chefmate.client.recipebook.edit.public.generated.resources.Res
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_create_title
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_delete_error
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_edit_title
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_invite_email_error
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_leave_error
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_name_error
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookCollaborationRepository
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookMember
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRepository
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRole
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookBloc
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.TextData
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AssistedInject
class EditRecipeBookViewModel(
    @Assisted private val props: EditRecipeBookBloc.Props,
    @Assisted private val onSaved: () -> Unit,
    @Main mainContext: CoroutineContext,
    @IO private val ioContext: CoroutineContext,
    private val repository: RecipeBookRepository,
    private val collaborationRepository: RecipeBookCollaborationRepository,
) : ViewModel(mainContext) {

    private val isCreate = props is EditRecipeBookBloc.Props.Create

    private val _state =
        MutableStateFlow(
            State(
                title =
                    ResourceString(
                        if (isCreate) Res.string.edit_recipe_book_create_title
                        else Res.string.edit_recipe_book_edit_title
                    ),
                isCreate = isCreate,
            )
        )
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        if (props is EditRecipeBookBloc.Props.Edit) {
            scope.launch {
                val book = withContext(ioContext) { repository.getRecipeBook(props.bookId).first() }
                if (book != null) {
                    _state.update { it.copy(name = book.name) }
                }
                val owner = withContext(ioContext) { collaborationRepository.isOwner(props.bookId) }
                _state.update {
                    it.copy(
                        canManageCollaborators = owner,
                        // The default "My Recipes" book is the fallback for unfiled recipes, so
                        // it can't be deleted. Only a non-owner has a membership to give up.
                        canDeleteBook = owner && book?.isDefault == false,
                        canLeaveBook = !owner,
                    )
                }
                // Everyone on the book sees the collaborator list; only the owner can
                // invite/remove.
                loadMembers(props.bookId)
            }
        }
    }

    private suspend fun loadMembers(bookId: Long) {
        val members = withContext(ioContext) { collaborationRepository.getMembers(bookId) }
        _state.update { it.copy(members = members) }
    }

    fun onNameChanged(name: String) {
        _state.update { it.copy(name = name, nameError = null) }
    }

    fun onInviteEmailChanged(email: String) {
        _state.update { it.copy(inviteEmail = email, inviteError = null) }
    }

    fun onInviteRoleChanged(role: RecipeBookRole) {
        _state.update { it.copy(inviteRole = role) }
    }

    fun onInviteClicked() {
        val bookId = (props as? EditRecipeBookBloc.Props.Edit)?.bookId ?: return
        val current = _state.value
        if (current.isInviting) return
        val email = current.inviteEmail.trim()
        if (!email.isValidEmail()) {
            _state.update {
                it.copy(
                    inviteError = ResourceString(Res.string.edit_recipe_book_invite_email_error)
                )
            }
            return
        }
        _state.update { it.copy(isInviting = true, inviteError = null) }
        scope.launch {
            try {
                withContext(ioContext) {
                    collaborationRepository.invite(bookId, email, current.inviteRole)
                }
                _state.update { it.copy(inviteEmail = "") }
                loadMembers(bookId)
            } catch (_: Throwable) {
                _state.update {
                    it.copy(
                        inviteError = ResourceString(Res.string.edit_recipe_book_invite_email_error)
                    )
                }
            } finally {
                _state.update { it.copy(isInviting = false) }
            }
        }
    }

    fun onRemoveMemberClicked(memberId: String) {
        val member = _state.value.members.firstOrNull { it.id == memberId } ?: return
        _state.update { it.copy(removingMember = member) }
    }

    fun onDismissRemoveMember() {
        _state.update { it.copy(removingMember = null) }
    }

    fun onConfirmRemoveMember() {
        val bookId = (props as? EditRecipeBookBloc.Props.Edit)?.bookId ?: return
        val memberId = _state.value.removingMember?.id ?: return
        _state.update { it.copy(removingMember = null) }
        scope.launch {
            withContext(ioContext) { collaborationRepository.removeMember(memberId) }
            loadMembers(bookId)
        }
    }

    fun onDeleteBookClicked() {
        if (!_state.value.canDeleteBook) return
        _state.update {
            it.copy(
                pendingBookAction = EditRecipeBookBloc.BookAction.DELETE,
                bookActionError = null,
            )
        }
    }

    fun onLeaveBookClicked() {
        if (!_state.value.canLeaveBook) return
        _state.update {
            it.copy(pendingBookAction = EditRecipeBookBloc.BookAction.LEAVE, bookActionError = null)
        }
    }

    fun onDismissBookAction() {
        _state.update { it.copy(pendingBookAction = null) }
    }

    fun onConfirmBookAction() {
        val bookId = (props as? EditRecipeBookBloc.Props.Edit)?.bookId ?: return
        val current = _state.value
        val action = current.pendingBookAction ?: return
        if (current.isRemovingBook) return
        _state.update { it.copy(pendingBookAction = null, isRemovingBook = true) }
        scope.launch {
            try {
                withContext(ioContext) {
                    when (action) {
                        EditRecipeBookBloc.BookAction.DELETE -> repository.deleteBook(bookId)
                        EditRecipeBookBloc.BookAction.LEAVE ->
                            collaborationRepository.leaveBook(bookId)
                    }
                }
                onSaved()
            } catch (_: Throwable) {
                // Both paths need the server, so a failure here is almost always connectivity.
                _state.update {
                    it.copy(
                        isRemovingBook = false,
                        bookActionError =
                            ResourceString(
                                if (action == EditRecipeBookBloc.BookAction.DELETE) {
                                    Res.string.edit_recipe_book_delete_error
                                } else {
                                    Res.string.edit_recipe_book_leave_error
                                }
                            ),
                    )
                }
            }
        }
    }

    fun onSaveClicked() {
        val current = _state.value
        if (current.isSaving) return
        val name = current.name.trim()
        if (name.isEmpty()) {
            _state.update {
                it.copy(nameError = ResourceString(Res.string.edit_recipe_book_name_error))
            }
            return
        }
        _state.update { it.copy(isSaving = true, nameError = null) }
        scope.launch {
            withContext(ioContext) {
                when (val p = props) {
                    is EditRecipeBookBloc.Props.Create -> repository.createBook(name)
                    is EditRecipeBookBloc.Props.Edit -> repository.renameBook(p.bookId, name)
                }
            }
            onSaved()
        }
    }

    private fun String.isValidEmail(): Boolean =
        isNotBlank() && contains('@') && substringAfter('@').contains('.')

    data class State(
        val title: TextData,
        val name: String = "",
        val isCreate: Boolean = true,
        val isSaving: Boolean = false,
        val nameError: TextData? = null,
        val canManageCollaborators: Boolean = false,
        val members: List<RecipeBookMember> = emptyList(),
        val inviteEmail: String = "",
        val inviteRole: RecipeBookRole = RecipeBookRole.EDITOR,
        val isInviting: Boolean = false,
        val inviteError: TextData? = null,
        val removingMember: RecipeBookMember? = null,
        val canDeleteBook: Boolean = false,
        val canLeaveBook: Boolean = false,
        val isRemovingBook: Boolean = false,
        val pendingBookAction: EditRecipeBookBloc.BookAction? = null,
        val bookActionError: TextData? = null,
    )

    @AssistedFactory
    fun interface Factory {
        fun create(props: EditRecipeBookBloc.Props, onSaved: () -> Unit): EditRecipeBookViewModel
    }
}
