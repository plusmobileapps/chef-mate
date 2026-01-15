# Email Verification Guide for Chef Mate

## Overview

This guide explains how email verification works in Chef Mate after the recent updates to handle Supabase's email confirmation flow.

## The Problem

When a user signs up with Supabase, by default:
1. Supabase creates a session immediately, even if email confirmation is required
2. The app sees this session and considers the user authenticated
3. The user needs to verify their email, but they're already "logged in"

## The Solution Implemented

The app now properly handles email verification by:

1. **Checking email confirmation status** after sign-up
2. **Signing out unverified users** immediately after registration
3. **Notifying the UI** that email verification is required
4. **Requiring the user to verify their email** before they can sign in

### Code Changes Made

#### 1. Auth State (`AuthState.kt`)
- Added new `AwaitingEmailVerification` state containing the user's email
- This state is observable by any part of the app

#### 2. Repository Layer (`AuthenticationRepository.kt` & `SupabaseAuthenticationRepository.kt`)
- Added `SignUpResult` sealed class with two states:
  - `Success`: User is authenticated (no email verification required)
  - `AwaitingEmailVerification`: User needs to verify their email
- After sign-up, checks if `emailConfirmedAt` is null
- If null, signs out the user, sets state to `AwaitingEmailVerification`, and returns the result
- If confirmed, returns `Success`

#### 3. Auth UI Layer (`AuthenticationViewModel.kt` & `AuthenticationBloc.kt`)
- Added `EmailVerificationRequired` output with the user's email
- Handles the sign-up result and sends appropriate output to navigation

#### 4. Settings State Management (`SettingsViewModel.kt` & `SettingsBlocImpl.kt`)
- `SettingsViewModel` observes the auth state and tracks `emailAwaitingVerification`
- `SettingsBlocImpl` maps the email to a `TextData` message using `createEmailVerificationMessage()`
- Message is exposed through the `verificationMessage` field in the model

#### 5. Settings UI (`SettingsScreen.kt` & `SettingsTextData.kt`)
- Added `EmailVerificationMessage` composable to display a colored banner
- Shows the message above sign-in/sign-up buttons when in unverified state
- Added helper function `createEmailVerificationMessage()` to create the localized message
- New string resource: `email_verification_required`

#### 6. Navigation Layer (`RootBlocImpl.kt`)
- Handles the `EmailVerificationRequired` output by popping back to Settings
- Settings screen automatically displays the verification message via state observation

## Current Flow

1. User enters email and password in sign-up form
2. App calls Supabase sign-up
3. Supabase creates account and sends verification email
4. App checks if email is confirmed
5. If not confirmed:
   - App signs out the user immediately
   - Sets auth state to `AwaitingEmailVerification` with the user's email
   - Returns to Settings screen which shows verification message
   - User receives email with verification link
6. User clicks verification link in email
7. User returns to app and signs in with their credentials
8. Sign-in succeeds because email is now verified and message disappears

## Additional Options to Consider

### Option 1: Show Verification Screen (Recommended Next Step)

Create a dedicated screen that:
- Shows a message like "Please check your email to verify your account"
- Displays the email address used for registration
- Has a "Resend verification email" button
- Has a "Go back to sign in" button

**Implementation:**
```kotlin
// Add to Configuration
@Serializable
data class EmailVerification(val email: String) : Configuration()

// Update handleAuthenticationOutput
is AuthenticationBloc.Output.EmailVerificationRequired -> {
    navigation.replaceCurrent(
        Configuration.EmailVerification(output.email)
    )
}
```

### Option 2: Add Deep Linking for Email Verification

If you want the user to be automatically signed in after clicking the verification link:

**1. Configure Supabase Auth with redirect URL:**

```kotlin
// In SupabaseModule.kt
install(Auth) {
    scheme = "chefmate"  // Your app's deep link scheme
    host = "auth"
}
```

**2. Update sign-up to include redirect URL:**

```kotlin
override suspend fun signUpWithEmailAndPassword(
    email: String,
    password: String,
): Result<SignUpResult> =
    try {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            redirectUrl = "chefmate://auth/callback"  // Deep link
        }
        
        val currentUser = supabaseClient.auth.currentUserOrNull()
        val userNeedsConfirmation = currentUser?.emailConfirmedAt == null
        
        if (userNeedsConfirmation) {
            supabaseClient.auth.signOut()
            Result.success(SignUpResult.AwaitingEmailVerification)
        } else {
            Result.success(SignUpResult.Success)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
```

