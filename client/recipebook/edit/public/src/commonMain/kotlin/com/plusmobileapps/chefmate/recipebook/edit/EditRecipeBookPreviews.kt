package com.plusmobileapps.chefmate.recipebook.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import chefmate.client.recipebook.edit.public.generated.resources.Res
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_create_title
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_edit_title
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_leave_error
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_name_error
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookMember
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookMemberStatus
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRole
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookBloc.Model
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.flow.MutableStateFlow

private fun editRecipeBookBloc(model: Model): EditRecipeBookBloc =
    object : EditRecipeBookBloc {
        override val state = MutableStateFlow(model)

        override fun onNameChanged(name: String) = Unit

        override fun onSaveClicked() = Unit

        override fun onCloseClicked() = Unit

        override fun onInviteEmailChanged(email: String) = Unit

        override fun onInviteRoleChanged(role: RecipeBookRole) = Unit

        override fun onInviteClicked() = Unit

        override fun onRemoveMemberClicked(memberId: String) = Unit

        override fun onConfirmRemoveMember() = Unit

        override fun onDismissRemoveMember() = Unit

        override fun onDeleteBookClicked() = Unit

        override fun onLeaveBookClicked() = Unit

        override fun onConfirmBookAction() = Unit

        override fun onDismissBookAction() = Unit
    }

val previewEditRecipeBookCreateBloc: EditRecipeBookBloc =
    editRecipeBookBloc(
        Model(title = Res.string.edit_recipe_book_create_title.asTextData(), isCreate = true)
    )

val previewEditRecipeBookEditBloc: EditRecipeBookBloc =
    editRecipeBookBloc(
        Model(
            title = Res.string.edit_recipe_book_edit_title.asTextData(),
            name = "Weeknight Dinners",
            isCreate = false,
        )
    )

val previewEditRecipeBookSavingBloc: EditRecipeBookBloc =
    editRecipeBookBloc(
        Model(
            title = Res.string.edit_recipe_book_create_title.asTextData(),
            name = "Holiday Baking",
            isSaving = true,
        )
    )

val previewEditRecipeBookErrorBloc: EditRecipeBookBloc =
    editRecipeBookBloc(
        Model(
            title = Res.string.edit_recipe_book_create_title.asTextData(),
            name = "",
            nameError = Res.string.edit_recipe_book_name_error.asTextData(),
        )
    )

val previewEditRecipeBookCollaboratorsBloc: EditRecipeBookBloc =
    editRecipeBookBloc(
        Model(
            title = Res.string.edit_recipe_book_edit_title.asTextData(),
            name = "Weeknight Dinners",
            isCreate = false,
            canManageCollaborators = true,
            canDeleteBook = true,
            members =
                listOf(
                    RecipeBookMember(
                        id = null,
                        email = "you@example.com",
                        role = RecipeBookRole.OWNER,
                        status = RecipeBookMemberStatus.ACCEPTED,
                        name = "Jordan Lee",
                        isOwner = true,
                    ),
                    RecipeBookMember(
                        id = "1",
                        email = "alex@example.com",
                        role = RecipeBookRole.EDITOR,
                        status = RecipeBookMemberStatus.ACCEPTED,
                        name = "Alex Rivera",
                    ),
                    RecipeBookMember(
                        id = "2",
                        email = "sam@example.com",
                        role = RecipeBookRole.VIEWER,
                        status = RecipeBookMemberStatus.PENDING,
                    ),
                    RecipeBookMember(
                        id = "3",
                        email = "jordan@example.com",
                        role = RecipeBookRole.EDITOR,
                        status = RecipeBookMemberStatus.REJECTED,
                        name = "Jordan Kim",
                    ),
                ),
        )
    )

