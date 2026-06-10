package com.plusmobileapps.chefmate.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import chefmate.client.ui.public.generated.resources.Res
import chefmate.client.ui.public.generated.resources.plus_outlined_container_collapse_a11y
import chefmate.client.ui.public.generated.resources.plus_outlined_container_expand_a11y
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

/**
 * A rounded-outline container that groups related fields into one visual unit (same outline the
 * recipe editors use). With a [title] it becomes a collapsible section: tapping the header toggles
 * the content with an animated reveal and a chevron that rotates between pointing down (collapsed)
 * and up (expanded). Without a [title] it's just a static outlined box.
 *
 * @param contentArrangement vertical spacing applied to [content] when a [title] is present.
 */
@Composable
fun PlusOutlinedContainer(
    modifier: Modifier = Modifier,
    title: String? = null,
    initiallyExpanded: Boolean = true,
    contentArrangement: Arrangement.Vertical =
        Arrangement.spacedBy(ChefMateTheme.dimens.paddingNormal),
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.medium,
                )
                .padding(ChefMateTheme.dimens.paddingNormal),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingExtraSmall),
    ) {
        if (title == null) {
            content()
        } else {
            val chevronRotation by
                animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription =
                        stringResource(
                            if (expanded) {
                                Res.string.plus_outlined_container_collapse_a11y
                            } else {
                                Res.string.plus_outlined_container_expand_a11y
                            },
                            title,
                        ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(chevronRotation),
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = contentArrangement, content = content)
            }
        }
    }
}
