package com.plusmobileapps.chefmate.aichat

/**
 * Clears AI chat data stored on the device. Exposed from the aichat `public` module so sign-out /
 * sign-in flows can wipe per-user chat history without depending on the aichat `impl` module.
 */
fun interface AiChatLocalDataCleaner {
    suspend fun clearLocalData()
}
