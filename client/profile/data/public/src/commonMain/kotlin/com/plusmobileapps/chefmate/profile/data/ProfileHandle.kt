package com.plusmobileapps.chefmate.profile.data

/**
 * Handle rules, shared by the claim form (for inline validation) and the repository (to avoid a
 * pointless round-trip on input that can't be valid).
 *
 * These MUST stay in step with the `profiles_handle_format` CHECK constraint in
 * `20260731_add_social_profiles.sql` — the database is the real authority, and this is only here so
 * the UI can give immediate feedback.
 */
object ProfileHandle {
    const val MIN_LENGTH: Int = 3
    const val MAX_LENGTH: Int = 30

    private val FORMAT = Regex("^[a-z0-9_]{$MIN_LENGTH,$MAX_LENGTH}$")

    /**
     * Canonicalizes user input to the stored form: trimmed, lowercased, and stripped of a leading
     * `@` that users habitually type. Not a validity check — call [isValidFormat] on the result.
     */
    fun normalize(raw: String): String = raw.trim().removePrefix("@").lowercase()

    /** True when [handle] is already normalized and matches the database's CHECK constraint. */
    fun isValidFormat(handle: String): Boolean = FORMAT.matches(handle)

    /** Why a normalized [handle] is unusable, or null when its format is fine. */
    fun formatError(handle: String): FormatError? =
        when {
            handle.length < MIN_LENGTH -> FormatError.TooShort
            handle.length > MAX_LENGTH -> FormatError.TooLong
            !isValidFormat(handle) -> FormatError.IllegalCharacters
            else -> null
        }

    enum class FormatError {
        TooShort,
        TooLong,

        /** Anything outside `a-z`, `0-9`, `_` survived normalization. */
        IllegalCharacters,
    }
}
