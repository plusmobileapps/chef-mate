package com.plusmobileapps.chefmate.aichat.impl.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chefmate.client.aichat.public.generated.resources.Res
import chefmate.client.aichat.public.generated.resources.aichat_history_delete_all
import chefmate.client.aichat.public.generated.resources.aichat_history_delete_all_dialog_message
import chefmate.client.aichat.public.generated.resources.aichat_history_delete_all_dialog_title
import chefmate.client.aichat.public.generated.resources.aichat_history_delete_conversation
import chefmate.client.aichat.public.generated.resources.aichat_history_delete_dialog_message
import chefmate.client.aichat.public.generated.resources.aichat_history_delete_dialog_title
import chefmate.client.aichat.public.generated.resources.aichat_history_dialog_cancel
import chefmate.client.aichat.public.generated.resources.aichat_history_dialog_confirm
import chefmate.client.aichat.public.generated.resources.aichat_history_empty_description
import chefmate.client.aichat.public.generated.resources.aichat_history_empty_title
import chefmate.client.aichat.public.generated.resources.aichat_history_new_conversation
import chefmate.client.aichat.public.generated.resources.aichat_history_title
import chefmate.client.aichat.public.generated.resources.aichat_history_untitled
import com.plusmobileapps.chefmate.aichat.AiChatConversation
import com.plusmobileapps.chefmate.aichat.AiChatHistoryBloc
import com.plusmobileapps.chefmate.aichat.AiChatHistoryTestTags
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusDialog
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun AiChatHistoryScreen(bloc: AiChatHistoryBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()
    var pendingDeleteAll by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }

    PlusHeaderContainer(
        modifier = modifier.fillMaxSize().testTag(AiChatHistoryTestTags.SCREEN),
        data =
            PlusHeaderData.Child(
                title = Res.string.aichat_history_title.asTextData(),
                onBackClick = bloc::onBackClicked,
                trailingAccessory =
                    if (state.conversations.isEmpty()) null
                    else
                        PlusHeaderData.TrailingAccessory.Custom {
                            IconButton(
                                onClick = { pendingDeleteAll = true },
                                modifier =
                                    Modifier.testTag(AiChatHistoryTestTags.DELETE_ALL_BUTTON),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription =
                                        stringResource(Res.string.aichat_history_delete_all),
                                )
                            }
                        },
            ),
        scrollEnabled = false,
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.conversations.isEmpty()) {
                    EmptyState(
                        onNewConversationClick = bloc::onNewConversationClick,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    ConversationList(
                        conversations = state.conversations,
                        onConversationClick = bloc::onConversationClick,
                        onNewConversationClick = bloc::onNewConversationClick,
                        onDeleteClick = { id -> pendingDeleteId = id },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        },
    )

    if (pendingDeleteAll) {
        PlusDialog(
            title = Res.string.aichat_history_delete_all_dialog_title.asTextData(),
            message = Res.string.aichat_history_delete_all_dialog_message.asTextData(),
            confirmButtonText = Res.string.aichat_history_dialog_confirm.asTextData(),
            dismissButtonText = Res.string.aichat_history_dialog_cancel.asTextData(),
            onConfirmClick = {
                pendingDeleteAll = false
                bloc.onDeleteAllClick()
            },
            onDismissRequest = { pendingDeleteAll = false },
        )
    }

    pendingDeleteId?.let { id ->
        PlusDialog(
            title = Res.string.aichat_history_delete_dialog_title.asTextData(),
            message = Res.string.aichat_history_delete_dialog_message.asTextData(),
            confirmButtonText = Res.string.aichat_history_dialog_confirm.asTextData(),
            dismissButtonText = Res.string.aichat_history_dialog_cancel.asTextData(),
            onConfirmClick = {
                pendingDeleteId = null
                bloc.onDeleteConversationClick(id)
            },
            onDismissRequest = { pendingDeleteId = null },
        )
    }
}

@Composable
private fun ConversationList(
    conversations: List<AiChatConversation>,
    onConversationClick: (Long) -> Unit,
    onNewConversationClick: () -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.testTag(AiChatHistoryTestTags.LIST),
        contentPadding = PaddingValues(vertical = ChefMateTheme.dimens.paddingSmall),
    ) {
        item("new-conversation") {
            ListItem(
                modifier =
                    Modifier.fillMaxWidth()
                        .testTag(AiChatHistoryTestTags.NEW_CONVERSATION_BUTTON)
                        .clickableRow(onNewConversationClick),
                headlineContent = {
                    Text(stringResource(Res.string.aichat_history_new_conversation))
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
            )
            HorizontalDivider()
        }
        items(conversations, key = { it.id }) { conversation ->
            ConversationRow(
                conversation = conversation,
                onClick = { onConversationClick(conversation.id) },
                onDelete = { onDeleteClick(conversation.id) },
            )
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: AiChatConversation,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title =
        conversation.title?.takeIf { it.isNotBlank() }
            ?: stringResource(Res.string.aichat_history_untitled)
    ListItem(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(AiChatHistoryTestTags.CONVERSATION_ROW_PREFIX + conversation.id)
                .clickableRow(onClick),
        headlineContent = {
            Text(text = title, maxLines = 2, style = MaterialTheme.typography.bodyLarge)
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            IconButton(
                onClick = onDelete,
                modifier =
                    Modifier.testTag(
                        AiChatHistoryTestTags.DELETE_CONVERSATION_PREFIX + conversation.id
                    ),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription =
                        stringResource(Res.string.aichat_history_delete_conversation),
                )
            }
        },
    )
}

@Composable
private fun EmptyState(onNewConversationClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .padding(horizontal = ChefMateTheme.dimens.paddingExtraLarge)
                .testTag(AiChatHistoryTestTags.EMPTY_STATE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingNormal))
        Text(
            text = stringResource(Res.string.aichat_history_empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingSmall))
        Text(
            text = stringResource(Res.string.aichat_history_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingLarge))
        TextButton(
            onClick = onNewConversationClick,
            modifier = Modifier.testTag(AiChatHistoryTestTags.NEW_CONVERSATION_BUTTON),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.size(ChefMateTheme.dimens.paddingSmall))
            Text(stringResource(Res.string.aichat_history_new_conversation))
        }
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier = clickable(onClick = onClick)