**3. Handle deep links in your Android app:**

Add to `AndroidManifest.xml`:
```xml
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="chefmate"
          android:host="auth"
          android:pathPrefix="/callback" />
</intent-filter>
```

**4. Handle deep link in MainActivity:**
```kotlin
override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    intent?.data?.let { uri ->
        // Supabase will automatically handle the session
        lifecycleScope.launch {
            supabaseClient.auth.handleDeeplinks(uri)
        }
    }
}
```

### Option 3: Disable Email Verification (Development Only)

If you want to disable email verification for development:

1. Go to Supabase Dashboard
2. Navigate to Authentication → Settings
3. Find "Enable email confirmations"
4. Toggle it OFF

**⚠️ Warning:** This is NOT recommended for production as it allows unverified emails to access your app.

### Option 4: Auto-Confirm Emails in Development

Set up Supabase to auto-confirm emails for specific domains (like test emails):

1. Go to Supabase Dashboard
2. Navigate to Authentication → Settings
3. Add your test domain to "Auto-confirm email domains"

## Testing the Current Implementation

1. Sign up with a new email
2. Check your email for verification link
3. App should return to sign-in screen
4. Try to sign in (should fail with unverified email error)
5. Click verification link in email
6. Try to sign in again (should succeed)

## Supabase Configuration Notes

### Email Templates

You can customize the verification email in Supabase Dashboard:
- Go to Authentication → Email Templates
- Edit the "Confirm signup" template
- Customize the message and styling

### Redirect URLs

If using deep linking, add your redirect URLs to the allowlist:
- Go to Authentication → URL Configuration
- Add your redirect URLs (e.g., `chefmate://auth/callback`)

## Next Steps

1. **Decide on user experience**: Do you want a verification screen or just return to sign-in?
2. **Implement deep linking** (optional): If you want seamless verification via email link
3. **Add "resend verification email"** feature: Allow users to request a new verification email
4. **Update error messages**: Make it clear when sign-in fails due to unverified email

## Architecture Benefits

This implementation follows clean architecture principles:

1. **Separation of Concerns**: Auth state is managed in the data layer, UI reacts to it
2. **Reusability**: Any screen can observe the auth state and show appropriate messaging
3. **Testability**: Each layer can be tested independently
4. **Single Source of Truth**: Auth state is the single source for verification status
5. **Reactive**: UI automatically updates when auth state changes

## Related Files

### Auth Layer
- `client/auth/data/public/src/commonMain/kotlin/com/plusmobileapps/chefmate/auth/data/AuthState.kt`
- `client/auth/data/public/src/commonMain/kotlin/com/plusmobileapps/chefmate/auth/data/AuthenticationRepository.kt`
- `client/auth/data/impl/src/commonMain/kotlin/com/plusmobileapps/chefmate/auth/data/impl/SupabaseAuthenticationRepository.kt`
- `client/auth/ui/impl/src/commonMain/kotlin/com/plusmobileapps/chefmate/auth/ui/impl/AuthenticationViewModel.kt`
- `client/auth/ui/public/src/commonMain/kotlin/com/plusmobileapps/chefmate/auth/ui/AuthenticationBloc.kt`

### Settings Layer
- `client/settings/impl/src/commonMain/kotlin/com/plusmobileapps/chefmate/settings/impl/SettingsViewModel.kt`
- `client/settings/impl/src/commonMain/kotlin/com/plusmobileapps/chefmate/settings/impl/SettingsBlocImpl.kt`
- `client/settings/public/src/commonMain/kotlin/com/plusmobileapps/chefmate/settings/SettingsBloc.kt`
- `client/settings/public/src/commonMain/kotlin/com/plusmobileapps/chefmate/settings/SettingsScreen.kt`
- `client/settings/public/src/commonMain/kotlin/com/plusmobileapps/chefmate/settings/SettingsTextData.kt`
- `client/settings/public/src/commonMain/composeResources/values/strings.xml`

### Navigation Layer
- `client/root/impl/src/commonMain/kotlin/com/plusmobileapps/chefmate/root/RootBlocImpl.kt`
