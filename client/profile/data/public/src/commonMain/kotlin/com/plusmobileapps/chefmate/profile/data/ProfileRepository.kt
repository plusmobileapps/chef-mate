package com.plusmobileapps.chefmate.profile.data

/**
 * Reads and writes the public `profiles` table.
 *
 * Remote-only by design: unlike recipes and groceries there is no local SQLDelight mirror, because
 * a profile is only meaningful when it can be fetched fresh (someone else's bio changes without any
 * sync event reaching this device). Callers must therefore handle failure as a normal state, the
 * way `PublicRecipeBloc.Model.Offline` already does.
 *
 * Published recipes are NOT fetched here — see `RecipeRepository.fetchPublishedRecipes`, which
 * already owns the remote-to-domain recipe mapping.
 */
interface ProfileRepository {

    /**
     * The signed-in user's own profile, or null when they haven't claimed a handle yet (the "no
     * profile" state, which is the default for every existing user). Fails when signed out or the
     * fetch errors.
     */
    suspend fun getMyProfile(): Result<SocialProfile?>

    /** Looks up a profile by its [handle], case-insensitively. Null when no such profile exists. */
    suspend fun getProfileByHandle(handle: String): Result<SocialProfile?>

    /**
     * Best-effort availability check for the claim form. A `true` here is NOT a reservation —
     * claiming is racy, so [claimHandle] can still come back with [HandleTaken].
     */
    suspend fun isHandleAvailable(handle: String): Result<Boolean>

    /**
     * Creates the signed-in user's profile with [handle], which can never be changed afterwards.
     * Fails with [HandleTaken] if someone claimed it first, or [HandleRejected] if the server
     * refused the format or it's on the reserved list.
     */
    suspend fun claimHandle(
        handle: String,
        displayName: String,
        bio: String,
        avatarUrl: String?,
    ): Result<SocialProfile>

    /** Updates the mutable fields of the signed-in user's existing profile. */
    suspend fun updateProfile(
        displayName: String,
        bio: String,
        avatarUrl: String?,
    ): Result<SocialProfile>

    /** The requested handle was claimed by someone else, most likely between check and claim. */
    class HandleTaken(handle: String) : Exception("Handle @$handle is already taken")

    /** The server refused the handle: bad format, or on the reserved list. */
    class HandleRejected(handle: String) : Exception("Handle @$handle is not allowed")
}
