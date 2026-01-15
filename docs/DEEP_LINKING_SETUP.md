# Deep Linking Setup for Email Verification

This guide explains how deep linking has been configured in Chef Mate to automatically sign in users when they click the email verification link.

## Overview

When a user signs up and needs to verify their email:
1. They receive an email with a verification link
2. Clicking the link opens the Chef Mate app automatically
3. The app processes the authentication tokens from the link
4. The user is automatically signed in without manual intervention

## Implementation Details

### 1. Supabase Auth Configuration

**File**: `client/auth/data/impl/src/commonMain/kotlin/com/plusmobileapps/chefmate/auth/data/impl/SupabaseModule.kt`

```kotlin
install(Auth) {
    scheme = "chefmate"
    host = "auth"
}
```

This configures Supabase to:
- Generate email verification links with the format: `chefmate://auth/callback#access_token=...`
- Automatically handle session restoration when the app receives these deep links

### 2. Android Manifest Configuration

**File**: `client/composeApp/src/androidMain/AndroidManifest.xml`

Added deep link intent filter to MainActivity:

```xml
<activity
    android:name=".MainActivity"
    android:launchMode="singleTask"
    ...>
    
    <!-- Deep link for email verification -->
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        
        <data
            android:scheme="chefmate"
            android:host="auth"
            android:pathPrefix="/callback" />
    </intent-filter>
</activity>
```

**Key attributes:**
- `android:launchMode="singleTask"`: Ensures only one instance of MainActivity exists
- `android:autoVerify="true"`: Enables Android App Links verification
- Scheme and host match the Supabase Auth configuration

### 3. Deep Link Handling in MainActivity

**File**: `client/composeApp/src/androidMain/kotlin/com/plusmobileapps/chefmate/MainActivity.kt`

The MainActivity now:
- Injects the `SupabaseClient`
- Handles deep links in both `onCreate()` (app not running) and `onNewIntent()` (app already running)
- Calls `supabaseClient.handleDeeplinks(intent)` which automatically processes the authentication tokens

```kotlin
class MainActivity : ComponentActivity() {
    private lateinit var supabaseClient: SupabaseClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appComponent = (application as MyApplication).appComponent
        supabaseClient = appComponent.supabaseClient
        
        val rootBloc = buildRootBloc(
            componentContext = defaultComponentContext(),
            applicationComponent = appComponent,
        )
        setContent {
            App(rootBloc)
        }
        
        // Handle deep link when app is launched
        supabaseClient.handleDeeplinks(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep link when app is already running
        supabaseClient.handleDeeplinks(intent)
    }
}
```

**Key points:**
- The Supabase SDK's `handleDeeplinks(intent)` method automatically extracts tokens from the intent
- No manual URL parsing needed
- No explicit session creation needed
- The method handles both deep links and regular intents safely

### 4. Application Component

**File**: `client/composeApp/src/commonMain/kotlin/com/plusmobileapps/chefmate/ApplicationComponent.kt`

Exposed `supabaseClient` from the application component so MainActivity can access it for deep link handling.

```kotlin
interface ApplicationComponent {
    val rootBlocFactory: RootBloc.Factory
    val authenticationRepository: AuthenticationRepository
    val supabaseClient: SupabaseClient
}
```

### 5. Automatic Session Detection

The `SupabaseAuthenticationRepository` has a session status listener (in its `init` block) that automatically observes session changes:

```kotlin
init {
    scope.launch {
        supabaseClient.auth.sessionStatus.collect { sessionStatus ->
            when (sessionStatus) {
                is SessionStatus.Authenticated -> {
                    val user = sessionStatus.session.user
                    user?.let {
                        _state.value = AuthState.Authenticated(it.toChefMateUser())
                    }
                }
                is SessionStatus.NotAuthenticated -> {
                    _state.value = AuthState.Unauthenticated
                }
                // ...
            }
        }
    }
}
```

