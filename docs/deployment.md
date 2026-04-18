# Deployment Guide

This project uses **GitHub Actions** for CI and **Fastlane** for mobile build/upload orchestration. All three platforms (Android, iOS, Desktop) have automated release workflows triggered by git tags.

## Quick Start

To create a release for all platforms:

```bash
git tag v1.0.0
git push --tags
```

This triggers three independent workflows that run in parallel:

| Platform | Workflow | Destination |
|----------|----------|-------------|
| Android | `android-release.yml` | Play Store (internal track) |
| iOS | `ios-release.yml` | App Store Connect (TestFlight) |
| Desktop | `desktop-release.yml` | GitHub Releases (DMG, MSI, DEB) |

All workflows also support manual triggering via the "Run workflow" button in the GitHub Actions UI (`workflow_dispatch`).

---

## Prerequisites

### Fastlane

Fastlane is managed via Bundler. Install it locally with:

```bash
bundle install
```

The `Gemfile` at the project root declares the Fastlane dependency.

### GitHub Secrets

The following repository secrets must be configured in **Settings > Secrets and variables > Actions**:

#### Build Configuration

| Secret | Description |
|--------|-------------|
| `SUPABASE_URL` | Supabase project URL |
| `SUPABASE_KEY` | Supabase anon/public key |
| `BUGSNAG_API_KEY` | Bugsnag API key for crash reporting |

These are read by BuildKonfig at build time. See [buildconfig-setup.md](buildconfig-setup.md) for details.

#### Android Signing

