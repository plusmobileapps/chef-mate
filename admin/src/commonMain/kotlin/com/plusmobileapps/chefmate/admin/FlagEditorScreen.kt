package com.plusmobileapps.chefmate.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FlagEditorScreen(draft: FlagDraft, onCancel: () -> Unit, onSave: (AdminFeatureFlag) -> Unit) {
    var state by remember { mutableStateOf(draft) }
    val validation = FlagDraftValidator.validate(state)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (draft.isNew) "New flag" else "Edit flag") },
                navigationIcon = { TextButton(onClick = onCancel) { Text("Cancel") } },
                actions = {
                    TextButton(
                        onClick = { if (validation.isValid) onSave(state.toFlag()) },
                        enabled = validation.isValid,
                    ) {
                        Text("Save")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.key,
                onValueChange = { state = state.copy(key = it) },
                label = { Text("Key") },
                singleLine = true,
                // Key is the primary key — don't allow changing it once the row exists.
                enabled = state.isNew,
                isError = validation.keyError != null,
                supportingText = { validation.keyError?.let { Text(it) } },
                textStyle =
                    MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.fillMaxWidth(),
            )

            LabeledSection("Type") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlagType.entries.forEach { type ->
                        FilterChip(
                            selected = state.type == type,
                            onClick = { state = state.copy(type = type) },
                            label = { Text(if (type == FlagType.BOOL) "Boolean" else "String") },
                        )
                    }
                }
            }

            LabeledSection("Value") {
                when (state.type) {
                    FlagType.BOOL ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = state.boolValue,
                                onCheckedChange = { state = state.copy(boolValue = it) },
                            )
                            Text(
                                if (state.boolValue) "true" else "false",
                                Modifier.padding(start = 8.dp),
                            )
                        }
                    FlagType.STRING ->
                        OutlinedTextField(
                            value = state.stringValue,
                            onValueChange = { state = state.copy(stringValue = it) },
                            label = { Text("String value") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = state.enabled,
                    onCheckedChange = { state = state.copy(enabled = it) },
                )
                Text("Enabled", Modifier.padding(start = 8.dp))
            }

            LabeledSection("Rollout: ${state.rolloutPercent}%") {
                Slider(
                    value = state.rolloutPercent.toFloat(),
                    onValueChange = { state = state.copy(rolloutPercent = it.toInt()) },
                    valueRange = 0f..100f,
                    steps = 99,
                )
            }

            LabeledSection("Platforms (none = all)") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlagDraft.ALL_PLATFORMS.forEach { platform ->
                        val selected = platform in state.platforms
                        FilterChip(
                            selected = selected,
                            onClick = {
                                state =
                                    state.copy(
                                        platforms =
                                            if (selected) state.platforms - platform
                                            else state.platforms + platform
                                    )
                            },
                            label = { Text(platform) },
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = state.minVersion,
                    onValueChange = { state = state.copy(minVersion = it) },
                    label = { Text("Min version") },
                    singleLine = true,
                    isError = validation.versionError != null,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.maxVersion,
                    onValueChange = { state = state.copy(maxVersion = it) },
                    label = { Text("Max version") },
                    singleLine = true,
                    isError = validation.versionError != null,
                    modifier = Modifier.weight(1f),
                )
            }
            validation.versionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            LabeledSection("User allowlist (optional)") {
                OutlinedTextField(
                    value = state.userIdsText,
                    onValueChange = { state = state.copy(userIdsText = it) },
                    label = { Text("Supabase user ids") },
                    supportingText = {
                        Text(
                            "One id per line (or comma-separated). These users get the flag on top of the rollout %."
                        )
                    },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            OutlinedTextField(
                value = state.description,
                onValueChange = { state = state.copy(description = it) },
                label = { Text("Description (admin notes)") },
                modifier = Modifier.fillMaxWidth(),
            )

            if (!draft.isNew) {
                Button(
                    onClick = { if (validation.isValid) onSave(state.toFlag()) },
                    enabled = validation.isValid,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save changes")
                }
            }
        }
    }
}

@Composable
private fun LabeledSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        content()
    }
}
