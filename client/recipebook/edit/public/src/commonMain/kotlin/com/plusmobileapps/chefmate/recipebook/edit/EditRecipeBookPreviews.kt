package com.plusmobileapps.chefmate.recipebook.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

        override fun onRemoveMember(memberId: String) = Unit

        @Composable
        override fun Content(modifier: Modifier) {
            EditRecipeBookScreen(bloc = this, modifier = modifier)
        }
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
                        isOwner = true,
                    ),
                    RecipeBookMember(
                        id = "1",
                        email = "alex@example.com",
                        role = RecipeBookRole.EDITOR,
                        accepted = true,
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
