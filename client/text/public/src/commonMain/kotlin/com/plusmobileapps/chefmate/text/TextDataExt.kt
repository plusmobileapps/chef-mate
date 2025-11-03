package com.plusmobileapps.chefmate.text

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource

@Composable
fun TextData.localized(): String {
    return when (this) {
        is FixedString -> this.value
        is ResourceString -> stringResource(this.resource)
        is PhraseModel -> {
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
}
