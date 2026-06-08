package com.plusmobileapps.chefmate.auth.data.testing

import com.plusmobileapps.chefmate.auth.data.ProfilePhotoStorage

class FakeProfilePhotoStorage : ProfilePhotoStorage {
    var uploadResult: String = "https://example.com/avatars/test/avatar.jpg"
    var uploadError: Throwable? = null

    var lastUploadedBytes: ByteArray? = null
        private set

    var deletedUrls: MutableList<String> = mutableListOf()
        private set

    override suspend fun uploadPhoto(bytes: ByteArray, fileExtension: String): String {
        lastUploadedBytes = bytes
        uploadError?.let { throw it }
        return uploadResult
    }

    override suspend fun deletePhoto(publicUrl: String) {
        deletedUrls.add(publicUrl)
    }
}
