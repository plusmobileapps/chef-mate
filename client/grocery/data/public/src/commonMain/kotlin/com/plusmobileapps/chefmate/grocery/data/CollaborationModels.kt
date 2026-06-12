package com.plusmobileapps.chefmate.grocery.data

enum class ListRole {
    OWNER,
    EDITOR,
    VIEWER,
}

enum class CollaborationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
}

data class ListCollaborator(
    val id: Long,
    val email: String,
    val displayName: String?,
    val role: ListRole,
    val status: CollaborationStatus,
)
