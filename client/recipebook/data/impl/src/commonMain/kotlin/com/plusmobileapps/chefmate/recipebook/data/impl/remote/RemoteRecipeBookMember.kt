package com.plusmobileapps.chefmate.recipebook.data.impl.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Wire shape for a row in the Supabase `recipe_book_members` table. */
@Serializable
data class RemoteRecipeBookMember(
    val id: String? = null,
    @SerialName("recipe_book_id") val recipeBookId: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("invited_email") val invitedEmail: String,
    val role: String = "editor",
    val status: String = "pending",
)

/** A pending invite joined with its book's name (read via the invitee-view-book policy). */
@Serializable
data class RemoteRecipeBookInvite(
    val id: String,
    val role: String,
    val status: String,
    @SerialName("recipe_books") val book: RemoteInviteBook,
)

@Serializable data class RemoteInviteBook(val name: String)
