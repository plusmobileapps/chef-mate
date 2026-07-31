package com.plusmobileapps.chefmate.profile.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.profile.ManageProfileBloc
import com.plusmobileapps.chefmate.profile.ManageProfileBloc.Model
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import com.plusmobileapps.chefmate.util.PickedImage
import kotlinx.coroutines.flow.MutableStateFlow

private fun manageProfileBloc(model: Model): ManageProfileBloc =
    object : ManageProfileBloc {
        override val state = MutableStateFlow(model)

        override fun onBack() = Unit

        override fun onDisplayNameChanged(displayName: String) = Unit

        override fun onHandleChanged(handle: String) = Unit

        override fun onBioChanged(bio: String) = Unit

        override fun onPhotoPicked(image: PickedImage) = Unit

        override fun onSaveClicked() = Unit

        override fun onDeleteAccountClicked() = Unit

        override fun onDeleteConfirmationChanged(confirmation: String) = Unit

        override fun onDeleteConfirmed() = Unit

        override fun onDeleteDismissed() = Unit
    }

val previewManageProfileBloc: ManageProfileBloc =
    manageProfileBloc(
        Model(displayName = "Julia Child", email = "julia@example.com", photoUrl = null)
    )

val previewManageProfileSavingBloc: ManageProfileBloc =
    manageProfileBloc(
        Model(displayName = "Julia Child", email = "julia@example.com", isSaving = true)
    )

val previewManageProfileDeleteDialogBloc: ManageProfileBloc =
    manageProfileBloc(
        Model(displayName = "Julia Child", email = "julia@example.com", showDeleteDialog = true)
    )

val previewManageProfileDeleteDialogConfirmableBloc: ManageProfileBloc =
    manageProfileBloc(
        Model(
            displayName = "Julia Child",
            email = "julia@example.com",
            showDeleteDialog = true,
            deleteConfirmation = "julia@example.com",
        )
    )

/** First-time claim: the handle field is editable and the typed handle is free. */
val previewManageProfileClaimHandleBloc: ManageProfileBloc =
    manageProfileBloc(
        Model(
            displayName = "Julia Child",
            email = "julia@example.com",
            handle = "juliachild",
            bio = "French cooking, demystified.",
            handleStatus = ManageProfileBloc.HandleStatus.Available,
        )
    )

/** Someone got there first — the save button stays disabled. */
val previewManageProfileHandleTakenBloc: ManageProfileBloc =
    manageProfileBloc(
        Model(
            displayName = "Julia Child",
            email = "julia@example.com",
            handle = "julia",
            handleStatus = ManageProfileBloc.HandleStatus.Taken,
        )
    )

/** Already claimed: the handle is locked and explained as permanent. */
val previewManageProfileClaimedBloc: ManageProfileBloc =
    manageProfileBloc(
        Model(
            displayName = "Julia Child",
            email = "julia@example.com",
            handle = "juliachild",
            isHandleClaimed = true,
            bio = "French cooking, demystified.",
        )
    )

val previewManageProfileErrorBloc: ManageProfileBloc =
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
