@file:OptIn(ExperimentalMaterial3Api::class)

package com.plusmobileapps.chefmate.grocery.core.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import chefmate.client.grocery.core.public.generated.resources.Res
import chefmate.client.grocery.core.public.generated.resources.grocery_detail
import chefmate.client.grocery.core.public.generated.resources.purchased
import chefmate.client.ui.public.generated.resources.save
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource
import chefmate.client.ui.public.generated.resources.Res as CommonRes

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
            ExtendedFloatingActionButton(
                onClick = bloc::onSaveClicked,
            ) {
                Text(stringResource(CommonRes.string.save))
            }
        },
    ) {
        when (val model = state.value) {
            is GroceryDetailBloc.Model.Loading -> {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    PlusLoadingIndicator()
                }
            }
            is GroceryDetailBloc.Model.Loaded -> GroceryDetailBody(model.item, bloc)
        }
    }
}

@Composable
fun ColumnScope.GroceryDetailBody(
    item: GroceryItem,
    bloc: GroceryDetailBloc,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = item.name,
        onValueChange = bloc::onGroceryNameChanged,
    )
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { bloc.onGroceryCheckedChanged(!item.isChecked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = bloc::onGroceryCheckedChanged,
        )
        Text(stringResource(Res.string.purchased))
    }
}
