package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
fun PlusLoadingDialog(message: TextData) {
    PlusDialogScaffold(
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingNormal),
            ) {
                CircularProgressIndicator()
                Text(
                    text = message.localized(),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        },
    )
}
