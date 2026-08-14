package com.plusmobileapps.chefmate.family.data

/** Invite lifecycle for a [FamilyMember]: awaiting a response, accepted, or turned down. */
enum class FamilyMemberStatus {
    PENDING,
    ACCEPTED,
    REJECTED;

    /** The wire value stored in `family_members.status`. */
    val wireValue: String
        get() = name.lowercase()

    companion object {
        fun fromWire(value: String?): FamilyMemberStatus =
            entries.firstOrNull { it.wireValue == value?.lowercase() } ?: PENDING
    }
}

/**
 * Someone on a family: an accepted member, a pending email invite, or an invite the recipient
 * declined (kept so the owner can see the outcome).
 *
 * [id] is the remote `family_members` row id. It is null for the owner, whose entry is synthesized
 * server-side by the `family_members_with_profiles` RPC rather than read from a member row — so
 * [isOwner], not [role], is what identifies them.
 */
data class FamilyMember(
    val id: String?,
    val email: String,
    val role: FamilyRole,
    val status: FamilyMemberStatus,
    /** Display name from the user's profile; null for pending invites (no account yet). */
    val name: String? = null,
    /** True for the synthesized entry representing the family owner. */
    val isOwner: Boolean = false,
    /** Profile photo URL when known; null entries fall back to a lettered avatar. */
    val avatarUrl: String? = null,
) {
    /** Convenience for the common "membership is live" check. */
    val accepted: Boolean
        get() = status == FamilyMemberStatus.ACCEPTED

    companion object {
        val SampleOwner =
            FamilyMember(
                id = null,
                email = "jamie@example.com",
                role = FamilyRole.OWNER,
                status = FamilyMemberStatus.ACCEPTED,
                name = "Jamie Henderson",
                isOwner = true,
            )

        val SampleMember =
            FamilyMember(
                id = "member-2",
                email = "alex@example.com",
                role = FamilyRole.MEMBER,
                status = FamilyMemberStatus.ACCEPTED,
                name = "Alex Henderson",
            )

        val SamplePending =
            FamilyMember(
                id = "member-3",
                email = "sam@example.com",
                role = FamilyRole.MEMBER,
                status = FamilyMemberStatus.PENDING,
            )

        val Samples = listOf(SampleOwner, SampleMember, SamplePending)
    }
}

/** A pending family invite addressed to the current user, surfaced in Notifications. */
data class FamilyInvite(val memberId: String, val familyName: String)