| Secret | Description |
|--------|-------------|
| `ANDROID_KEYSTORE_BASE64` | Release keystore file, base64-encoded (see [Generating the keystore](#generating-the-android-keystore)) |
| `ANDROID_KEYSTORE_PASSWORD` | Password for the keystore |
| `ANDROID_KEY_ALIAS` | Alias of the signing key within the keystore |
| `ANDROID_KEY_PASSWORD` | Password for the signing key |

#### Play Store Upload

| Secret | Description |
|--------|-------------|
| `PLAY_STORE_SERVICE_ACCOUNT_JSON` | Google Play service account JSON key content (see [Play Store setup](#play-store-service-account)) |

#### iOS Code Signing (Fastlane Match)

| Secret | Description |
|--------|-------------|
| `MATCH_PASSWORD` | Encryption password for the match certificates repository |
| `MATCH_GIT_BASIC_AUTHORIZATION` | Base64-encoded `username:personal_access_token` for accessing the certificates repo |

#### App Store Connect

| Secret | Description |
|--------|-------------|
| `ASC_KEY_ID` | App Store Connect API Key ID |
| `ASC_ISSUER_ID` | App Store Connect API Issuer ID |
| `ASC_API_KEY` | App Store Connect API private key (`.p8` content, base64-encoded) |
| `APPLE_TEAM_ID` | Apple Developer Team ID |

---

## Platform Details

### Android

**Workflow:** `.github/workflows/android-release.yml`

**What it does:**
1. Decodes the release keystore from the `ANDROID_KEYSTORE_BASE64` secret
2. Builds a signed AAB via `./gradlew :client:composeApp:bundleRelease`
3. Uploads the AAB to the Play Store **internal** track via Fastlane `supply`

**Fastlane lane:** `bundle exec fastlane android release`

**Signing configuration:** The `signingConfigs` block in `client/composeApp/build.gradle.kts` reads credentials from environment variables first (used in CI), then falls back to a `keystore.properties` file at the project root (for local builds):

| Environment Variable | keystore.properties Key | Description |
|---------------------|------------------------|-------------|
| `ANDROID_KEYSTORE_FILE` | `releaseKeyStore` | Path to the keystore file (defaults to `release.keystore`) |
| `ANDROID_KEYSTORE_PASSWORD` | `releaseStorePassword` | Password for the keystore |
| `ANDROID_KEY_ALIAS` | `releaseKeyAlias` | Alias of the signing key |
| `ANDROID_KEY_PASSWORD` | `releaseKeyPassword` | Password for the signing key |

For local builds, create a `keystore.properties` file in the project root (this file is gitignored):

```properties
releaseKeyAlias=upload
releaseKeyPassword=YOUR_KEY_PASSWORD
releaseKeyStore=path/to/your/keystore
releaseStorePassword=YOUR_STORE_PASSWORD
```

**Promoting releases:** Builds are uploaded to the `internal` track. Promote to higher tracks (alpha, beta, production) through the [Google Play Console](https://play.google.com/console).

### iOS

**Workflow:** `.github/workflows/ios-release.yml`

**What it does:**
1. Fetches signing certificates and provisioning profiles via Fastlane `match` (readonly mode)
2. Builds the IPA via `build_app` targeting `iosApp/iosApp.xcodeproj`
3. Uploads to App Store Connect via Fastlane `deliver`

**Fastlane lane:** `bundle exec fastlane ios release`

**Code signing:** Uses [Fastlane match](https://docs.fastlane.tools/actions/match/) with a private git repository to store encrypted certificates and provisioning profiles. The repository URL is configured in `fastlane/Matchfile`.

**App Store Connect API:** Uses an API key (recommended over Apple ID for CI). The key avoids 2FA prompts and is more reliable for automation.

### Desktop

**Workflow:** `.github/workflows/desktop-release.yml`

**What it does:**
1. Builds native packages in parallel on three runners:
   - `macos-15` — builds `.dmg` via `./gradlew :client:composeApp:packageDmg`
   - `windows-latest` — builds `.msi` via `./gradlew :client:composeApp:packageMsi`
   - `ubuntu-latest` — builds `.deb` via `./gradlew :client:composeApp:packageDeb`
2. Uploads each package as a build artifact
3. Creates a GitHub Release from the tag with all three packages attached
4. Auto-generates release notes from commits since the last tag

**No Fastlane:** Desktop distribution uses Gradle's Compose Desktop packaging plugin and GitHub Actions directly, since Fastlane is mobile-only.

**Package configuration:** Native distribution settings (package name, version, vendor, platform-specific options) are in `client/composeApp/build.gradle.kts` under the `compose.desktop.application.nativeDistributions` block.

---

## First-Time Setup

### Generating the Android Keystore

If you don't already have a release keystore:

```bash
keytool -genkeypair \
  -alias release \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -keystore release.keystore \
  -storepass YOUR_STORE_PASSWORD \
  -keypass YOUR_KEY_PASSWORD \
  -dname "CN=Chef Mate, O=Plus Mobile Apps"
```

To base64-encode it for the GitHub secret:

```bash
base64 -i release.keystore | pbcopy   # macOS (copies to clipboard)
base64 -w 0 release.keystore          # Linux (prints to stdout)
```

Store the output as the `ANDROID_KEYSTORE_BASE64` secret.

### Play Store Service Account

1. Go to the [Google Cloud Console](https://console.cloud.google.com)
2. Create a service account with the "Service Account User" role
3. Generate a JSON key for the service account
4. In Google Play Console, go to **Settings > API access** and grant the service account access
5. Paste the entire JSON key content as the `PLAY_STORE_SERVICE_ACCOUNT_JSON` secret

**Important:** The first AAB must be uploaded manually through the Play Console to create the app listing. Fastlane `supply` can only upload to an existing app.

### Fastlane Match (iOS Certificates)

1. Create a **private** git repository to store encrypted certificates (e.g., `plusmobileapps/certificates`)
2. Run match locally to generate and store certificates:

```bash
bundle exec fastlane match appstore
```

3. Set the `MATCH_PASSWORD` secret to the encryption password you chose
4. Generate a personal access token with `repo` scope and base64-encode it:

```bash
echo -n "username:ghp_your_token_here" | base64
```

5. Store the result as `MATCH_GIT_BASIC_AUTHORIZATION`

### App Store Connect API Key

1. Go to [App Store Connect > Users and Access > Integrations > App Store Connect API](https://appstoreconnect.apple.com/access/integrations/api)
2. Create a new key with "App Manager" role
3. Download the `.p8` file (you can only download it once)
4. Base64-encode the key content:

```bash
base64 -i AuthKey_XXXXXXXXXX.p8 | pbcopy
```

5. Set the three secrets:
   - `ASC_KEY_ID` — the Key ID shown in App Store Connect
   - `ASC_ISSUER_ID` — the Issuer ID shown at the top of the API keys page
   - `ASC_API_KEY` — the base64-encoded `.p8` content

---

## Local Testing

You can run the Fastlane lanes locally to test the build steps (uploading will require valid credentials).

If you have a `keystore.properties` file at the project root, no environment variables are needed for Android builds:

```bash
# Android — build the AAB locally (uses keystore.properties)
./gradlew :client:composeApp:bundleRelease

# iOS — run the full lane (requires match setup and Xcode)
bundle exec fastlane ios release

# Desktop — build packages for your current OS
./gradlew :client:composeApp:packageDmg    # macOS
./gradlew :client:composeApp:packageMsi    # Windows
./gradlew :client:composeApp:packageDeb    # Linux
```

## Version Management

Versions are currently hardcoded in three places:

| Platform | File | Field |
|----------|------|-------|
| Android | `client/composeApp/build.gradle.kts` | `versionCode` / `versionName` |
| iOS | `iosApp/Configuration/Config.xcconfig` | `CURRENT_PROJECT_VERSION` / `MARKETING_VERSION` |
| Desktop | `client/composeApp/build.gradle.kts` | `packageVersion` (in `nativeDistributions`) |

Update all three before tagging a new release.

## Known Limitations

- **macOS DMG is unsigned** — Apple requires notarization for DMGs distributed outside the Mac App Store. Users may see a Gatekeeper warning. Adding notarization requires `compose.desktop` signing config and `xcrun notarytool`.
- **R8/ProGuard is disabled** — Android release builds are not minified. Enabling R8 requires ProGuard rules for KMP libraries (Ktor, Supabase, kotlinx.serialization, Decompose).
- **No automatic version bumping** — versions must be updated manually before tagging.
