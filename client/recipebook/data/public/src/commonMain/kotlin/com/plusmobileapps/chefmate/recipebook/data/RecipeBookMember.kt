package com.plusmobileapps.chefmate.recipebook.data

/**
 * A collaborator on a recipe book. Covers both pending email invites (no [accepted]) and accepted
 * members. [id] is the remote member-row id; null for the synthesized owner entry.
 */
data class RecipeBookMember(
    val id: String?,
    val email: String,
    val role: RecipeBookRole,
    val accepted: Boolean,
    /** Display name from the user's profile; null for pending invites (no account yet). */
    val name: String? = null,
    /** True for the synthesized entry representing the book owner. */
    val isOwner: Boolean = false,
    /** Profile photo URL when known; null entries fall back to a lettered avatar. */
    val avatarUrl: String? = null,
)

/** A pending invite addressed to the current user, surfaced as the recipe-list banner. */
data class RecipeBookInvite(val memberId: String, val bookName: String, val role: RecipeBookRole)
