package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.auth.ui.otp.OtpScreen
import com.plusmobileapps.chefmate.auth.ui.otp.previewOtpBlocCountdown
import com.plusmobileapps.chefmate.auth.ui.otp.previewOtpBlocError
import com.plusmobileapps.chefmate.auth.ui.otp.previewOtpBlocLoading
import com.plusmobileapps.chefmate.auth.ui.otp.previewOtpBlocPasswordless
import com.plusmobileapps.chefmate.auth.ui.otp.previewOtpBlocSignUp
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OtpSignUpLightScreenshot() {
    ChefMateTheme { OtpScreen(bloc = previewOtpBlocSignUp) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OtpSignUpDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { OtpScreen(bloc = previewOtpBlocSignUp) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OtpPasswordlessLightScreenshot() {
    ChefMateTheme { OtpScreen(bloc = previewOtpBlocPasswordless) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OtpPasswordlessDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { OtpScreen(bloc = previewOtpBlocPasswordless) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OtpLoadingScreenshot() {
    ChefMateTheme { OtpScreen(bloc = previewOtpBlocLoading) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OtpErrorScreenshot() {
    ChefMateTheme { OtpScreen(bloc = previewOtpBlocError) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OtpResendCountdownScreenshot() {
    ChefMateTheme { OtpScreen(bloc = previewOtpBlocCountdown) }
}