/** A non-owner collaborator viewing the book: read-only list, no invite controls, can leave. */
val previewEditRecipeBookMemberViewBloc: EditRecipeBookBloc =
    editRecipeBookBloc(
        Model(
            title = Res.string.edit_recipe_book_edit_title.asTextData(),
            name = "Weeknight Dinners",
            isCreate = false,
            canManageCollaborators = false,
            canLeaveBook = true,
            members =
                listOf(
                    RecipeBookMember(
                        id = null,
                        email = "casey@example.com",
                        role = RecipeBookRole.OWNER,
                        status = RecipeBookMemberStatus.ACCEPTED,
                        name = "Casey Morgan",
                        isOwner = true,
                    ),
                    RecipeBookMember(
                        id = "1",
                        email = "you@example.com",
                        role = RecipeBookRole.EDITOR,
                        status = RecipeBookMemberStatus.ACCEPTED,
                        name = "Jordan Lee",
                    ),
                    RecipeBookMember(
                        id = "2",
                        email = "sam@example.com",
                        role = RecipeBookRole.VIEWER,
                        status = RecipeBookMemberStatus.PENDING,
                    ),
                ),
        )
    )

/** Owner view with the remove-collaborator confirmation dialog showing. */
val previewEditRecipeBookRemoveConfirmBloc: EditRecipeBookBloc =
    editRecipeBookBloc(
        Model(
            title = Res.string.edit_recipe_book_edit_title.asTextData(),
            name = "Weeknight Dinners",
            isCreate = false,
            canManageCollaborators = true,
            members =
                listOf(
                    RecipeBookMember(
                        id = null,
                        email = "you@example.com",
                        role = RecipeBookRole.OWNER,
                        status = RecipeBookMemberStatus.ACCEPTED,
                        name = "Jordan Lee",
                        isOwner = true,
                    ),
                    RecipeBookMember(
                        id = "1",
                        email = "alex@example.com",
                        role = RecipeBookRole.EDITOR,
                        status = RecipeBookMemberStatus.ACCEPTED,
                        name = "Alex Rivera",
                    ),
                ),
            removingMember =
                RecipeBookMember(
                    id = "1",
                    email = "alex@example.com",
                    role = RecipeBookRole.EDITOR,
                    status = RecipeBookMemberStatus.ACCEPTED,
                    name = "Alex Rivera",
                ),
        )
    )

/** Owner view with the delete-book confirmation dialog showing. */
val previewEditRecipeBookDeleteConfirmBloc: EditRecipeBookBloc =
    editRecipeBookBloc(
        Model(
            title = Res.string.edit_recipe_book_edit_title.asTextData(),
            name = "Weeknight Dinners",
            isCreate = false,
            canManageCollaborators = true,
            canDeleteBook = true,
            pendingBookAction = EditRecipeBookBloc.BookAction.DELETE,
        )
    )

/** Collaborator view with the leave-book confirmation dialog showing. */
val previewEditRecipeBookLeaveConfirmBloc: EditRecipeBookBloc =
    editRecipeBookBloc(
        Model(
            title = Res.string.edit_recipe_book_edit_title.asTextData(),
            name = "Weeknight Dinners",
            isCreate = false,
            canLeaveBook = true,
            pendingBookAction = EditRecipeBookBloc.BookAction.LEAVE,
        )
    )

/** Collaborator view after a failed leave — the destructive button carries an inline error. */
val previewEditRecipeBookLeaveErrorBloc: EditRecipeBookBloc =
    editRecipeBookBloc(
        Model(
            title = Res.string.edit_recipe_book_edit_title.asTextData(),
            name = "Weeknight Dinners",
            isCreate = false,
            canLeaveBook = true,
            bookActionError = Res.string.edit_recipe_book_leave_error.asTextData(),
        )
    )

@Preview
@Composable
internal fun EditRecipeBookCreatePreview() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookCreateBloc) }
}

@Preview
@Composable
internal fun EditRecipeBookEditPreview() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookEditBloc) }
}

@Preview
@Composable
internal fun EditRecipeBookErrorPreview() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookErrorBloc) }
}

@Preview
@Composable
internal fun EditRecipeBookCreateDarkPreview() {
    ChefMateTheme(darkTheme = true) { EditRecipeBookScreen(bloc = previewEditRecipeBookCreateBloc) }
}

@Preview
@Composable
internal fun EditRecipeBookCollaboratorsPreview() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookCollaboratorsBloc) }
}

@Preview
@Composable
internal fun EditRecipeBookMemberViewPreview() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookMemberViewBloc) }
}

@Preview
@Composable
internal fun EditRecipeBookDeleteConfirmPreview() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookDeleteConfirmBloc) }
}

@Preview
@Composable
internal fun EditRecipeBookLeaveConfirmPreview() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookLeaveConfirmBloc) }
}
