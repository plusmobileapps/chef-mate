package com.plusmobileapps.chefmate.recipe.data

enum class RecipeRole {
    OWNER,
    EDITOR,
    VIEWER,
}

enum class RecipeCollaborationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
}

data class RecipeCollaborator(
    val id: Long,
    val email: String,
    val displayName: String?,
    val role: RecipeRole,
    val status: RecipeCollaborationStatus,
)
