package com.plusmobileapps.chefmate.grocery.core.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chefmate.client.grocery.core.public.generated.resources.Res
import chefmate.client.grocery.core.public.generated.resources.grocery_checked
import chefmate.client.grocery.core.public.generated.resources.grocery_not_checked
import chefmate.client.grocery.core.public.generated.resources.grocery_recipe_source
import com.plusmobileapps.chefmate.grocery.core.displayName
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import org.jetbrains.compose.resources.stringResource

data class GroceryDisplayItem(
    val key: Any,
    val displayName: String,
    val quantity: String? = null,
    val isChecked: Boolean = false,
    val recipeName: String? = null,
)

data class GroceryDisplayGroup(val category: GroceryCategory, val items: List<GroceryDisplayItem>)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroceryGroupedList(
    groups: List<GroceryDisplayGroup>,
    onItemClick: (Any) -> Unit,
    onCheckedChange: (Any) -> Unit,
    modifier: Modifier = Modifier,
    showHeaders: Boolean = true,
    trailingContent: (@Composable (GroceryDisplayItem) -> Unit)? = null,
    footer: (LazyListScope.() -> Unit)? = null,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        groups.forEach { group ->
            if (showHeaders) {
                stickyHeader(key = "header_${group.category.name}") {
                    CategoryHeader(category = group.category)
                }
            }
            items(group.items.size, key = { group.items[it].key }) { index ->
                val item = group.items[index]
                GroceryDisplayListItem(
                    item = item,
                    onCheckedChange = { onCheckedChange(item.key) },
                    onClick = { onItemClick(item.key) },
                    trailingContent = trailingContent,
                    modifier = Modifier.animateItem(),
                )
                HorizontalDivider()
            }
        }
        footer?.invoke(this)
    }
}

@Composable
internal fun CategoryHeader(category: GroceryCategory, modifier: Modifier = Modifier) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier.fillMaxWidth()) {
        Text(
            text = category.displayName().localized(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun GroceryDisplayListItem(
    item: GroceryDisplayItem,
    onCheckedChange: () -> Unit,
    onClick: () -> Unit,
    trailingContent: (@Composable (GroceryDisplayItem) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCheckedChange) {
            if (item.isChecked) {
                Icon(
                    Icons.Default.CheckBox,
                    contentDescription = stringResource(Res.string.grocery_checked),
                )
            } else {
                Icon(
                    Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = stringResource(Res.string.grocery_not_checked),
                )
            }
        }
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text(text = item.displayName, style = MaterialTheme.typography.bodyLarge)
            val quantity = item.quantity
            if (quantity != null) {
                Text(
                    text = quantity,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val recipeName = item.recipeName
            if (recipeName != null) {
                Text(
                    text = stringResource(Res.string.grocery_recipe_source, recipeName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
        if (trailingContent != null) {
            trailingContent(item)
        }
    }
}
