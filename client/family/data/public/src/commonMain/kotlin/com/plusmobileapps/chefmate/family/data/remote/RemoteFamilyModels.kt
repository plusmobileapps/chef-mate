package com.plusmobileapps.chefmate.family.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A row of the `families` table, or of the `current_family()` RPC which returns the same shape. */
@Serializable
data class RemoteFamily(
    val id: String? = null,
    val name: String,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

/** A row of the `family_members` table, used for inserts (invites) and status updates. */
@Serializable
data class RemoteFamilyMember(
    val id: String? = null,
    @SerialName("family_id") val familyId: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("invited_email") val invitedEmail: String,
    @SerialName("invited_by") val invitedBy: String? = null,
    val role: String = "member",
    val status: String = "pending",
)

/**
 * A row of the `family_members_with_profiles` RPC: every member plus the synthesized owner entry,
 * with names and avatars resolved from `auth.users` (which clients can't read directly). Same
 * 7-column shape as `grocery_list_collaborators` and `recipe_book_collaborators`.
 */
@Serializable
data class RemoteFamilyCollaborator(
    @SerialName("member_id") val memberId: String?,
    val email: String,
    val name: String?,
    val role: String,
    val status: String,
    @SerialName("is_owner") val isOwner: Boolean,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

/** A row of the `family_pending_invites` RPC: invites addressed to the caller. */
@Serializable
data class RemoteFamilyInvite(
    @SerialName("member_id") val memberId: String,
    @SerialName("family_id") val familyId: String,
    @SerialName("family_name") val familyName: String,
    val role: String,
    val status: String,
)
