package com.plusmobileapps.chefmate.admin

enum class FlagType {
    BOOL,
    STRING,
}

/**
 * Editable, UI-facing form of a feature flag. Keeps fields as raw editor inputs (strings/sets) and
 * converts to/from [AdminFeatureFlag]. Validation and conversion are pure so they can be unit
 * tested without Compose.
 */
data class FlagDraft(
    val key: String = "",
    val type: FlagType = FlagType.BOOL,
    val boolValue: Boolean = false,
    val stringValue: String = "",
    val enabled: Boolean = false,
    val rolloutPercent: Int = 100,
    val platforms: Set<String> = emptySet(),
    val minVersion: String = "",
    val maxVersion: String = "",
    // Raw editor text for the user-id allowlist: one id per line (commas also accepted). Parsed
    // into the `user_ids` array by [toFlag].
    val userIdsText: String = "",
    val description: String = "",
    val archived: Boolean = false,
    val isNew: Boolean = true,
) {
    companion object {
        // Targetable platforms. Must match the platform identifiers the client reports in
        // FeatureFlagContext (see FeatureFlagEvaluator.platformMatches).
        val ALL_PLATFORMS = listOf("android", "ios", "desktop", "web")

        // User ids are separated by commas, newlines, or surrounding whitespace.
        private val USER_ID_SPLIT = Regex("[\\s,]+")

        fun parseUserIds(text: String): List<String> =
            text.split(USER_ID_SPLIT).map { it.trim() }.filter { it.isNotEmpty() }.distinct()

        fun from(flag: AdminFeatureFlag): FlagDraft {
            val isBool = flag.valueType.equals(AdminFeatureFlag.TYPE_BOOL, ignoreCase = true)
            return FlagDraft(
                key = flag.key,
                type = if (isBool) FlagType.BOOL else FlagType.STRING,
                boolValue = flag.value.equals("true", ignoreCase = true),
                stringValue = if (isBool) "" else flag.value,
                enabled = flag.enabled,
                rolloutPercent = flag.rolloutPercent,
                platforms = flag.platforms?.toSet() ?: emptySet(),
                minVersion = flag.minVersion.orEmpty(),
                maxVersion = flag.maxVersion.orEmpty(),
                userIdsText = flag.userIds?.joinToString("\n").orEmpty(),
                description = flag.description.orEmpty(),
                archived = flag.archived,
                isNew = false,
            )
        }
    }
}

data class FlagValidation(
    val keyError: String? = null,
    val rolloutError: String? = null,
    val versionError: String? = null,
) {
    val isValid: Boolean
        get() = keyError == null && rolloutError == null && versionError == null
}

object FlagDraftValidator {
    // Flag keys are lower snake_case identifiers (matches FeatureFlagRegistry keys like
    // `cook_mode_v2`).
    private val KEY_REGEX = Regex("^[a-z0-9_]+$")
    // Dotted numeric versions, e.g. 1, 1.2, 1.2.3.
    private val VERSION_REGEX = Regex("^\\d+(\\.\\d+)*$")

    fun validate(draft: FlagDraft): FlagValidation =
        FlagValidation(
            keyError =
                when {
                    draft.key.isBlank() -> "Key is required"
                    !KEY_REGEX.matches(draft.key) ->
                        "Use lower-case letters, numbers, and underscores"
                    else -> null
                },
            rolloutError =
                if (draft.rolloutPercent in 0..100) null else "Rollout must be between 0 and 100",
            versionError =
                listOf(draft.minVersion, draft.maxVersion)
                    .firstOrNull { it.isNotBlank() && !VERSION_REGEX.matches(it.trim()) }
                    ?.let { "Versions must look like 1.2.3" },
        )
}

fun FlagDraft.toFlag(): AdminFeatureFlag =
    AdminFeatureFlag(
        key = key.trim(),
        valueType =
            if (type == FlagType.BOOL) AdminFeatureFlag.TYPE_BOOL else AdminFeatureFlag.TYPE_STRING,
        value = if (type == FlagType.BOOL) boolValue.toString() else stringValue,
        enabled = enabled,
        rolloutPercent = rolloutPercent,
        platforms = platforms.takeIf { it.isNotEmpty() }?.sorted(),
        minVersion = minVersion.trim().ifBlank { null },
        maxVersion = maxVersion.trim().ifBlank { null },
        userIds = FlagDraft.parseUserIds(userIdsText).takeIf { it.isNotEmpty() },
        archived = archived,
        description = description.trim().ifBlank { null },
    )
