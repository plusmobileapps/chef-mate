package com.plusmobileapps.chefmate.recipebook.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import chefmate.client.recipebook.edit.public.generated.resources.Res
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_create_title
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_edit_title
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_name_error
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookMember
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
            members =
                listOf(
                    RecipeBookMember(
                        id = null,
                        email = "you@example.com",
                        role = RecipeBookRole.OWNER,
                        accepted = true,
                        name = "Jordan Lee",
                        isOwner = true,
                    ),
                    RecipeBookMember(
                        id = "1",
                        email = "alex@example.com",
                        role = RecipeBookRole.EDITOR,
                        accepted = true,
                        name = "Alex Rivera",
                    ),
                    RecipeBookMember(
                        id = "2",
                        email = "sam@example.com",
                        role = RecipeBookRole.VIEWER,
                        accepted = false,
                    ),
                ),
        )
    )

/** A non-owner collaborator viewing the book: read-only list, no invite controls. */
val previewEditRecipeBookMemberViewBloc: EditRecipeBookBloc =
    editRecipeBookBloc(
        Model(
            title = Res.string.edit_recipe_book_edit_title.asTextData(),
            name = "Weeknight Dinners",
            isCreate = false,
            canManageCollaborators = false,
            members =
                listOf(
                    RecipeBookMember(
                        id = null,
                        email = "casey@example.com",
                        role = RecipeBookRole.OWNER,
                        accepted = true,
                        name = "Casey Morgan",
                        isOwner = true,
                    ),
                    RecipeBookMember(
                        id = "1",
                        email = "you@example.com",
                        role = RecipeBookRole.EDITOR,
                        accepted = true,
                        name = "Jordan Lee",
                    ),
                    RecipeBookMember(
                        id = "2",
                        email = "sam@example.com",
                        role = RecipeBookRole.VIEWER,
                        accepted = false,
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
                        accepted = true,
                        name = "Jordan Lee",
                        isOwner = true,
                    ),
                    RecipeBookMember(
                        id = "1",
                        email = "alex@example.com",
                        role = RecipeBookRole.EDITOR,
                        accepted = true,
                        name = "Alex Rivera",
                    ),
                ),
            removingMember =
                RecipeBookMember(
                    id = "1",
                    email = "alex@example.com",
                    role = RecipeBookRole.EDITOR,
                    accepted = true,
                    name = "Alex Rivera",
                ),
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
