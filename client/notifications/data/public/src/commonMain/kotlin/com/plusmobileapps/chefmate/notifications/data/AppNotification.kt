package com.plusmobileapps.chefmate.notifications.data

import com.plusmobileapps.chefmate.grocery.data.ListRole
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRole

/**
 * An item shown in the in-app Notifications section. Today the only kind is a pending collaboration
 * invite (to a grocery list or a recipe book) awaiting the current user's Accept/Decline. New kinds
 * can be added here as the feature grows.
 *
 * [key] is a stable, type-qualified identifier safe to use as a list key and to track in-flight
 * actions — the underlying member ids are unique per table but could otherwise collide across
 * kinds.
 */
sealed interface AppNotification {
    val key: String

    /** A pending invite to collaborate on the grocery list named [listName]. */
    data class GroceryInvite(val memberId: String, val listName: String, val role: ListRole) :
        AppNotification {
        override val key: String
            get() = "grocery:$memberId"
    }

    /** A pending invite to collaborate on the recipe book named [bookName]. */
    data class RecipeBookInvite(
        val memberId: String,
        val bookName: String,
        val role: RecipeBookRole,
    ) : AppNotification {
        override val key: String
            get() = "recipe_book:$memberId"
    }
}
