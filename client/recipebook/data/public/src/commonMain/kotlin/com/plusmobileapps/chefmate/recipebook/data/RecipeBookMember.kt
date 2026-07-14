package com.plusmobileapps.chefmate.recipebook.data

/** Invite lifecycle for a [RecipeBookMember]: awaiting a response, accepted, or turned down. */
enum class RecipeBookMemberStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
}

/**
 * A collaborator on a recipe book. Covers pending email invites, accepted members, and invites the
 * recipient declined (kept so the owner can see the outcome). [id] is the remote member-row id;
 * null for the synthesized owner entry.
 */
data class RecipeBookMember(
    val id: String?,
    val email: String,
    val role: RecipeBookRole,
    val status: RecipeBookMemberStatus,
    /** Display name from the user's profile; null for pending invites (no account yet). */
    val name: String? = null,
    /** True for the synthesized entry representing the book owner. */
    val isOwner: Boolean = false,
    /** Profile photo URL when known; null entries fall back to a lettered avatar. */
    val avatarUrl: String? = null,
) {
    /** Convenience for the common "membership is live" check. */
    val accepted: Boolean
        get() = status == RecipeBookMemberStatus.ACCEPTED
}

/** A pending invite addressed to the current user, surfaced as the recipe-list banner. */
data class RecipeBookInvite(val memberId: String, val bookName: String, val role: RecipeBookRole)
