package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.auth.ui.impl.otp.ui.previewOtpBlocCountdown
import com.plusmobileapps.chefmate.auth.ui.impl.otp.ui.previewOtpBlocError
import com.plusmobileapps.chefmate.auth.ui.impl.otp.ui.previewOtpBlocLoading
import com.plusmobileapps.chefmate.auth.ui.impl.otp.ui.previewOtpBlocPasswordless
import com.plusmobileapps.chefmate.auth.ui.impl.otp.ui.previewOtpBlocSignUp
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OtpSignUpLightScreenshot() {
    ChefMateTheme { previewOtpBlocSignUp.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OtpSignUpDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewOtpBlocSignUp.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OtpPasswordlessLightScreenshot() {
    ChefMateTheme { previewOtpBlocPasswordless.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OtpPasswordlessDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewOtpBlocPasswordless.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OtpLoadingScreenshot() {
    ChefMateTheme { previewOtpBlocLoading.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OtpErrorScreenshot() {
    ChefMateTheme { previewOtpBlocError.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OtpResendCountdownScreenshot() {
    ChefMateTheme { previewOtpBlocCountdown.Content() }
}
