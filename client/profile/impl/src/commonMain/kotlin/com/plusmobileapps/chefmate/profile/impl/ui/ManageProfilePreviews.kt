package com.plusmobileapps.chefmate.profile.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.profile.ManageProfileBloc
import com.plusmobileapps.chefmate.profile.ManageProfileBloc.Model
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.flow.MutableStateFlow

private fun manageProfileBloc(model: Model): ManageProfileBloc =
    object : ManageProfileBloc {
        override val state = MutableStateFlow(model)

        override fun onBack() = Unit

        override fun onDisplayNameChanged(displayName: String) = Unit

        override fun onSaveClicked() = Unit

        override fun onDeleteAccountClicked() = Unit

        override fun onDeleteConfirmed() = Unit

        override fun onDeleteDismissed() = Unit

        @Composable
        override fun Content(modifier: Modifier) {
            ManageProfileScreen(bloc = this, modifier = modifier)
        }
    }

val previewManageProfileBloc: ManageProfileBloc =
    manageProfileBloc(Model(displayName = "Julia Child", email = "julia@example.com"))

val previewManageProfileSavingBloc: ManageProfileBloc =
    manageProfileBloc(
        Model(displayName = "Julia Child", email = "julia@example.com", isSaving = true)
    )

val previewManageProfileDeleteDialogBloc: ManageProfileBloc =
    manageProfileBloc(
        Model(displayName = "Julia Child", email = "julia@example.com", showDeleteDialog = true)
    )

val previewManageProfileDeleteErrorBloc: ManageProfileBloc =
    manageProfileBloc(
        Model(
            displayName = "Julia Child",
            email = "julia@example.com",
            deleteError = FixedString("Couldn’t delete your account. Please try again."),
        )
    )

@Preview
@Composable
internal fun ManageProfilePreview() {
    ChefMateTheme { previewManageProfileBloc.Content(Modifier) }
}

@Preview
@Composable
internal fun ManageProfileDeleteDialogPreview() {
    ChefMateTheme { previewManageProfileDeleteDialogBloc.Content(Modifier) }
}
