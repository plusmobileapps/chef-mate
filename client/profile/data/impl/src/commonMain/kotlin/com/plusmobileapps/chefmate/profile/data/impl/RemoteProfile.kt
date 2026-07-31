package com.plusmobileapps.chefmate.profile.data.impl

import com.plusmobileapps.chefmate.profile.data.SocialProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire shape of a `profiles` row. Matches both the table itself and the column list returned by the
 * `get_profile_by_handle` / `get_profile_by_id` RPCs — [publishedRecipeCount] is only populated by
 * the latter, since a plain table select has no count to give.
 */
@Serializable
internal data class RemoteProfile(
    val id: String,
    val handle: String,
    @SerialName("display_name") val displayName: String = "",
    val bio: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("published_recipe_count") val publishedRecipeCount: Long = 0,
) {
    fun toSocialProfile(): SocialProfile =
        SocialProfile(
            id = id,
            handle = handle,
            displayName = displayName,
            bio = bio,
            avatarUrl = avatarUrl,
            publishedRecipeCount = publishedRecipeCount.toInt(),
        )
}

/** Insert/update payload. Omits [RemoteProfile.publishedRecipeCount], which is server-derived. */
@Serializable
internal data class ProfileUpsert(
    val id: String? = null,
    val handle: String? = null,
    @SerialName("display_name") val displayName: String,
    val bio: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)
