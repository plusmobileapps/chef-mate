# Deployment Guide

This project uses **GitHub Actions** for CI and **Fastlane** for mobile build/upload orchestration. All three platforms (Android, iOS, Desktop) have automated release workflows triggered by git tags.

## Quick Start

The recommended path is the automated **Bump Version** workflow (see [Version Management](#version-management)) — it bumps versions, opens a PR, and tags the release on merge. To release manually, push a tag directly:

```bash
git tag v1.0.0
git push --tags
```

Either way, the tag triggers three independent workflows that run in parallel:

| Platform | Workflow | Destination |
|----------|----------|-------------|
| Android | `android-release.yml` | Play Store (internal track) |
| iOS | `ios-release.yml` | App Store Connect (TestFlight) |
| Desktop | `desktop-release.yml` | GitHub Releases (signed DMG + DEB) and `latest.json` update feed |

All workflows also support manual triggering via the "Run workflow" button in the GitHub Actions UI (`workflow_dispatch`).

### Desktop update strategy

Desktop uses **two different update channels** depending on platform:

| Platform | Distribution | Updates |
|----------|--------------|---------|
| macOS | Notarized `.dmg` on GitHub Releases | **In-app updater** (`DesktopUpdater`) polls the `latest.json` feed and prompts the user to download + install |
| Linux | `.deb` on GitHub Releases | **In-app updater** (same feed) |
| Windows | **Microsoft Store** (MSIX) | **The Store** — the in-app updater is intentionally a no-op on Windows (Store policy forbids self-updating, and the MSIX sandbox can't replace its own install) |

See [Windows: Microsoft Store (MSIX)](#windows-microsoft-store-msix) for the Windows path.

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

#### Desktop Code Signing

**macOS** desktop signing reuses the **existing iOS `MATCH_*` and `ASC_*` secrets** — no desktop-specific secrets are required. The `desktop-release.yml` workflow installs the Developer ID certificate via `fastlane mac certificates` (Fastlane match), resolves the identity into `MACOS_SIGN_IDENTITY` at runtime, signs during `packageReleaseDmg`, and notarizes/staples the DMG with `xcrun notarytool`. See [Desktop macOS code signing](#desktop-macos-code-signing).

**Windows** code signing is **not required** — the Microsoft Store signs MSIX packages on ingestion.

#### Microsoft Store (Partner Center API)

Required only for [automated MSIX submission](#automating-store-submission). Created from an Azure AD application associated with your Partner Center account.

| Secret | Description |
|--------|-------------|
| `PARTNER_CENTER_TENANT_ID` | Azure AD tenant ID |
| `PARTNER_CENTER_CLIENT_ID` | Azure AD application (client) ID |
| `PARTNER_CENTER_CLIENT_SECRET` | Client secret for that Azure AD application |
| `PARTNER_CENTER_SELLER_ID` | Seller ID from Partner Center → Account settings |
| `STORE_PRODUCT_ID` | The Store product/app ID for the reserved app — gates the `publish-store` job |
| `STORE_IDENTITY_NAME` | Partner Center **Package/Identity Name** — substituted into `AppxManifest.xml` (falls back to a placeholder if unset, so the MSIX still builds) |
| `STORE_PUBLISHER` | Partner Center **Publisher** `CN=...` string — substituted into `AppxManifest.xml` |

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
   - `macos-15` — builds `.dmg` via `./gradlew :client:composeApp:packageReleaseDmg`
   - `windows-latest` — builds `.msi` via `./gradlew :client:composeApp:packageReleaseMsi`
   - `ubuntu-latest` — builds `.deb` via `./gradlew :client:composeApp:packageReleaseDeb`

   These `packageRelease*` tasks (not the plain `package*` variants) are required so the requested
   task name contains "Release", which sets `BuildConfig.IS_DEBUG = false` and hides the developer-only
   UI. R8/ProGuard minification is disabled for the release build type in
   `client/composeApp/build.gradle.kts`, so the binary is functionally identical to the old debug
   packaging — it just carries the release marker.
2. On macOS, signs and notarizes the DMG (see [Code signing](#desktop-macos-code-signing) below)
3. On Windows, also builds the app image (`createReleaseDistributable`) and packs an MSIX via `makeappx`
4. Uploads each package (and, on Windows, the MSIX) as build artifacts
5. Generates `latest.json` (the in-app update feed) with the macOS + Linux download URLs — **Windows is intentionally omitted** because it updates via the Store
6. Creates a GitHub Release from the tag with the packages + `latest.json` attached
7. Auto-generates release notes from commits since the last tag
8. On tag pushes, the `publish-store` job submits the MSIX to the Microsoft Store via the MSStore CLI (no-ops until `STORE_PRODUCT_ID` is set)

**Package configuration:** Native distribution settings (package name, version, vendor, platform-specific options) are in `client/composeApp/build.gradle.kts` under the `compose.desktop.application.nativeDistributions` block. The macOS `signing { }` / `notarization { }` blocks are gated on `MACOS_SIGN_IDENTITY` so local `packageDmg` works without a cert.

#### In-app updater (macOS + Linux)

The desktop app embeds `DesktopUpdater` (`client/composeApp/src/jvmMain/.../update/`). On launch it polls the stable feed URL:

```
https://github.com/Plus-Mobile-Apps/chef-mate/releases/latest/download/latest.json
```

GitHub always serves `releases/latest/download/<asset>` from the newest non-prerelease release, so no separate hosting is needed. The updater compares the feed `version` against `BuildConfig.VERSION_NAME`; if newer, it surfaces a banner to download the platform installer and open it. It is a **no-op on Windows** by design.

This is a *signed-installer* model (download + run the new signed package), **not** in-place jar patching — patching jars inside the bundle would invalidate the macOS notarization staple.

**`latest.json` shape:**

```json
{
  "version": "1.8.0",
  "notesUrl": "https://github.com/Plus-Mobile-Apps/chef-mate/releases/tag/v1.8.0",
  "downloads": {
    "macos": "https://github.com/.../releases/download/v1.8.0/Chef-Mate-1.8.0.dmg",
    "linux": "https://github.com/.../releases/download/v1.8.0/chef-mate_1.8.0_amd64.deb"
  }
}
```

#### macOS notarization

The `.p12` for `MACOS_CERT_P12_BASE64` is a "Developer ID Application" certificate exported from Keychain Access (include the private key). Export and encode:

```bash
# In Keychain Access: right-click the "Developer ID Application" cert → Export → .p12
base64 -i DeveloperID.p12 | pbcopy   # store as MACOS_CERT_P12_BASE64
```

`APPLE_APP_PASSWORD` is an app-specific password generated at <https://appleid.apple.com> (Sign-In and Security → App-Specific Passwords). Without notarization, Gatekeeper refuses to launch the app ("damaged / cannot be opened").

---

## Windows: Microsoft Store (MSIX)

Windows ships through the **Microsoft Store** as an MSIX package. This is the recommended Windows path because:

- **The Store signs the package for free** on ingestion — no code-signing certificate needed (no ~$10/month or ~$200+/year cert).
- **The Store handles auto-updates** — which is why the in-app updater no-ops on Windows.
- SmartScreen trusts Store apps, so users never see "unknown publisher" warnings.

> When creating the product in Partner Center, choose **"MSIX or PWA app"**, *not* "EXE or MSI app". The EXE/MSI option only provides a listing that points at your own self-hosted, self-signed installer — it gives you neither free signing nor Store-managed updates.

### One-time Partner Center setup

1. In [Partner Center](https://partner.microsoft.com/dashboard), create a **new MSIX product** and reserve the app name.
2. From the product's **Product Identity** page, record three values needed for the manifest:
   - **Package/Identity Name** (e.g. `12345PlusMobileApps.ChefMate`)
   - **Publisher** (the `CN=...` string, e.g. `CN=ABCD1234-...`)
   - **Publisher Display Name** (e.g. `Plus Mobile Apps`)
3. The **first submission must be done manually** in Partner Center to establish the listing. Automated submission (below) is for subsequent releases.

### Packaging sketch

jpackage / Compose Desktop does **not** emit MSIX, so we package the unpackaged *app image* ourselves. Compose Desktop's `createDistributable` task produces the app image (app + bundled JRE) at:

```
client/composeApp/build/compose/binaries/main/app/Chef Mate/
```

The MSIX is then built by laying that directory next to an `AppxManifest.xml` and running `makeappx` (part of the Windows SDK, preinstalled on `windows-latest` runners). **This is wired into `desktop-release.yml`** — the manifest lives at `packaging/windows/AppxManifest.xml` and uses `__TOKEN__` placeholders that the workflow substitutes at build time.

**`packaging/windows/AppxManifest.xml`** (the committed manifest uses tokens; the identity values come from Partner Center, and the version must be 4-part with a `.0` revision):

```xml
<?xml version="1.0" encoding="utf-8"?>
<Package
    xmlns="http://schemas.microsoft.com/appx/manifest/foundation/windows10"
    xmlns:uap="http://schemas.microsoft.com/appx/manifest/uap/windows10"
    xmlns:rescap="http://schemas.microsoft.com/appx/manifest/foundation/windows10/restrictedcapabilities">

  <Identity Name="[PACKAGE_IDENTITY_NAME]"
            Publisher="[CN=PUBLISHER_ID]"
            Version="1.7.1.0"
            ProcessorArchitecture="x64" />

  <Properties>
    <DisplayName>Chef Mate</DisplayName>
    <PublisherDisplayName>Plus Mobile Apps</PublisherDisplayName>
    <Logo>Assets\StoreLogo.png</Logo>
  </Properties>

  <Dependencies>
    <TargetDeviceFamily Name="Windows.Desktop" MinVersion="10.0.17763.0"
                        MaxVersionTested="10.0.22621.0" />
  </Dependencies>

  <Resources>
    <Resource Language="en-us" />
  </Resources>

  <Applications>
    <Application Id="ChefMate"
                 Executable="Chef Mate.exe"
                 EntryPoint="Windows.FullTrustApplication">
      <uap:VisualElements
          DisplayName="Chef Mate"
          Description="Chef Mate - Your AI Cooking Assistant"
          BackgroundColor="transparent"
          Square150x150Logo="Assets\Square150x150Logo.png"
          Square44x44Logo="Assets\Square44x44Logo.png" />
    </Application>
  </Applications>

  <Capabilities>
    <!-- Win32 / Desktop Bridge apps require the runFullTrust restricted capability -->
    <rescap:Capability Name="runFullTrust" />
  </Capabilities>
</Package>
```

`runFullTrust` is the restricted capability that lets a packaged Win32 app (which a bundled-JRE app is) run unsandboxed. The required logo assets (`StoreLogo`, `Square150x150Logo`, `Square44x44Logo`) are generated in CI by copying the existing app icon — replace these with correctly-sized PNGs before submitting for Store certification.

**Build step** (the `Build app image for MSIX` + `Package MSIX` steps on the Windows leg of `desktop-release.yml`). The packaging step substitutes the manifest tokens, copies the icon into the `Assets`, and packs with `makeappx`:

```yaml
  - name: Package MSIX (Windows)
    if: runner.os == 'Windows'
    shell: pwsh
    env:
      STORE_IDENTITY_NAME: ${{ secrets.STORE_IDENTITY_NAME }}
      STORE_PUBLISHER: ${{ secrets.STORE_PUBLISHER }}
    run: |
      # resolve version (tag → x.y.z), substitute __IDENTITY_NAME__/__PUBLISHER__/__VERSION__,
      # copy icon to Assets\{StoreLogo,Square150x150Logo,Square44x44Logo}.png, then:
      $makeappx = (Get-ChildItem "${env:ProgramFiles(x86)}\Windows Kits\10\bin\*\x64\makeappx.exe" |
                   Sort-Object FullName -Descending | Select-Object -First 1).FullName
      & $makeappx pack /d "$img" /p "$env:RUNNER_TEMP\ChefMate.msix" /o
```

> Because the MSIX is signed by the Store on submission, you do **not** sign it yourself in CI. (You only need a self-signed cert to *sideload* it for local testing.)

### Automating Store submission

**Yes — this is wired in** via Microsoft's [**MSStore CLI**](https://github.com/microsoft/msstore-cli), driven from the `publish-store` job in `desktop-release.yml`.

**Auth setup (one-time):**
1. In Partner Center → **Account settings → User management → Azure AD applications**, add (or create) an Azure AD application and grant it the **Manager** role.
2. Record the **tenant ID**, **client ID**, and create a **client secret**; grab the **seller ID** from Account settings. Store them as the `PARTNER_CENTER_*` secrets listed above.
3. Set `STORE_PRODUCT_ID` (and `STORE_IDENTITY_NAME` / `STORE_PUBLISHER`). The `publish-store` job no-ops while `STORE_PRODUCT_ID` is unset, so it's safe to leave dormant until you're ready.

**The job** (runs only on tag pushes; each step is gated on `STORE_PRODUCT_ID`):

```yaml
  publish-store:
    needs: build
    if: startsWith(github.ref, 'refs/tags/')
    runs-on: windows-latest
    env:
      STORE_PRODUCT_ID: ${{ secrets.STORE_PRODUCT_ID }}
    steps:
      - uses: actions/download-artifact@v4
        if: env.STORE_PRODUCT_ID != ''
        with: { name: desktop-windows-msix, path: msix }
      - name: Setup MSStore CLI
        if: env.STORE_PRODUCT_ID != ''
        uses: microsoft/setup-msstore-cli@v1
      - name: Configure MSStore CLI
        if: env.STORE_PRODUCT_ID != ''
        run: >
          msstore reconfigure
          --tenantId ${{ secrets.PARTNER_CENTER_TENANT_ID }}
          --sellerId ${{ secrets.PARTNER_CENTER_SELLER_ID }}
          --clientId ${{ secrets.PARTNER_CENTER_CLIENT_ID }}
          --clientSecret ${{ secrets.PARTNER_CENTER_CLIENT_SECRET }}
      - name: Publish to Microsoft Store
        if: env.STORE_PRODUCT_ID != ''
        shell: pwsh
        run: |
          $msix = (Get-ChildItem msix/*.msix | Select-Object -First 1).FullName
          msstore publish -i $msix -id $env:STORE_PRODUCT_ID
```

**Caveats / things to verify on first run:**
- The **first** submission must be made manually in Partner Center; the API updates an existing listing, it doesn't create one.
- Verify the `microsoft/setup-msstore-cli` action ref and the `msstore publish -id` value (Partner Center product ID vs. Store app ID) against the [current MSStore CLI docs](https://learn.microsoft.com/en-us/windows/apps/publish/msstore-dev-cli/commands) — the CLI is in preview and flags shift.
- Store **certification review** still applies to each submission and can take hours to a day, so Store releases lag the GitHub Release / mobile releases.
- The MSIX `Identity/Version` is derived from the release tag, so it increments automatically with the [version bump](#version-management).

> **Status:** the MSIX packaging steps and `publish-store` job are wired into `desktop-release.yml`. The MSIX builds on every run (with placeholder identity until the Store secrets are set); submission only fires on tag pushes once `STORE_PRODUCT_ID` is configured. The unsigned `.msi` is still built as a fallback/sideload artifact.

#### Desktop macOS code signing

The macOS DMG is signed with a **Developer ID Application** certificate and notarized so it opens without a Gatekeeper warning. Windows and Linux packages remain unsigned.

**Certificate:** Stored and fetched via Fastlane `match` (`type: "developer_id"`) from the same private certificates repo used for iOS. The `mac certificates` lane (`fastlane/Fastfile`) runs `setup_ci` + `match` (readonly) to import the cert into a temporary CI keychain.

**Signing:** Enabled in `client/composeApp/build.gradle.kts` via the `macOS { signing { ... } }` block, gated on the `MACOS_SIGN_IDENTITY` env var (resolved in CI from the keychain). Local builds without that var stay unsigned. Compose Desktop applies hardened runtime + default entitlements automatically when signing.

**Notarization:** The workflow submits the built DMG with `xcrun notarytool submit --wait` and then `xcrun stapler staple`, reusing the App Store Connect API key (`ASC_*` secrets) — no app-specific password needed.

**One-time bootstrap:** Apple does **not** allow creating a Developer ID Application certificate via
the App Store Connect API (only the Account Holder can, through an interactive session), so the cert
must be created manually once and then imported into the Match repo:

1. Create the cert as the Account Holder — in Xcode: **Settings → Accounts → Manage Certificates → +
   → Developer ID Application** (or via developer.apple.com → Certificates). This also places the
   cert + private key in your login keychain.
2. Download the certificate `.cer` (developer.apple.com → Certificates → the Developer ID Application
   row → Download), e.g. `developerID_application.cer`.
3. Export the private key from **Keychain Access** as a `.p12` (any password), then convert it to the
   exact format `match` + `security import` expect — a **password-less PKCS#1 PEM key**:

   ```bash
   # strip the p12 password (OpenSSL 3 needs -legacy for Apple's RC2 container)
   openssl pkcs12 -legacy -in developer_id.p12 -nodes -nocerts -out /tmp/nopass.pem   # enter export pw
   # emit a bare PKCS#1 key ("BEGIN RSA PRIVATE KEY"); PKCS#8 ("BEGIN PRIVATE KEY") is rejected as
   # "Unknown format" when the file has a .p12 extension, and bag-attribute preambles break pairing
   openssl rsa -traditional -in /tmp/nopass.pem -out developer_id_key.p12
   head -1 developer_id_key.p12   # MUST be: -----BEGIN RSA PRIVATE KEY-----
   rm -f /tmp/nopass.pem
   ```

   Why: `match` stores the key as `<id>.p12` and installs it in CI with `security import … -P ""`
   (empty password). That only works if the file is a bare PKCS#1 PEM key — a real PKCS12 container
   fails MAC verification (OpenSSL↔Apple empty-password mismatch), and a PKCS#8 key is "Unknown
   format" under the `.p12` extension.

4. Import both into the certificates repo. **Unset the ASC API key env vars first** — `match` reads
   `APP_STORE_CONNECT_API_KEY` as its `api_key` option and fails with `'api_key' value must be a Hash`:

   ```bash
   unset APP_STORE_CONNECT_API_KEY APP_STORE_CONNECT_API_KEY_ID APP_STORE_CONNECT_API_ISSUER_ID
   export MATCH_PASSWORD=<match repo passphrase>

   bundle exec fastlane match import \
     --type developer_id \
     --git_url git@github.com:Plus-Mobile-Apps/certificates.git
   # Certificate (.cer): developerID_application.cer
   # Private Key (.p12): developer_id_key.p12
   # Provisioning Profile: <press Enter — Developer ID apps use none>
   ```

CI then consumes the stored cert read-only via `fastlane mac certificates`.

**Intermediate CA:** a Developer ID leaf only forms a *valid* codesigning identity when its issuing
intermediate is in the keychain. CI runners lack it, so the correct one (subject
`CN=Developer ID Certification Authority, OU=Apple Certification Authority` — **not** the G2
intermediate) is vendored at `.github/certs/DeveloperIDCA.pem` and imported by the workflow. If you
rotate to a cert issued by a different intermediate, replace that file to match the new leaf's issuer.

**Reused secrets:** `MATCH_PASSWORD`, `MATCH_GIT_BASIC_AUTHORIZATION`, `ASC_KEY_ID`, `ASC_ISSUER_ID`, `ASC_API_KEY` (no new secrets required).

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

# Desktop — build release packages for your current OS (hides developer-only UI).
# Output lands in build/compose/binaries/main-release/. Use the plain package* tasks
# only for local debug builds where you want the Developer Settings row visible.
./gradlew :client:composeApp:packageReleaseDmg    # macOS
./gradlew :client:composeApp:packageReleaseMsi    # Windows
./gradlew :client:composeApp:packageReleaseDeb    # Linux
```

## Version Management

Versions are defined in two files across three platforms:

| Platform | File | Fields |
|----------|------|--------|
| Android | `client/composeApp/build.gradle.kts` | `versionCode` / `versionName` |
| iOS | `iosApp/Configuration/Config.xcconfig` | `CURRENT_PROJECT_VERSION` / `MARKETING_VERSION` |
| Desktop | `client/composeApp/build.gradle.kts` | `packageVersion` (generic + macOS) |

### CI version bump (recommended)

The normal release path is fully automated and **does not require running anything locally or creating tags by hand**:

1. Trigger the **Bump Version** workflow (`bump-version.yml`) from the GitHub Actions UI (`workflow_dispatch`), choosing `major` / `minor` / `patch` / `custom`.
2. It updates all version fields (Android, iOS, Desktop) and opens a PR (`chore: bump version to X.Y.Z`).
3. Merging that PR triggers `build-release.yml`, which creates the `vX.Y.Z` tag and publishes the GitHub Release.
4. The tag push then triggers `android-release`, `desktop-release`, and `ios-release` (requires `RELEASE_TOKEN`).

See [ci/VERSION_BUMP_SETUP.md](ci/VERSION_BUMP_SETUP.md) for the required `RELEASE_TOKEN` configuration.

### Bump script (local alternative)

For local bumps (e.g. when not using the CI workflow), `scripts/bump-version.sh` updates all version fields at once. Run it with no arguments for interactive mode:

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

## Known Limitations

- **Windows MSI and Linux DEB are unsigned** — only the macOS DMG is signed/notarized (see [Desktop macOS code signing](#desktop-macos-code-signing)). Windows ships via the Microsoft Store (Store-signed), so this only affects the fallback MSI.
- **MSIX packaging logo assets are placeholders** — CI reuses the app icon for all three Store logos, which won't pass Store certification. Replace with correctly-sized PNGs (see [Windows: Microsoft Store (MSIX)](#windows-microsoft-store-msix)).
- **MSStore CLI is in preview** — the `publish-store` job's action ref and `publish` flags should be verified against current docs on first real submission.
- **Store releases lag** — Microsoft Store certification review delays Windows releases relative to the GitHub Release and mobile stores.
- **R8/ProGuard is disabled** — Android release builds are not minified. Enabling R8 requires ProGuard rules for KMP libraries (Ktor, Supabase, kotlinx.serialization, Decompose).
