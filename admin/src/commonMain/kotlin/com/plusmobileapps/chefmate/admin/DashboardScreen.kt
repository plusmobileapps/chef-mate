package com.plusmobileapps.chefmate.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(repo: FeatureFlagAdminRepository, auth: AdminAuth) {
    val scope = rememberCoroutineScope()
    var flags by remember { mutableStateOf<List<AdminFeatureFlag>>(emptyList()) }
    var showArchived by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<FlagDraft?>(null) }
    var pendingDelete by remember { mutableStateOf<AdminFeatureFlag?>(null) }

    suspend fun reload() {
        loading = true
        error = null
        runCatching { repo.list(includeArchived = showArchived) }
            .onSuccess { flags = it }
            .onFailure { error = it.message ?: "Failed to load flags" }
        loading = false
    }

    LaunchedEffect(showArchived) { reload() }

    editing?.let { draft ->
        FlagEditorScreen(
            draft = draft,
            onCancel = { editing = null },
            onSave = { flag ->
                scope.launch {
                    val result =
                        if (draft.isNew) runCatching { repo.create(flag) }
                        else runCatching { repo.update(flag) }
                    result
                        .onSuccess {
                            editing = null
                            reload()
                        }
                        .onFailure { error = it.message ?: "Save failed" }
                }
            },
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feature flags") },
                actions = {
                    TextButton(onClick = { scope.launch { reload() } }) { Text("Refresh") }
                    TextButton(onClick = { scope.launch { auth.signOut() } }) { Text("Sign out") }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("New flag") },
                icon = {},
                onClick = { editing = FlagDraft() },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = showArchived,
                    onClick = { showArchived = !showArchived },
                    label = { Text("Show archived") },
                )
            }
            error?.let {
                Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }

            when {
                loading -> CenteredProgress()
                flags.isEmpty() ->
                    Text(
                        "No flags yet. Tap “New flag” to create one.",
                        Modifier.padding(top = 24.dp),
                    )
                else ->
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(flags, key = { it.key }) { flag ->
                            FlagRow(
                                flag = flag,
                                onEdit = { editing = FlagDraft.from(flag) },
                                onToggleArchive = {
                                    scope.launch {
                                        runCatching { repo.setArchived(flag.key, !flag.archived) }
                                            .onSuccess { reload() }
                                            .onFailure { error = it.message ?: "Update failed" }
                                    }
                                },
                                onDelete = { pendingDelete = flag },
                            )
                        }
                    }
            }
        }
    }

    pendingDelete?.let { flag ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete “${flag.key}”?") },
            text = { Text("This permanently removes the flag row. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val key = flag.key
                        pendingDelete = null
                        scope.launch {
                            runCatching { repo.delete(key) }
                                .onSuccess { reload() }
                                .onFailure { error = it.message ?: "Delete failed" }
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FlagRow(
    flag: AdminFeatureFlag,
    onEdit: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                flag.key,
                fontFamily = FontFamily.Monospace,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            )
            Text(
                buildString {
                    append(if (flag.enabled) "enabled" else "disabled")
                    append(" · ${flag.valueType}=${flag.value}")
                    append(" · ${flag.rolloutPercent}%")
                    flag.platforms
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { append(" · ${it.joinToString(",")}") }
                    if (flag.archived) append(" · ARCHIVED")
                },
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            )
            flag.description
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    Text(it, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onToggleArchive) {
                    Text(if (flag.archived) "Unarchive" else "Archive")
                }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun CenteredProgress() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}
