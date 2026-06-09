package com.plusmobileapps.chefmate.recipebook.data

/** A collaborator's permission level on a recipe book. */
enum class RecipeBookRole {
    /** Full control, including managing collaborators and deleting the book. */
    OWNER,
    /** Can add and edit recipes in the book. */
    EDITOR,
    /** Read-only access. */
    VIEWER;

    /** The wire value used by Supabase's `recipe_book_role` enum. */
    val wireValue: String
        get() = name.lowercase()

    companion object {
        fun fromWire(value: String?): RecipeBookRole =
            entries.firstOrNull { it.wireValue == value?.lowercase() } ?: EDITOR
    }
}
