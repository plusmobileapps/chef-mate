package com.plusmobileapps.chefmate.auth.data

interface ProfilePhotoStorage {
    /**
     * Uploads the user's avatar to the `avatars` bucket and returns its public URL. Overwrites any
     * existing avatar for the user (one avatar per account).
     */
    suspend fun uploadPhoto(bytes: ByteArray, fileExtension: String): String

    /**
     * Best-effort delete of a previously uploaded avatar. URLs that don't point at the avatars
     * bucket are ignored; failures are swallowed so caller flows aren't disrupted.
     */
    suspend fun deletePhoto(publicUrl: String)
}
