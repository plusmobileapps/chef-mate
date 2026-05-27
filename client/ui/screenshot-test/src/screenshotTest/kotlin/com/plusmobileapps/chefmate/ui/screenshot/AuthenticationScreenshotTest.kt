package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.auth.ui.impl.ui.previewAuthBlocSignUp
import com.plusmobileapps.chefmate.auth.ui.impl.ui.previewAuthBlocSignUpWithDialogError
import com.plusmobileapps.chefmate.auth.ui.impl.ui.previewAuthBlocSignUpWithPasswordError
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun AuthenticationSignUpLightScreenshot() {
    ChefMateTheme { previewAuthBlocSignUp.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun AuthenticationSignUpWithPasswordErrorScreenshot() {
    ChefMateTheme { previewAuthBlocSignUpWithPasswordError.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AuthenticationSignUpWithPasswordErrorDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewAuthBlocSignUpWithPasswordError.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun AuthenticationSignUpWithDialogErrorScreenshot() {
    ChefMateTheme { previewAuthBlocSignUpWithDialogError.Content() }
}
