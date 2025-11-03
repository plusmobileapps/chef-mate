package com.plusmobileapps.chefmate.text

import androidx.compose.runtime.Composable
import com.plusmobileapps.chefmate.text.PhraseModel
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.collections.component1
import kotlin.collections.component2

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

/**
 * A text data model that represents a plural string resource.
 *
 * Usage example:
 *
 * <plurals name="new_message">
 *  <item quantity="one">{quantity} new message from {name}</item>
 *  <item quantity="other">{quantity} new messages from {name}</item>
 * </plurals>
 *
 * ```kotlin
 * PluralPhraseModel(
 *    resource = Res.plurals.item_count,
 *    quantity = itemCount,
 *    "quantity" to FixedString(itemCount.toString())
 *    "name" to FixedString("John")
 * )
 * ```
 */
data class PluralResourceString(
    val resource: PluralStringResource,
    val quantity: Int,
    val args: Map<String, TextData> = emptyMap(),
) : TextData() {
    constructor(
        resource: PluralStringResource,
        quantity: Int,
        vararg args: Pair<String, TextData>,
    ) : this(
        resource = resource,
        quantity = quantity,
        args = args.toMap(),
    )

    @Composable
    override fun localized(): String {
        val string = pluralStringResource(this.resource, quantity)
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
