# Google Sign-In Setup

This guide walks through the one-time external setup required for "Continue with Google" to
actually work on each platform. The code in this PR is wired up end-to-end — but none of it will
function until the OAuth clients exist and the Supabase project knows about them.

## 1. Google Cloud Console — create four OAuth client IDs

In a single project at <https://console.cloud.google.com/apis/credentials>:

1. **Web application** — server-side client ID used by Supabase to verify ID tokens, AND used by
   Android Credential Manager as `serverClientId`.
   - Authorized redirect URI: `https://<your-project>.supabase.co/auth/v1/callback`
2. **Android** — package name `com.plusmobileapps.chefmate`, plus the **debug** and **release**
   SHA-1 fingerprints (get them from `./gradlew :client:composeApp:signingReport`).
3. **iOS** — bundle ID `com.plusmobileapps.chefmate.ChefMate`. After creation, Google shows a
   reverse-client URL scheme — you'll add this to the iOS Info.plist (step 4 below).
4. **Desktop app** — used by the JVM loopback flow. No host/redirect to configure. A "secret" is
   shown — store it in `local.properties` as `google.desktopClientSecret`. Google explicitly
   documents this as not a real secret; PKCE is what protects the desktop flow.

## 2. Supabase dashboard — enable the Google provider

Authentication → Providers → Google → Enable.

- **Client ID (for OAuth)**: paste the **Web** client ID from step 1.
- **Client Secret (for OAuth)**: paste the Web client secret.
- **Authorized Client IDs**: comma-separated list of the **Android + iOS + Desktop** client IDs.
  This is what tells Supabase to accept ID tokens whose `aud` claim matches one of these — i.e.
  ID tokens minted by the native SDKs on-device.

## 3. `local.properties` — wire the keys into BuildKonfig

Add to `local.properties` at the repo root (already in `.gitignore`):

```properties
google.webClientId=<your-web-client-id>.apps.googleusercontent.com
google.desktopClientId=<your-desktop-client-id>.apps.googleusercontent.com
google.desktopClientSecret=<your-desktop-client-secret>
```

`google.webClientId` is what Android Credential Manager passes as `serverClientId`. The desktop
ones are used by the JVM loopback flow.

The iOS client ID lives in the iOS app's Info.plist (next section) — Kotlin/native code doesn't
need it.

## 4. iOS — add GoogleSignIn SDK + Info.plist entries + Bridge

The iOS app needs the GoogleSignIn-iOS SDK to actually drive the native sign-in sheet.

1. **Open `iosApp/iosApp.xcodeproj` in Xcode.**
2. **Add Swift Package**: File → Add Package Dependencies →
   `https://github.com/google/GoogleSignIn-iOS` → choose "Up to next major version" from `7.1.0`.
   Add the `GoogleSignIn` library to the `iosApp` target.
3. **Add `GoogleSignInBridge.swift` to the project**: drag `iosApp/iosApp/GoogleSignInBridge.swift`
   into Xcode's `iosApp` group (the file is already on disk from this PR).
4. **Info.plist** — add two entries:
   ```xml
   <key>GIDClientID</key>
   <string>YOUR_IOS_CLIENT_ID.apps.googleusercontent.com</string>
   <key>CFBundleURLTypes</key>
   <array>
       <!-- existing chefmate:// entry stays -->
       <dict>
           <key>CFBundleURLSchemes</key>
           <array>
               <string>com.googleusercontent.apps.YOUR_IOS_CLIENT_ID</string>
           </array>
       </dict>
   </array>
   ```
   The URL scheme is exactly Google's reverse-client-id format. Find it on the iOS OAuth client's
   page in Google Cloud Console.
5. **AppDelegate** — `iOSApp.swift` already registers the bridge:
   `IosGoogleSignInBridgeHolder.shared.bridge = GoogleSignInBridge()`. Nothing else to do.

## 5. Android — release SHA-1

For Play Store builds: the **release signing** SHA-1 must be added to the Android OAuth client.
Get it from your release keystore:
```bash
keytool -list -v -keystore release.keystore -alias <your-alias>
```

## 6. Verify

- **Android**: `./gradlew :client:composeApp:installDebug` → tap "Continue with Google" → native
  Credential Manager bottom sheet.
- **iOS**: open Xcode, run on simulator → tap "Continue with Google" → ASWebAuthenticationSession
  presents Google's consent screen.
- **Desktop**: `./gradlew :client:composeApp:run` → tap "Continue with Google" → system browser
  opens; sign in; redirect lands on `http://127.0.0.1:<random-port>/callback`; window auto-closes
  and the app proceeds.

## Architecture notes

- `GoogleSignInProvider` is an `expect class` in `client/auth/data/impl/commonMain`. The actual on
  each platform returns a `(idToken, rawNonce)` pair that `SupabaseAuthenticationRepository` hands
  to `supabase.auth.signInWith(IDToken) { provider = Google; idToken = ...; nonce = ... }`.
- The raw nonce → SHA-256 → ID token's `nonce` claim chain is what stops replay attacks. Each
  platform's actual is responsible for generating the raw nonce, hashing it, passing the hash to
  the SDK, and returning the raw nonce to Kotlin.
- **Android**: Credential Manager + `com.google.android.libraries.identity.googleid:googleid`.
  Activity context comes from `CurrentActivityHolder`, populated by `MyApplication`'s
  `ActivityLifecycleCallbacks`.
- **iOS**: Swift bridge implementing the Kotlin `IosGoogleSignInBridge` fun interface. CryptoKit
  generates the nonce; GoogleSignIn-iOS handles the rest.
- **Desktop**: RFC 8252 — system browser + PKCE + loopback HTTP server on a random localhost port.
  Google's docs explicitly bless this pattern for installed apps.

## See also

- [buildconfig-setup.md](buildconfig-setup.md) — for the broader BuildKonfig story.
- [Supabase Kotlin docs — signInWith(IDToken)](https://supabase.com/docs/reference/kotlin/auth-signinwithidtoken)
