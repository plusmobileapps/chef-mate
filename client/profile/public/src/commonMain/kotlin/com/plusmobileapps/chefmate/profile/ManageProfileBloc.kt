package com.plusmobileapps.chefmate.profile

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.BlocScreen
import com.plusmobileapps.chefmate.util.PickedImage
import kotlinx.coroutines.flow.StateFlow

interface ManageProfileBloc : BlocScreen {
    val state: StateFlow<Model>

    fun onBack()

    fun onDisplayNameChanged(displayName: String)

    fun onPhotoPicked(image: PickedImage)

    fun onSaveClicked()

    data class Model(
        val displayName: String = "",
        val email: String = "",
        /** Remote avatar URL, shown when no fresh photo has been picked this session. */
        val photoUrl: String? = null,
        /** Bytes of a just-picked photo, previewed before it's uploaded on save. */
        val pickedPhoto: ByteArray? = null,
        val isSaving: Boolean = false,
        val saveError: TextData? = null,
    ) {
        /** Save is enabled once a non-blank name exists and no save is in flight. */
        val canSave: Boolean
            get() = displayName.isNotBlank() && !isSaving

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Model) return false
            if (displayName != other.displayName) return false
            if (email != other.email) return false
            if (photoUrl != other.photoUrl) return false
            if (!pickedPhoto.contentEqualsNullable(other.pickedPhoto)) return false
            if (isSaving != other.isSaving) return false
            if (saveError != other.saveError) return false
            return true
        }

        override fun hashCode(): Int {
            var result = displayName.hashCode()
            result = 31 * result + email.hashCode()
            result = 31 * result + (photoUrl?.hashCode() ?: 0)
            result = 31 * result + (pickedPhoto?.contentHashCode() ?: 0)
            result = 31 * result + isSaving.hashCode()
            result = 31 * result + (saveError?.hashCode() ?: 0)
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
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): ManageProfileBloc
    }
}
