package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.family.core.ui.previewFamilyEmptyBloc
import com.plusmobileapps.chefmate.family.core.ui.previewFamilyMemberBloc
import com.plusmobileapps.chefmate.family.core.ui.previewFamilyOwnerBloc
import com.plusmobileapps.chefmate.family.core.ui.previewFamilyRenamingBloc
import com.plusmobileapps.chefmate.family.core.ui.previewFamilySignedOutBloc
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 800)
@Composable
fun FamilyOwnerLightScreenshot() {
    ChefMateTheme { previewFamilyOwnerBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FamilyOwnerDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewFamilyOwnerBloc.Content() }
}

/** A non-owner sees the member list and "Leave family", but no invite or rename controls. */
@PreviewTest
@Preview(showBackground = true, heightDp = 800)
@Composable
fun FamilyMemberScreenshot() {
    ChefMateTheme { previewFamilyMemberBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 800)
@Composable
fun FamilyEmptyScreenshot() {
    ChefMateTheme { previewFamilyEmptyBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FamilyEmptyDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewFamilyEmptyBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 800)
@Composable
fun FamilySignedOutScreenshot() {
    ChefMateTheme { previewFamilySignedOutBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 800)
@Composable
fun FamilyRenamingScreenshot() {
    ChefMateTheme { previewFamilyRenamingBloc.Content() }
}
