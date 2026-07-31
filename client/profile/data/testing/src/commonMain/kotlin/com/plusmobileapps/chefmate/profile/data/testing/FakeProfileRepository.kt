package com.plusmobileapps.chefmate.profile.data.testing

import com.plusmobileapps.chefmate.profile.data.ProfileHandle
import com.plusmobileapps.chefmate.profile.data.ProfileRepository
import com.plusmobileapps.chefmate.profile.data.SocialProfile

/**
 * In-memory [ProfileRepository]. Handle uniqueness is enforced the same way the database does it,
 * so tests can exercise the taken/rejected paths without a server.
 */
class FakeProfileRepository : ProfileRepository {

    /** Profiles keyed by handle. Seed with [addProfile]. */
    private val profiles = mutableMapOf<String, SocialProfile>()

    /** Handles the fake should refuse outright, standing in for the reserved list. */
    val reservedHandles: MutableSet<String> = mutableSetOf("admin", "support", "chefmate")

    /** The signed-in user's id, used by [getMyProfile] and the write methods. */
    var currentUserId: String = "user-1"

    /** Set to make every method fail, for exercising error states. */
    var failure: Throwable? = null

    var claimHandleCallCount: Int = 0
        private set

    var lastClaimedHandle: String? = null
        private set

    fun addProfile(profile: SocialProfile) {
        profiles[profile.handle] = profile
    }

    fun profileFor(handle: String): SocialProfile? = profiles[handle]

    override suspend fun getMyProfile(): Result<SocialProfile?> = withFailure {
        profiles.values.firstOrNull { it.id == currentUserId }
    }

    override suspend fun getProfileByHandle(handle: String): Result<SocialProfile?> = withFailure {
        profiles[ProfileHandle.normalize(handle)]
    }

    override suspend fun isHandleAvailable(handle: String): Result<Boolean> = withFailure {
        val normalized = ProfileHandle.normalize(handle)
        ProfileHandle.isValidFormat(normalized) &&
            normalized !in reservedHandles &&
            normalized !in profiles
    }

    override suspend fun claimHandle(
        handle: String,
        displayName: String,
        bio: String,
        avatarUrl: String?,
    ): Result<SocialProfile> = withFailure {
        claimHandleCallCount++
        val normalized = ProfileHandle.normalize(handle)
        lastClaimedHandle = normalized
        if (!ProfileHandle.isValidFormat(normalized) || normalized in reservedHandles) {
            throw ProfileRepository.HandleRejected(normalized)
        }
        if (normalized in profiles) throw ProfileRepository.HandleTaken(normalized)
        SocialProfile(
                id = currentUserId,
                handle = normalized,
                displayName = displayName.trim(),
                bio = bio.trim(),
                avatarUrl = avatarUrl,
            )
            .also { profiles[normalized] = it }
    }

    override suspend fun updateProfile(
        displayName: String,
        bio: String,
        avatarUrl: String?,
    ): Result<SocialProfile> = withFailure {
        val existing =
            profiles.values.firstOrNull { it.id == currentUserId }
                ?: error("No profile to update for $currentUserId")
        existing
            .copy(
                displayName = displayName.trim(),
                bio = bio.trim(),
                // Null means "unchanged", matching the real repository.
                avatarUrl = avatarUrl ?: existing.avatarUrl,
            )
            .also { profiles[it.handle] = it }
    }

    private inline fun <T> withFailure(block: () -> T): Result<T> =
        failure?.let { Result.failure(it) } ?: runCatching(block)
}
