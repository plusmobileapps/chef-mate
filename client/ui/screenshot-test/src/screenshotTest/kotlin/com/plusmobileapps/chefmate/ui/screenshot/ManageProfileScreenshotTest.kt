package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.profile.impl.ui.previewManageProfileBloc
import com.plusmobileapps.chefmate.profile.impl.ui.previewManageProfileDeleteDialogBloc
import com.plusmobileapps.chefmate.profile.impl.ui.previewManageProfileSavingBloc
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 800)
@Composable
fun ManageProfileLightScreenshot() {
    ChefMateTheme { previewManageProfileBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ManageProfileDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewManageProfileBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 800)
@Composable
fun ManageProfileSavingScreenshot() {
    ChefMateTheme { previewManageProfileSavingBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 800)
@Composable
fun ManageProfileDeleteDialogScreenshot() {
    ChefMateTheme { previewManageProfileDeleteDialogBloc.Content() }
}
