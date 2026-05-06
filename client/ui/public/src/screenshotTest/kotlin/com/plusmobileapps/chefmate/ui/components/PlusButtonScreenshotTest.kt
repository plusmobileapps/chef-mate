package com.plusmobileapps.chefmate.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true)
@Composable
private fun PlusButtonPrimaryPreview() {
    ChefMateTheme {
        Surface {
            PlusButton(
                text = FixedString("Primary"),
                variant = PlusButtonVariant.PRIMARY,
                modifier = Modifier.padding(16.dp),
                onClick = {},
            )
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
private fun PlusButtonSecondaryPreview() {
    ChefMateTheme {
        Surface {
            PlusButton(
                text = FixedString("Secondary"),
                variant = PlusButtonVariant.SECONDARY,
                modifier = Modifier.padding(16.dp),
                onClick = {},
            )
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
private fun PlusButtonDestructivePreview() {
    ChefMateTheme {
        Surface {
            PlusButton(
                text = FixedString("Delete"),
                variant = PlusButtonVariant.DESTRUCTIVE,
                modifier = Modifier.padding(16.dp),
                onClick = {},
            )
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
private fun PlusButtonLoadingPreview() {
    ChefMateTheme {
        Surface {
            PlusButton(
                text = FixedString("Saving..."),
                variant = PlusButtonVariant.PRIMARY,
                isLoading = true,
                modifier = Modifier.padding(16.dp),
                onClick = {},
            )
        }
    }
}

@PreviewTest
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlusButtonAllVariantsDarkPreview() {
    ChefMateTheme(darkTheme = true) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PlusButton(
                    text = FixedString("Primary"),
                    variant = PlusButtonVariant.PRIMARY,
                    onClick = {},
                )
                PlusButton(
                    text = FixedString("Secondary"),
                    variant = PlusButtonVariant.SECONDARY,
                    onClick = {},
                )
                PlusButton(
                    text = FixedString("Destructive"),
                    variant = PlusButtonVariant.DESTRUCTIVE,
                    onClick = {},
                )
            }
        }
    }
}
