package com.plusmobileapps.chefmate.text

import org.jetbrains.compose.resources.StringResource

/**
 * Represents a generic text to be evaluated with a [Context] in the UI layer.
 */
sealed class TextData

/**
 * A text data model that represents a fixed string.
 */
data class FixedString(
    val value: String,
) : TextData()

/**
 * A text data model that represents a string resource that does not require any
 * formatting or placeholders.
 */
data class ResourceString(
    val resource: StringResource,
) : TextData()

/**
 * A text data model that represents a phrase with placeholders.
 *
 * Usage example:
 *
 * <string name="greeting">Hello {name}, welcome to {place}!</string>
 *
 * ```kotlin
 * PhraseModel(
 *     resource = Res.string.greeting,
 *     "name" to FixedString("Alice"),
 *     "place" to ResourceString(Res.string.wonderland)
 * )
 * ```
 */
data class PhraseModel(
    val resource: StringResource,
    val args: Map<String, TextData> = emptyMap(),
) : TextData() {
    constructor(
        resource: StringResource,
        vararg args: Pair<String, TextData>,
    ) : this(
        resource = resource,
        args = args.toMap(),
    )
}
