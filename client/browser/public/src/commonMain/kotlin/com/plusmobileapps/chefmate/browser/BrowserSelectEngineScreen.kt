package com.plusmobileapps.chefmate.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chefmate.client.browser.public.generated.resources.Res
import chefmate.client.browser.public.generated.resources.browser_select_engine_subtitle
import chefmate.client.browser.public.generated.resources.browser_select_engine_title
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BrowserSelectEngineScreen(bloc: BrowserSelectEngineBloc, modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .testTag(BrowserTestTags.SELECT_ENGINE_SCREEN)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ChefMateTheme.dimens.paddingExtraLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingLarge))
        Icon(
            imageVector = Icons.Outlined.Restaurant,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = ChefMateTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingLarge))
        Text(
            text = stringResource(Res.string.browser_select_engine_title),
            style = ChefMateTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingSmall))
        Text(
            text = stringResource(Res.string.browser_select_engine_subtitle),
            style = ChefMateTheme.typography.bodyMedium,
            color = ChefMateTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingExtraLarge))

        SearchEngine.entries.forEach { engine ->
            EngineOption(
                engine = engine,
                onClick = { bloc.onEngineSelected(engine) },
                modifier = Modifier.padding(bottom = ChefMateTheme.dimens.paddingSmall),
            )
        }
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingLarge))
    }
}

@Composable
private fun EngineOption(engine: SearchEngine, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .testTag(BrowserTestTags.engineOption(engine)),
        shape = RoundedCornerShape(ChefMateTheme.dimens.paddingNormal),
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .height(ChefMateTheme.dimens.rowHeight)
                    .padding(horizontal = ChefMateTheme.dimens.paddingNormal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(engine.logo),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(ChefMateTheme.dimens.paddingLarge),
            )
            Spacer(Modifier.size(ChefMateTheme.dimens.paddingNormal))
            Text(text = engine.displayName, style = ChefMateTheme.typography.titleMedium)
        }
    }
}
