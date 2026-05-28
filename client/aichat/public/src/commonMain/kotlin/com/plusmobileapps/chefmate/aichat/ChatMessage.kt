package com.plusmobileapps.chefmate.aichat

data class ChatMessage(
    val id: Long,
    val role: Role,
    val content: String,
    val isStreaming: Boolean,
) {
    enum class Role {
        USER,
        MODEL,
    }
}
