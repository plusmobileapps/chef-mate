package com.plusmobileapps.chefmate.family.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.family.core.ui.FamilyScreen
import com.plusmobileapps.chefmate.family.data.Family
import com.plusmobileapps.chefmate.family.data.FamilyMember
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

/**
 * The single screen behind the More tab's Family row. Renders one of three states depending on
 * [Model]: signed out, not in a family (create one), or in a family (members, invites, danger
 * zone).
 */
interface FamilyBloc : ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        FamilyScreen(bloc = this, modifier = modifier)
    }

    fun onBack()

    fun onSignInClicked()

    fun onSignUpClicked()

    // --- Creating a family ---

    fun onNewFamilyNameChanged(name: String)

    fun onCreateFamilyClicked()

    // --- Renaming (owner only) ---

    /** Owner tapped the edit affordance — seeds and shows the inline rename field. */
    fun onRenameClicked()

    fun onEditingNameChanged(name: String)

    fun onRenameConfirmed()

    fun onRenameCancelled()

    // --- Inviting (owner only) ---

    fun onInviteEmailChanged(email: String)

    fun onInviteClicked()

    // --- Removing a member (owner only) ---

    /** Owner tapped remove on a member — asks for confirmation first. */
    fun onRemoveMemberClicked(memberId: String)

    fun onConfirmRemoveMember()

    fun onDismissRemoveMember()

    // --- Leaving / deleting ---

    /** Member tapped "Leave family" — asks for confirmation first. */
    fun onLeaveFamilyClicked()

    /** Owner tapped "Delete family" — asks for confirmation first. */
    fun onDeleteFamilyClicked()

    fun onConfirmFamilyAction()

    fun onDismissFamilyAction()

    /** The destructive action awaiting confirmation in [Model.pendingFamilyAction]. */
    enum class FamilyAction {
        /** The owner is deleting the family for everyone. */
        DELETE,
        /** A member is removing themselves from the family. */
        LEAVE,
    }

    data class Model(
        val isLoading: Boolean = true,
        /**
         * False when there's no real (non-anonymous) session. A family is tied to account emails,
         * so a signed-out user sees a sign-in prompt instead of the create form.
         */
        val isSignedIn: Boolean = true,
        /** Null when the user isn't in a family — the screen shows the create form instead. */
        val family: Family? = null,
        val members: ImmutableList<FamilyMember> = persistentListOf(),
        /** True when the user owns the family, gating invite/rename/remove/delete. */
        val isOwner: Boolean = false,
        val newFamilyName: String = "",
        val isCreating: Boolean = false,
        val createError: TextData? = null,
        /** Non-null shows the inline rename field, holding the in-progress value. */
        val editingName: String? = null,
        val isRenaming: Boolean = false,
        val renameError: TextData? = null,
        val inviteEmail: String = "",
        val isInviting: Boolean = false,
        val inviteError: TextData? = null,
        /** The member awaiting remove confirmation; non-null shows the confirm dialog. */
        val removingMember: FamilyMember? = null,
        /** Non-null shows the leave/delete confirmation dialog. */
        val pendingFamilyAction: FamilyAction? = null,
        /** True while the leave or delete is in flight. */
        val isRemovingFamily: Boolean = false,
        /** Set when the leave or delete failed, e.g. offline. */
        val familyActionError: TextData? = null,
    ) {
        val canCreate: Boolean
            get() = newFamilyName.isNotBlank() && !isCreating

        val canInvite: Boolean
            get() = inviteEmail.isNotBlank() && !isInviting

        val canConfirmRename: Boolean
            get() = editingName?.isNotBlank() == true && !isRenaming
    }

    sealed class Output {
        /** Pop back to the More tab. */
        data object Back : Output()

        /** Signed-out user tapped Sign In on the prompt — open the auth flow. */
        data object OpenSignIn : Output()

        /** Signed-out user tapped Sign Up on the prompt — open the auth flow. */
        data object OpenSignUp : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): FamilyBloc
    }
}
