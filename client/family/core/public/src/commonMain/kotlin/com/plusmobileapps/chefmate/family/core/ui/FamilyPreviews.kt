package com.plusmobileapps.chefmate.family.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.family.core.FamilyBloc
import com.plusmobileapps.chefmate.family.data.Family
import com.plusmobileapps.chefmate.family.data.FamilyMember
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fake [FamilyBloc] for previews and screenshot tests. Public so `client/ui/screenshot-test` can
 * reuse it; all handlers are no-ops.
 */
private class PreviewFamilyBloc(model: FamilyBloc.Model) : FamilyBloc {
    override val state: StateFlow<FamilyBloc.Model> = MutableStateFlow(model)

    override fun onBack() = Unit

    override fun onSignInClicked() = Unit

    override fun onSignUpClicked() = Unit

    override fun onNewFamilyNameChanged(name: String) = Unit

    override fun onCreateFamilyClicked() = Unit

    override fun onRenameClicked() = Unit

    override fun onEditingNameChanged(name: String) = Unit

    override fun onRenameConfirmed() = Unit

    override fun onRenameCancelled() = Unit

    override fun onInviteEmailChanged(email: String) = Unit

    override fun onInviteClicked() = Unit

    override fun onRemoveMemberClicked(memberId: String) = Unit

    override fun onConfirmRemoveMember() = Unit

    override fun onDismissRemoveMember() = Unit

    override fun onLeaveFamilyClicked() = Unit

    override fun onDeleteFamilyClicked() = Unit

    override fun onConfirmFamilyAction() = Unit

    override fun onDismissFamilyAction() = Unit
}

/** Signed out — the create form is hidden behind a sign-in prompt. */
val previewFamilySignedOutBloc: FamilyBloc =
    PreviewFamilyBloc(FamilyBloc.Model(isLoading = false, isSignedIn = false))

/** Signed in but not in a family yet. */
val previewFamilyEmptyBloc: FamilyBloc = PreviewFamilyBloc(FamilyBloc.Model(isLoading = false))

/** In a family the user owns: rename, invite, remove, and delete are all available. */
val previewFamilyOwnerBloc: FamilyBloc =
    PreviewFamilyBloc(
        FamilyBloc.Model(
            isLoading = false,
            family = Family.Sample,
            members = FamilyMember.Samples.toImmutableList(),
            isOwner = true,
        )
    )

/** In a family someone else owns: read-only membership plus "Leave family". */
val previewFamilyMemberBloc: FamilyBloc =
    PreviewFamilyBloc(
        FamilyBloc.Model(
            isLoading = false,
            family = Family.Sample.copy(isOwnedByCurrentUser = false),
            members = FamilyMember.Samples.toImmutableList(),
            isOwner = false,
        )
    )

/** Owner mid-rename, with the inline name field showing. */
val previewFamilyRenamingBloc: FamilyBloc =
    PreviewFamilyBloc(
        FamilyBloc.Model(
            isLoading = false,
            family = Family.Sample,
            members = FamilyMember.Samples.toImmutableList(),
            isOwner = true,
            editingName = "The Hendersons",
        )
    )

@Preview
@Composable
internal fun FamilySignedOutPreview() {
    ChefMateTheme { FamilyScreen(bloc = previewFamilySignedOutBloc, modifier = Modifier) }
}

@Preview
@Composable
internal fun FamilyEmptyPreview() {
    ChefMateTheme { FamilyScreen(bloc = previewFamilyEmptyBloc, modifier = Modifier) }
}

@Preview
@Composable
internal fun FamilyOwnerPreview() {
    ChefMateTheme { FamilyScreen(bloc = previewFamilyOwnerBloc, modifier = Modifier) }
}

@Preview
@Composable
internal fun FamilyMemberPreview() {
    ChefMateTheme { FamilyScreen(bloc = previewFamilyMemberBloc, modifier = Modifier) }
}
