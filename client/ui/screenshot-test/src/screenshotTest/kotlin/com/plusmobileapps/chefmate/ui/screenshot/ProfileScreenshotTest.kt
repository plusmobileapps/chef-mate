package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.profile.ProfileScreen
import com.plusmobileapps.chefmate.profile.impl.ui.previewNoProfileBloc
import com.plusmobileapps.chefmate.profile.impl.ui.previewOwnProfileBloc
import com.plusmobileapps.chefmate.profile.impl.ui.previewOwnProfileEmptyBloc
import com.plusmobileapps.chefmate.profile.impl.ui.previewProfileBloc
import com.plusmobileapps.chefmate.profile.impl.ui.previewProfileNotFoundBloc
import com.plusmobileapps.chefmate.profile.impl.ui.previewProfileOfflineBloc
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun ProfileScreenshot() {
    ChefMateTheme { ProfileScreen(bloc = previewProfileBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProfileDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { ProfileScreen(bloc = previewProfileBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OwnProfileScreenshot() {
    ChefMateTheme { ProfileScreen(bloc = previewOwnProfileBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 700)
@Composable
fun OwnProfileEmptyScreenshot() {
    ChefMateTheme { ProfileScreen(bloc = previewOwnProfileEmptyBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 700)
@Composable
fun NoProfileScreenshot() {
    ChefMateTheme { ProfileScreen(bloc = previewNoProfileBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 600)
@Composable
fun ProfileNotFoundScreenshot() {
    ChefMateTheme { ProfileScreen(bloc = previewProfileNotFoundBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 600)
@Composable
fun ProfileOfflineScreenshot() {
    ChefMateTheme { ProfileScreen(bloc = previewProfileOfflineBloc) }
}
