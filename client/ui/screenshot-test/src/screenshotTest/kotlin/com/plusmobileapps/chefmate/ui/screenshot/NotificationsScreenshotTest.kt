package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.notifications.impl.ui.previewNotificationsBloc
import com.plusmobileapps.chefmate.notifications.impl.ui.previewNotificationsEmptyBloc
import com.plusmobileapps.chefmate.notifications.impl.ui.previewNotificationsProcessingBloc
import com.plusmobileapps.chefmate.notifications.impl.ui.previewNotificationsSignedOutBloc
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 800)
@Composable
fun NotificationsLightScreenshot() {
    ChefMateTheme { previewNotificationsBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificationsDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewNotificationsBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 800)
@Composable
fun NotificationsProcessingScreenshot() {
    ChefMateTheme { previewNotificationsProcessingBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 800)
@Composable
fun NotificationsEmptyScreenshot() {
    ChefMateTheme { previewNotificationsEmptyBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 800)
@Composable
fun NotificationsSignedOutScreenshot() {
    ChefMateTheme { previewNotificationsSignedOutBloc.Content() }
}
