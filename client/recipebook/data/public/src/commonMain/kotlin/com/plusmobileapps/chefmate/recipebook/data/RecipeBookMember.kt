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
    /** True for the synthesized entry representing the book owner. */
    val isOwner: Boolean = false,
)

/** A pending invite addressed to the current user, surfaced as the recipe-list banner. */
data class RecipeBookInvite(val memberId: String, val bookName: String, val role: RecipeBookRole)
