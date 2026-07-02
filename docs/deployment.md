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
| `MATCH_GIT_BASIC_AUTHORIZATION` | Base64-encoded `username:personal_access_token` for read access to the certificates repo. See [Fastlane Match (iOS Certificates)](#fastlane-match-ios-certificates) for the creation/rotation walkthrough. |

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
releaseKeyStore=/absolute/path/to/your/keystore
releaseStorePassword=YOUR_STORE_PASSWORD
```

**Note:** `releaseKeyStore` must be an absolute path. Relative paths are resolved from the `client/composeApp/` module directory, not the project root.

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

### Android Setup Checklist

Follow these steps in order to go from zero to automated Play Store deployments:

1. **Generate a release keystore** (see [below](#generating-the-android-keystore)), or use an existing one.

2. **Create a `keystore.properties`** file in the project root with your keystore details (see the [signing configuration](#android) section above for the format). Use an absolute path for `releaseKeyStore`.

3. **Create your app on Google Play Console** — set up the app listing with the required store details (title, description, etc.).

4. **Upload the first AAB manually** — the Play Store API cannot create a new app, only upload to an existing one:
   ```bash
   ./gradlew :client:composeApp:bundleRelease
   ```
   Then upload `client/composeApp/build/outputs/bundle/release/composeApp-release.aab` through the Play Console.

5. **Create a Google Play service account** (see [below](#play-store-service-account)) and grant it access in the Play Console.

6. **Test Fastlane locally** to verify the service account works:
   ```bash
   export SUPPLY_JSON_KEY_DATA=$(cat /path/to/service-account-key.json)
   bundle exec fastlane android release
   ```

7. **Configure GitHub secrets** — add all required secrets listed in the [GitHub Secrets](#github-secrets) section above.

8. **Trigger a release** — commit your changes, create a tag, and push:
   ```bash
   git tag v0.1.0
   git push origin v0.1.0
   ```

9. **After first publish** — once the app is live on any track, update `release_status` in `fastlane/Fastfile` from `"draft"` to `"completed"` so future uploads go live automatically.

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

**Draft apps:** If your app has not yet been published on any track, the Play Store API requires uploads to have `release_status: "draft"`. This is configured in `fastlane/Fastfile`. Once the app is published for the first time (even to the internal track), you can change this to `"completed"` so releases go live automatically.

### Fastlane Match (iOS Certificates)

The certificates repo (`Plus-Mobile-Apps/certificates`) is configured in
`fastlane/Matchfile`. Match runs in `readonly: true` mode on CI, so the PAT
behind `MATCH_GIT_BASIC_AUTHORIZATION` only needs read access.

#### First-time setup

1. Create a **private** git repository under the org to store encrypted
   certificates (currently `Plus-Mobile-Apps/certificates`). If you move it,
   update `fastlane/Matchfile`.
2. Run match locally to generate and store certificates:

   ```bash
   bundle exec fastlane ios certificates
   ```

   This read-write lane authenticates with the App Store Connect API key (set
   the `APP_STORE_CONNECT_API_*` env vars, same as the release lane), registers
   the watchOS companion's App ID via `produce` if it doesn't exist yet (it
   ships inside the parent app, so no App Store Connect record is created), and
   then provisions every app id in `fastlane/Matchfile` — the app, the share
   extension, and the watchOS app (`com.plusmobileapps.chefmate.ChefMate.watchkitapp`).
   Re-run it after adding a new app id so its App Store profile is registered
   before the next release; the release lane / CI fetches profiles in `readonly`
   mode and will fail if one is missing.

   > Don't run `fastlane match appstore` directly — on its own it doesn't build
   > the API-key Hash and fails with `'api_key' value must be a Hash`. Use the
   > `certificates` lane, which sets it up.

3. Set the `MATCH_PASSWORD` secret to the encryption password you chose.

#### Creating / rotating `MATCH_GIT_BASIC_AUTHORIZATION`

`MATCH_GIT_BASIC_AUTHORIZATION` is **not** the raw PAT — it's a base64-encoded
`username:token` string that Fastlane passes as an HTTP Basic Auth header to
clone the cert repo. Rotate it whenever the underlying PAT expires.

1. Open <https://github.com/settings/personal-access-tokens> →
   **Generate new token** (fine-grained) with:
   - **Token name**: `chef-mate match certificates (read)`
   - **Resource owner**: **`Plus-Mobile-Apps`** (the org owns the cert repo;
     a personally-owned token will hit `403 Write access to repository not
     granted` even on a clone).
   - **Expiration**: pick a date and calendar it.
   - **Repository access**: **Only select repositories** → `Plus-Mobile-Apps/certificates`.
   - **Repository permissions** → **Contents: Read-only**, Metadata: Read-only.
   Approve via org settings if pending.
2. Base64-encode `username:token`. Use `printf`, not `echo` — a trailing
   newline silently breaks Basic Auth:

   ```bash
   printf 'Plus-Mobile-Apps:<PAT>' | base64
   ```

   The username field is essentially decorative for fine-grained PATs; what
   matters is that the token itself is valid.
3. Paste the entire base64 string into the `MATCH_GIT_BASIC_AUTHORIZATION`
   secret at
   <https://github.com/Plus-Mobile-Apps/chef-mate/settings/secrets/actions>.

If the `match` step fails with `Error cloning certificates git repo` or
`The requested URL returned error: 403`, the PAT has expired, been revoked,
or was issued under the wrong Resource owner. Regenerate and re-encode.

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

Versions are defined in two files across three platforms:

| Platform | File | Fields |
|----------|------|--------|
| Android | `client/composeApp/build.gradle.kts` | `versionCode` / `versionName` |
| iOS | `iosApp/Configuration/Config.xcconfig` | `CURRENT_PROJECT_VERSION` / `MARKETING_VERSION` |
| Desktop | `client/composeApp/build.gradle.kts` | `packageVersion` (generic + macOS) |

### Bump Script

Use `scripts/bump-version.sh` to update all version fields at once. Run it with no arguments for interactive mode:

```bash
./scripts/bump-version.sh
```

This displays the current versions and presents four options:

1. **Increment build number only** — keeps the current version, bumps the build code
2. **Bump patch** — increments the patch version (e.g. `0.1.18` → `0.1.19`) and build number
3. **Custom version** — prompts for a version (`X.Y.Z`), auto-increments build number
4. **Custom version and build number** — prompts for both values

CLI flags are also available for non-interactive use:

```bash
./scripts/bump-version.sh --build                          # build number only
./scripts/bump-version.sh --version 0.2.0                  # set version, auto-increment build
./scripts/bump-version.sh --version 1.0.0 --build-number 1 # set both
```

Run the script before tagging a new release.

## Known Limitations

- **macOS DMG is unsigned** — Apple requires notarization for DMGs distributed outside the Mac App Store. Users may see a Gatekeeper warning. Adding notarization requires `compose.desktop` signing config and `xcrun notarytool`.
- **R8/ProGuard is disabled** — Android release builds are not minified. Enabling R8 requires ProGuard rules for KMP libraries (Ktor, Supabase, kotlinx.serialization, Decompose).
- **No CI version bumping** — versions are bumped locally via `scripts/bump-version.sh` before tagging; there is no automatic version increment in CI.
