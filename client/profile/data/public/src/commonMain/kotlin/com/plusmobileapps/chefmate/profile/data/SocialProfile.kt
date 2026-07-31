package com.plusmobileapps.chefmate.profile.data

/**
 * A user's public identity — what a stranger sees at `https://chefmate.plusmobileapps.com/@handle`.
 *
 * Distinct from `ChefMateUser`, which is the *account* (including the email) and is only ever
 * visible to its owner. Identity is mirrored into the `profiles` table precisely because
 * `auth.users` is not publicly readable; this type carries only the fields that are safe to
 * publish, and deliberately has no email.
 *
 * A user has no profile at all until they claim a [handle] — being public is opt-in.
 */
data class SocialProfile(
    /** The owner's Supabase user id; also the `profiles` primary key. */
    val id: String,
    /** Unique, lowercase, immutable. Rendered with a leading `@`. */
    val handle: String,
    val displayName: String,
    val bio: String,
    val avatarUrl: String?,
    /** How many recipes this profile has published. Server-computed, so it survives pagination. */
    val publishedRecipeCount: Int = 0,
)
