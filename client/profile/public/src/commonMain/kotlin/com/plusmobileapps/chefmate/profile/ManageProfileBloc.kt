package com.plusmobileapps.chefmate.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.ComposeScreen
import com.plusmobileapps.chefmate.util.PickedImage
import kotlinx.coroutines.flow.StateFlow

interface ManageProfileBloc : ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        ManageProfileScreen(bloc = this, modifier = modifier)
    }

    fun onBack()

    fun onDisplayNameChanged(displayName: String)

    fun onHandleChanged(handle: String)

    fun onBioChanged(bio: String)

    fun onPhotoPicked(image: PickedImage)

    fun onSaveClicked()

    fun onDeleteAccountClicked()

    fun onDeleteConfirmationChanged(confirmation: String)

    fun onDeleteConfirmed()

    fun onDeleteDismissed()

    data class Model(
        val displayName: String = "",
        val email: String = "",
        /**
         * The public @handle. Empty until claimed. Editable only while [isHandleClaimed] is false —
         * handles are permanent, so the field locks once the profile exists.
         */
        val handle: String = "",
        /** True once a profile exists, which makes [handle] read-only for good. */
        val isHandleClaimed: Boolean = false,
        /** Result of the debounced availability check, or null while unknown/in flight. */
        val handleStatus: HandleStatus? = null,
        /** Public bio shown on the profile. */
        val bio: String = "",
        /** Remote avatar URL, shown when no fresh photo has been picked this session. */
        val photoUrl: String? = null,
        /** Bytes of a just-picked photo, previewed before it's uploaded on save. */
        val pickedPhoto: ByteArray? = null,
        val isSaving: Boolean = false,
        val saveError: TextData? = null,
        val showDeleteDialog: Boolean = false,
        /** What the user has typed into the delete-confirmation field; must match [email]. */
        val deleteConfirmation: String = "",
        val isDeleting: Boolean = false,
        val deleteError: TextData? = null,
    ) {
        /**
         * Save is enabled once a non-blank name exists, no save/delete is in flight, and — when the
         * user is claiming a handle for the first time — that handle is known to be usable. An
         * unclaimed blank handle is fine: a profile is opt-in, so saving without one just updates
         * the account and leaves the user without a public profile.
         */
        val canSave: Boolean
            get() =
                displayName.isNotBlank() &&
                    !isSaving &&
                    !isDeleting &&
                    (isHandleClaimed || handle.isBlank() || handleStatus == HandleStatus.Available)

        /**
         * Deletion is only allowed once the typed confirmation matches the account email
         * (case-insensitive, trimmed) — the guard against accidental account deletion.
         */
        val canConfirmDelete: Boolean
            get() = email.isNotBlank() && deleteConfirmation.trim().equals(email, ignoreCase = true)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Model) return false
            if (displayName != other.displayName) return false
            if (email != other.email) return false
            if (handle != other.handle) return false
            if (isHandleClaimed != other.isHandleClaimed) return false
            if (handleStatus != other.handleStatus) return false
            if (bio != other.bio) return false
            if (photoUrl != other.photoUrl) return false
            if (!pickedPhoto.contentEqualsNullable(other.pickedPhoto)) return false
            if (isSaving != other.isSaving) return false
            if (saveError != other.saveError) return false
            if (showDeleteDialog != other.showDeleteDialog) return false
            if (deleteConfirmation != other.deleteConfirmation) return false
            if (isDeleting != other.isDeleting) return false
            if (deleteError != other.deleteError) return false
            return true
        }

        override fun hashCode(): Int {
            var result = displayName.hashCode()
            result = 31 * result + email.hashCode()
            result = 31 * result + handle.hashCode()
            result = 31 * result + isHandleClaimed.hashCode()
            result = 31 * result + (handleStatus?.hashCode() ?: 0)
            result = 31 * result + bio.hashCode()
            result = 31 * result + (photoUrl?.hashCode() ?: 0)
            result = 31 * result + (pickedPhoto?.contentHashCode() ?: 0)
            result = 31 * result + isSaving.hashCode()
            result = 31 * result + (saveError?.hashCode() ?: 0)
            result = 31 * result + showDeleteDialog.hashCode()
            result = 31 * result + deleteConfirmation.hashCode()
            result = 31 * result + isDeleting.hashCode()
            result = 31 * result + (deleteError?.hashCode() ?: 0)
            return result
        }

        private fun ByteArray?.contentEqualsNullable(other: ByteArray?): Boolean =
            when {
                this == null && other == null -> true
                this == null || other == null -> false
                else -> this.contentEquals(other)
            }
    }

    /** Outcome of checking a typed handle, driving the inline hint under the field. */
    enum class HandleStatus {
        Checking,
        Available,

        /** Someone already has it, or it's on the server's reserved list. */
        Taken,

        /** Wrong length, or characters outside `a-z`, `0-9`, `_`. */
        InvalidFormat,
    }

    sealed class Output {
        /** Pop back to the More tab (also emitted after a successful save). */
        data object Back : Output()

        /** The account was deleted; the host pops and the More tab re-renders unauthenticated. */
        data object AccountDeleted : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): ManageProfileBloc
    }
}