When `supabaseClient.handleDeeplinks(intent)` restores the session, this listener automatically detects it and updates the auth state.

## User Flow

### Complete Email Verification Flow

1. **User Signs Up**
   - Enters email and password in sign-up form
   - App calls `signUpWithEmailAndPassword()`
   - Supabase creates account and sends verification email

2. **App Response**
   - Checks if email is confirmed (`emailConfirmedAt == null`)
   - Signs out the user
   - Sets auth state to `AwaitingEmailVerification`
   - User sees verification message on Settings screen

3. **Email Verification**
   - User opens their email
   - Clicks verification link: `chefmate://auth/callback#access_token=...&refresh_token=...&type=signup`
   - Android OS detects the deep link and opens Chef Mate app

4. **Deep Link Processing**
   - MainActivity receives the intent with the deep link URL
   - Calls `supabaseClient.handleDeeplinks(intent)`
   - Supabase SDK automatically extracts tokens and restores the session

5. **Session Restoration**
   - Session status listener detects the new session
   - Updates auth state to `Authenticated` with user info
   - Settings screen automatically hides verification message and shows user's name
   - User is fully logged in without manual sign-in! ✅

## Testing Deep Links

### On a Physical Android Device

1. **Build and install the app**
   ```bash
   ./gradlew :client:composeApp:installDebug
   ```

2. **Sign up a new user** in the app

3. **Check your email** for the verification link

4. **Click the verification link** on your phone
   - The Chef Mate app should open automatically
   - You should be signed in without entering credentials

### Using ADB (Android Debug Bridge)

You can simulate deep links for testing:

```bash
# Test the deep link
adb shell am start -a android.intent.action.VIEW \
  -d "chefmate://auth/callback#access_token=test&refresh_token=test&type=signup" \
  com.plusmobileapps.chefmate
```

**Note**: This won't actually authenticate (invalid tokens), but you can verify:
- The app opens
- The deep link is received and logged
- The `handleDeepLinkIntent` method is called

### Check Logs

View logs to see deep link processing:

```bash
adb logcat | grep "Deep link"
```

You should see logs like:
```
D/Napier: Received deep link: chefmate://auth/callback#...
D/Napier: Handling deep link: chefmate://auth/callback#...
D/Napier: Deep link received - Supabase will handle session restoration automatically
```

## Supabase Dashboard Configuration

### Adding Redirect URLs to Allowlist

1. Go to your Supabase Dashboard
2. Navigate to **Authentication → URL Configuration**
3. Add the following to **Redirect URLs**:
   ```
   chefmate://auth/callback
   ```

**Important**: Without this, Supabase will reject the redirect URL and use a default web URL instead.

### Testing Email Templates

You can customize the verification email template:

1. Go to **Authentication → Email Templates**
2. Select **Confirm signup** template
3. Verify the `{{ .ConfirmationURL }}` placeholder is present
4. The URL will automatically use your configured redirect URL

### Example Email Template

```html
<h2>Confirm your signup</h2>

<p>Follow this link to confirm your email:</p>
<p><a href="{{ .ConfirmationURL }}">Confirm your email</a></p>

<p>If you didn't sign up for Chef Mate, you can safely ignore this email.</p>
```

## Troubleshooting

### Deep Link Not Opening App

**Problem**: Clicking the verification link opens a browser instead of the app.

