package com.plusmobileapps.chefmate.recipe.data

/**
 * In-memory hand-off for a photo picked during recipe extraction.
 *
 * The picked bytes can't ride in the serializable navigation config: a 100–300 KB `ByteArray` in
 * Android saved-instance-state risks `TransactionTooLargeException`. Instead the producer (the chat
 * photo-attach or the standalone scan flow) stashes the bytes here and the Edit Recipe view model
 * [consume]s them once when it opens.
 *
 * Survives navigation within a process (it's an app-scoped singleton). It intentionally does
 * **not** survive process death — the extracted text fields restore from the serializable nav
 * config, while the photo is silently dropped and the user re-picks it.
 */
interface PendingRecipePhotoStore {
    fun put(bytes: ByteArray, fileExtension: String)

    /** Returns the stored photo and clears the slot, or `null` if nothing is stored. */
    fun consume(): PendingPhoto?
}

data class PendingPhoto(val bytes: ByteArray, val fileExtension: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PendingPhoto) return false
        if (fileExtension != other.fileExtension) return false
        if (!bytes.contentEquals(other.bytes)) return false
        return true
    }

    override fun hashCode(): Int = 31 * bytes.contentHashCode() + fileExtension.hashCode()
}
