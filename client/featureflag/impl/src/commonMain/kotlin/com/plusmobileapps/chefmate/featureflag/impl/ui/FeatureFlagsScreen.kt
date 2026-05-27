package com.plusmobileapps.chefmate.featureflag.impl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import chefmate.client.featureflag.public.generated.resources.Res
import chefmate.client.featureflag.public.generated.resources.feature_flags_clear_all
import chefmate.client.featureflag.public.generated.resources.feature_flags_default
import chefmate.client.featureflag.public.generated.resources.feature_flags_default_value
import chefmate.client.featureflag.public.generated.resources.feature_flags_empty
import chefmate.client.featureflag.public.generated.resources.feature_flags_off
import chefmate.client.featureflag.public.generated.resources.feature_flags_on
import chefmate.client.featureflag.public.generated.resources.feature_flags_reset
import chefmate.client.featureflag.public.generated.resources.feature_flags_resolved
import chefmate.client.featureflag.public.generated.resources.feature_flags_save
import chefmate.client.featureflag.public.generated.resources.feature_flags_string_placeholder
import chefmate.client.featureflag.public.generated.resources.feature_flags_title
import com.plusmobileapps.chefmate.featureflag.BooleanFlag
import com.plusmobileapps.chefmate.featureflag.FeatureFlagsBloc
import com.plusmobileapps.chefmate.featureflag.Override
import com.plusmobileapps.chefmate.featureflag.StringFlag
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
fun FeatureFlagsScreen(bloc: FeatureFlagsBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

    PlusHeaderContainer(
        modifier = modifier,
        data =
            PlusHeaderData.Child(
                title = Res.string.feature_flags_title.asTextData(),
                onBackClick = bloc::onBack,
                trailingAccessory =
                    PlusHeaderData.TrailingAccessory.Button(
                        text = Res.string.feature_flags_clear_all.asTextData(),
                        onClick = bloc::onClearAllOverrides,
                    ),
            ),
        content = {
            if (state.rows.isEmpty()) {
                Text(
                    text = Res.string.feature_flags_empty.asTextData().localized(),
                    style = ChefMateTheme.typography.bodyMedium,
                    modifier = Modifier.padding(ChefMateTheme.dimens.paddingNormal),
                )
            } else {
                state.rows.forEach { row ->
                    FlagRow(row = row, bloc = bloc)
                    HorizontalDivider()
                }
            }
        },
    )
}

@Composable
private fun FlagRow(row: FeatureFlagsBloc.Row, bloc: FeatureFlagsBloc) {
    Card(modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingSmall)) {
        Column(modifier = Modifier.padding(ChefMateTheme.dimens.paddingNormal)) {
            Text(text = row.flag.key, style = ChefMateTheme.typography.titleMedium)
            Text(
                text = row.flag.description,
                style = ChefMateTheme.typography.bodySmall,
                color = ChefMateTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text =
                    PhraseModel(
                            resource = Res.string.feature_flags_resolved,
                            "value" to FixedString(row.resolvedValue.toString()),
                        )
                        .localized(),
                style = ChefMateTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = ChefMateTheme.dimens.paddingSmall),
            )
            Text(
                text =
                    PhraseModel(
                            resource = Res.string.feature_flags_default_value,
                            "value" to FixedString(row.flag.defaultValue.toString()),
                        )
                        .localized(),
                style = ChefMateTheme.typography.bodySmall,
                color = ChefMateTheme.colorScheme.onSurfaceVariant,
            )

            when (val flag = row.flag) {
                is BooleanFlag -> {
                    @Suppress("UNCHECKED_CAST")
                    BooleanFlagControls(
                        flag = flag,
                        override = row.override as Override<Boolean>,
                        onSetOverride = bloc::onSetBooleanOverride,
                    )
                }
                is StringFlag -> {
                    @Suppress("UNCHECKED_CAST")
                    StringFlagControls(
                        flag = flag,
                        override = row.override as Override<String>,
                        onSave = bloc::onSetStringOverride,
                        onReset = { bloc.onClearOverride(flag) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BooleanFlagControls(
    flag: BooleanFlag,
    override: Override<Boolean>,
    onSetOverride: (BooleanFlag, Override<Boolean>) -> Unit,
) {
    val selectedIndex =
        when (override) {
            Override.Default -> 0
            is Override.ForceValue -> if (override.value) 1 else 2
        }
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth().padding(top = ChefMateTheme.dimens.paddingSmall)
    ) {
        SegmentedButton(
            selected = selectedIndex == 0,
            onClick = { onSetOverride(flag, Override.Default) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
        ) {
            Text(Res.string.feature_flags_default.asTextData().localized())
        }
        SegmentedButton(
            selected = selectedIndex == 1,
            onClick = { onSetOverride(flag, Override.ForceValue(true)) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
        ) {
            Text(Res.string.feature_flags_on.asTextData().localized())
        }
        SegmentedButton(
            selected = selectedIndex == 2,
            onClick = { onSetOverride(flag, Override.ForceValue(false)) },
            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
        ) {
            Text(Res.string.feature_flags_off.asTextData().localized())
        }
    }
}

@Composable
private fun StringFlagControls(
    flag: StringFlag,
    override: Override<String>,
    onSave: (StringFlag, String) -> Unit,
    onReset: () -> Unit,
) {
    val initialText =
        when (override) {
            Override.Default -> ""
            is Override.ForceValue -> override.value
        }
    var input by rememberSaveable(flag.key) { mutableStateOf(initialText) }
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        placeholder = {
            Text(Res.string.feature_flags_string_placeholder.asTextData().localized())
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(top = ChefMateTheme.dimens.paddingSmall),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onReset) {
            Text(Res.string.feature_flags_reset.asTextData().localized())
        }
        TextButton(onClick = { onSave(flag, input) }) {
            Text(Res.string.feature_flags_save.asTextData().localized())
        }
    }
}
