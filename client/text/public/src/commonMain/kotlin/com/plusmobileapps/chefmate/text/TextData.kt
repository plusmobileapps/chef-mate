package com.plusmobileapps.chefmate.text

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Represents a generic text to be evaluated with a [Context] in the UI layer.
 */
sealed class TextData {
    @Composable
    abstract fun localized(): String
}

/**
 * A text data model that represents a fixed string.
 */
data class FixedString(
    val value: String,
) : TextData() {
    @Composable
    override fun localized(): String = value
}

/**
 * A text data model that represents a string resource that does not require any
 * formatting or placeholders.
 */
data class ResourceString(
    val resource: StringResource,
) : TextData() {
    @Composable
    override fun localized(): String = stringResource(resource)
}

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

    @Composable
    override fun localized(): String {
        val string = stringResource(this.resource)
        if (args.isEmpty()) {
            return string
        }

        var resultString = string
        args.forEach { (key, textData) ->
            val placeholder = "{$key}"
            val value = textData.localized()
            resultString = resultString.replace(placeholder, value)
        }

        return resultString
    }
}
