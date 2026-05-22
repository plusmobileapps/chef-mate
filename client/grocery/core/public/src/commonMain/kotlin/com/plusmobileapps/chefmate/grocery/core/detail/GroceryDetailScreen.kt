@file:OptIn(ExperimentalMaterial3Api::class)

package com.plusmobileapps.chefmate.grocery.core.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import chefmate.client.grocery.core.public.generated.resources.Res
import chefmate.client.grocery.core.public.generated.resources.grocery_detail
import chefmate.client.grocery.core.public.generated.resources.grocery_detail_aisle_label
import chefmate.client.grocery.core.public.generated.resources.grocery_detail_name_label
import chefmate.client.grocery.core.public.generated.resources.purchased
import chefmate.client.ui.public.generated.resources.Res as CommonRes
import chefmate.client.ui.public.generated.resources.save
import com.plusmobileapps.chefmate.grocery.core.displayName
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

object GroceryDetailTestTags {
    const val SHEET = "grocery_detail_sheet"
    const val AISLE_DROPDOWN = "grocery_detail_aisle_dropdown"
}

@Composable
fun GroceryDetailScreen(bloc: GroceryDetailBloc) {
    val state = bloc.models.collectAsState()

    PlusHeaderContainer(
        modifier = Modifier.fillMaxSize(),
        data =
            PlusHeaderData.Child(
                title = Res.string.grocery_detail.asTextData(),
                onBackClick = bloc::onBackClicked,
            ),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingNormal),
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = bloc::onSaveClicked) {
                Text(stringResource(CommonRes.string.save))
            }
        },
    ) {
        when (val model = state.value) {
            is GroceryDetailBloc.Model.Loading -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    PlusLoadingIndicator()
                }
            }
            is GroceryDetailBloc.Model.Loaded -> GroceryDetailBody(model.item, bloc)
        }
    }
}

@Composable
fun ColumnScope.GroceryDetailBody(item: GroceryItem, bloc: GroceryDetailBloc) {
    GroceryDetailFields(item = item, bloc = bloc)
}

/**
 * Sheet-shaped detail body: the same editable fields rendered inside a [ModalBottomSheet], plus a
 * Save button that doubles as dismiss. Snapshot tests target this composable directly because the
 * Compose screenshot test plugin can't render [ModalBottomSheet] reliably.
 */
@Composable
fun GroceryDetailSheetContent(bloc: GroceryDetailBloc, modifier: Modifier = Modifier) {
    val state by bloc.models.collectAsState()
    val dimens = ChefMateTheme.dimens

    Column(
        modifier =
            modifier
                .testTag(GroceryDetailTestTags.SHEET)
                .fillMaxWidth()
                .padding(horizontal = dimens.paddingNormal)
                .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(dimens.paddingNormal),
    ) {
        when (val model = state) {
            is GroceryDetailBloc.Model.Loading ->
                Box(
                    modifier = Modifier.fillMaxWidth().padding(dimens.paddingLarge),
                    contentAlignment = Alignment.Center,
                ) {
                    PlusLoadingIndicator()
                }
            is GroceryDetailBloc.Model.Loaded -> {
                GroceryDetailFields(item = model.item, bloc = bloc)
                Button(onClick = bloc::onSaveClicked, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(CommonRes.string.save))
                }
            }
        }
    }
}

@Composable
private fun GroceryDetailFields(item: GroceryItem, bloc: GroceryDetailBloc) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = item.name,
        onValueChange = bloc::onGroceryNameChanged,
        label = { Text(stringResource(Res.string.grocery_detail_name_label)) },
        singleLine = true,
    )
    AisleDropdown(selected = item.category, onSelected = bloc::onAisleChanged)
    Row(
        modifier =
            Modifier.fillMaxWidth().clickable { bloc.onGroceryCheckedChanged(!item.isChecked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = item.isChecked, onCheckedChange = bloc::onGroceryCheckedChanged)
        Text(stringResource(Res.string.purchased))
    }
}

@Composable
private fun AisleDropdown(
    selected: GroceryCategory,
    onSelected: (GroceryCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth().testTag(GroceryDetailTestTags.AISLE_DROPDOWN),
    ) {
        OutlinedTextField(
            value = selected.displayName().localized(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.grocery_detail_aisle_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier.menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        enabled = true,
                    )
                    .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            GroceryCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.displayName().localized()) },
                    onClick = {
                        onSelected(category)
                        expanded = false
                    },
                )
            }
        }
    }
}
