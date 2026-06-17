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

    fun onPhotoPicked(image: PickedImage)

    fun onSaveClicked()

    fun onDeleteAccountClicked()

    fun onDeleteConfirmed()

    fun onDeleteDismissed()

    data class Model(
        val displayName: String = "",
        val email: String = "",
        /** Remote avatar URL, shown when no fresh photo has been picked this session. */
        val photoUrl: String? = null,
        /** Bytes of a just-picked photo, previewed before it's uploaded on save. */
        val pickedPhoto: ByteArray? = null,
        val isSaving: Boolean = false,
        val saveError: TextData? = null,
        val showDeleteDialog: Boolean = false,
        val isDeleting: Boolean = false,
        val deleteError: TextData? = null,
    ) {
        /** Save is enabled once a non-blank name exists and no save/delete is in flight. */
        val canSave: Boolean
            get() = displayName.isNotBlank() && !isSaving && !isDeleting

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Model) return false
            if (displayName != other.displayName) return false
            if (email != other.email) return false
            if (photoUrl != other.photoUrl) return false
            if (!pickedPhoto.contentEqualsNullable(other.pickedPhoto)) return false
            if (isSaving != other.isSaving) return false
            if (saveError != other.saveError) return false
            if (showDeleteDialog != other.showDeleteDialog) return false
            if (isDeleting != other.isDeleting) return false
            if (deleteError != other.deleteError) return false
            return true
        }

        override fun hashCode(): Int {
            var result = displayName.hashCode()
            result = 31 * result + email.hashCode()
            result = 31 * result + (photoUrl?.hashCode() ?: 0)
            result = 31 * result + (pickedPhoto?.contentHashCode() ?: 0)
            result = 31 * result + isSaving.hashCode()
            result = 31 * result + (saveError?.hashCode() ?: 0)
            result = 31 * result + showDeleteDialog.hashCode()
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
