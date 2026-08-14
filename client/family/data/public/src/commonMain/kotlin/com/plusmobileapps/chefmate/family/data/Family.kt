package com.plusmobileapps.chefmate.family.data

/**
 * A group of accounts that share content across grocery lists, recipe books, and the meal plan.
 *
 * A user belongs to at most one family. That's enforced by the database (a partial unique index on
 * accepted `family_members` rows), which is why the repository exposes a single nullable [Family]
 * rather than a list plus an active selection.
 */
data class Family(
    /** Device-local row id. */
    val id: Long,
    /** Server id — the value future phases stamp onto grocery lists, recipe books, and meals. */
    val remoteId: String,
    val name: String,
    /** True when the signed-in user created the family, and so may invite, rename, and delete. */
    val isOwnedByCurrentUser: Boolean,
) {
    companion object {
        val Sample =
            Family(
                id = 1L,
                remoteId = "family-1",
                name = "The Hendersons",
                isOwnedByCurrentUser = true,
            )
    }
}
