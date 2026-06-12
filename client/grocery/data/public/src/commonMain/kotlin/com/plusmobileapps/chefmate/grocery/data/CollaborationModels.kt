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
    val avatarUrl: String? = null,
)

/** A pending grocery-list invite addressed to the current user. */
data class GroceryListInvite(val memberId: String, val listName: String, val role: ListRole)
