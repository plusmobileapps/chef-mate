package com.plusmobileapps.chefmate.family.data

/**
 * A member's permission level within a family.
 *
 * Only two roles, unlike grocery lists' and recipe books' three. A family implies trust: every
 * member can edit all family-scoped content, and the distinction that matters is who administers
 * the group.
 */
enum class FamilyRole {
    /** Created the family. Can invite, remove members, rename, and delete it. */
    OWNER,
    /** Can read and edit everything shared with the family, but not administer the group. */
    MEMBER;

    /** The wire value stored in `family_members.role`. */
    val wireValue: String
        get() = name.lowercase()

    companion object {
        fun fromWire(value: String?): FamilyRole =
            entries.firstOrNull { it.wireValue == value?.lowercase() } ?: MEMBER
    }
}