**Solutions**:
1. Verify the redirect URL is in Supabase allowlist
2. Check AndroidManifest.xml has the correct intent filter
3. Ensure `scheme` and `host` match between Supabase config and manifest
4. Try uninstalling and reinstalling the app (clears Android's deep link cache)

### App Opens But Not Authenticated

**Problem**: App opens but user isn't signed in.

**Solutions**:
1. Check logs for deep link URL format
2. Verify tokens are in the URL fragment (after `#`)
3. Ensure Supabase SDK version is compatible (using 3.2.6)
4. Check that session status listener is properly observing changes

### Email Contains Wrong URL

**Problem**: Email verification link doesn't use `chefmate://` scheme.

**Solutions**:
1. Verify `scheme` and `host` are set in `SupabaseModule.kt`
2. Add `chefmate://auth/callback` to Supabase redirect URL allowlist
3. Test with a new user signup (old emails may have cached URLs)

### Testing on Emulator

**Note**: Deep links work differently on emulators:
- Some emulators may not handle custom schemes properly
- Test on a physical device for accurate results
- Use ADB commands to simulate deep links on emulator

## iOS Considerations

For iOS support (when you add it), you'll need to:

1. **Add Universal Links** to your iOS app
2. **Configure Associated Domains** in Xcode
3. **Handle URL callbacks** in iOS-specific code
4. **Update Supabase redirect URLs** to support both Android and iOS

Example iOS redirect URL:
```
https://yourapp.com/auth/callback
```

And configure Universal Links to handle this domain.

## Security Considerations

### Access Token Exposure

**Risk**: Access tokens are visible in the deep link URL.

**Mitigations**:
- Tokens are short-lived (typically 1 hour)
- Refresh tokens allow secure long-term sessions
- Deep links are only processed by your app (not accessible to other apps)
- URLs aren't logged by Android system (custom scheme)

### Intent Hijacking

**Risk**: Malicious apps could try to intercept deep links.

**Mitigations**:
- Use `android:autoVerify="true"` for App Links verification
- Only your app can handle the `chefmate://` scheme after installation
- Android's package manager ensures scheme uniqueness

## Architecture Benefits

1. **Seamless UX**: Users don't need to manually sign in after verification
2. **Secure**: Tokens are handled automatically by Supabase SDK
3. **Observable**: Auth state changes are automatically reflected in UI
4. **Testable**: Deep link handling is isolated in repository layer
5. **Maintainable**: Configuration is centralized in SupabaseModule

## Key Implementation Points

1. **Supabase Configuration**: Set `scheme` and `host` in SupabaseModule to configure deep link format
2. **Android Manifest**: Add intent filter with matching scheme and host for deep link handling
3. **MainActivity**: Call `supabaseClient.handleDeeplinks(intent)` in `onCreate()` and `onNewIntent()`
4. **Session Listener**: Existing session status observer automatically detects and updates auth state
5. **Supabase Dashboard**: Add redirect URL to allowlist

## Related Files

### Auth Layer
- `client/auth/data/impl/src/commonMain/kotlin/com/plusmobileapps/chefmate/auth/data/impl/SupabaseModule.kt` - Configures scheme and host
- `client/auth/data/impl/src/commonMain/kotlin/com/plusmobileapps/chefmate/auth/data/impl/SupabaseAuthenticationRepository.kt` - Session status listener

### Android Layer
- `client/composeApp/src/androidMain/AndroidManifest.xml` - Deep link intent filter
- `client/composeApp/src/androidMain/kotlin/com/plusmobileapps/chefmate/MainActivity.kt` - Deep link handling
- `client/composeApp/src/commonMain/kotlin/com/plusmobileapps/chefmate/ApplicationComponent.kt` - Exposes SupabaseClient

## Next Steps

1. **Test on Physical Device**: Build and install the app, then test the complete flow
2. **Configure Supabase Dashboard**: Add redirect URL to allowlist
3. **Customize Email Template**: Make the verification email match your brand
4. **Monitor Logs**: Watch for any issues during testing
5. **Add Analytics** (optional): Track verification completion rates

## References

- [Supabase Auth Documentation](https://supabase.com/docs/guides/auth)
- [Native Mobile Deep Linking](https://supabase.com/docs/guides/auth/native-mobile-deep-linking)
- [Android Deep Links](https://developer.android.com/training/app-links/deep-linking)
- [Supabase Kotlin Client](https://github.com/supabase-community/supabase-kt)
