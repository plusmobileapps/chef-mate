# Version Bump & Release Automation

This document covers the two GitHub Actions workflows that automate versioning and
releasing across all platforms (Android, iOS, Desktop).

## How it works

```
Developer                   GitHub Actions
──────────                  ──────────────────────────────────────────────────
Run "Bump Version"          bump-version.yml
  (Actions UI)           ─► reads current version
                            computes new version
                            edits build.gradle.kts + Config.xcconfig
                            opens PR "chore: bump version to X.Y.Z (build N)"
                                │
Merge PR (squash)           build-release.yml  (triggered by push to main)
                         ─► creates git tag vX.Y.Z
                            builds signed release APK
                            publishes GitHub release with APK attached
                                │
                         (tag push triggers downstream workflows if RELEASE_TOKEN set)
                             ├─► android-release.yml  → uploads to Play Store
                             ├─► desktop-release.yml  → builds DMG / MSI / Deb
                             └─► ios-release.yml      → iOS release
```

## Files changed by the bump

| File | Fields |
|---|---|
| `client/composeApp/build.gradle.kts` | `versionCode`, `versionName`, `packageVersion` (JVM), `packageVersion` (macOS block) |
| `iosApp/Configuration/Config.xcconfig` | `CURRENT_PROJECT_VERSION`, `MARKETING_VERSION` |

The macOS `packageVersion` is always `1.x.y` when the app version is `0.x.y`
because macOS DMG packaging requires `MAJOR > 0`. See the comment in
`client/composeApp/build.gradle.kts` macOS block.

> **Note:** `scripts/bump-version.sh` (the interactive local script) has a
> `to_mac_version()` function that is currently a pass-through and does **not**
> apply this mapping. If you use the script locally, update `to_mac_version()`
> so it matches CI behaviour, or correct the macOS value manually after running.

## Required GitHub secrets

Configure these in **Settings → Secrets and variables → Actions → Repository secrets**.

### Always required

| Secret | Description |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded release keystore (`base64 -i release.jks`) |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Key alias within the keystore |
| `ANDROID_KEY_PASSWORD` | Key password |
| `SUPABASE_URL` | Supabase project URL |
| `SUPABASE_KEY` | Supabase anon/service key |
| `BUGSNAG_API_KEY` | Bugsnag API key |

### Required for downstream workflow triggering

| Secret | Description |
|---|---|
| `RELEASE_TOKEN` | Personal Access Token with `repo` scope (or fine-grained with `contents: write` + `workflows: write`). Without this, `android-release`, `desktop-release`, and `ios-release` will **not** run after the tag is pushed, because the default `GITHUB_TOKEN` cannot trigger other workflows. |

### Required by existing workflows (already configured)

| Secret | Used by |
|---|---|
| `PLAY_STORE_SERVICE_ACCOUNT_JSON` | `android-release.yml` (Fastlane) |

## Step-by-step: running your first bump

1. Go to **Actions → Bump Version → Run workflow**.
2. Choose a bump type:
   - `patch` — `0.1.21` → `0.1.22`, build `22`
   - `minor` — `0.1.21` → `0.2.0`, build `22`
   - `major` — `0.1.21` → `1.0.0`, build `22`
   - `custom` — enter an explicit `X.Y.Z` in the second field
3. Click **Run workflow**. The workflow will open a PR titled
   `chore: bump version to X.Y.Z (build N)`.
4. Review the PR (check the version table in the body), then **squash merge** it.
   - Squash merge ensures the commit message on `main` matches
     `chore: bump version to X.Y.Z (build N)`, which is what `build-release.yml`
     looks for.
5. `build-release.yml` triggers automatically on the push to `main`:
   - Creates and pushes git tag `vX.Y.Z`
   - Builds the signed release APK
   - Creates a GitHub release with the APK attached
6. If `RELEASE_TOKEN` is configured, the tag push also triggers:
   - `android-release.yml` → Fastlane uploads the AAB to Play Store
   - `desktop-release.yml` → builds DMG / MSI / Deb packages
   - `ios-release.yml` → iOS release

## Configuring `RELEASE_TOKEN`

1. Go to **GitHub → Settings → Developer settings → Personal access tokens → Fine-grained tokens**.
2. Create a token scoped to this repository with these permissions:
   - **Contents**: Read and write (for creating tags)
   - **Workflows**: Read and write (for triggering workflow runs)
3. Add it as a repository secret named `RELEASE_TOKEN`.

## Setting up Android signing

The `build-release.yml` workflow calls `./gradlew :client:composeApp:assembleRelease`
with signing env vars that are read by the signing config in
`client/composeApp/build.gradle.kts`.

To prepare the `ANDROID_KEYSTORE_BASE64` secret:

```bash
# Encode your keystore file
base64 -i /path/to/release.jks | pbcopy   # copies to clipboard (macOS)
# Paste the output as the ANDROID_KEYSTORE_BASE64 secret value
```

## Validating locally

Run the test script before pushing changes to the workflow:

```bash
./scripts/test-version-bump.sh           # tests bump to 0.99.0 (build 99) by default
./scripts/test-version-bump.sh 1.0.0 42  # custom version and build number
```

The script exercises the same sed/awk patterns as the CI and exits non-zero if any
value is not set correctly.

## Common issues

### Workflow does not trigger after merging the bump PR

Check that you used **squash merge** and that the resulting commit message on `main`
starts with `chore: bump version`. If you used a regular merge commit, the commit
message is `Merge pull request #N from ...` and the `if:` condition won't match.

### `android-release` / `desktop-release` not triggered by the tag

The `RELEASE_TOKEN` secret is missing or has insufficient permissions. See
[Configuring `RELEASE_TOKEN`](#configuring-release_token) above.

### APK not found after build

The `assembleRelease` task might have failed silently, or the keystore env vars are
not set. Check the workflow logs for Gradle errors. Ensure all four signing secrets
are configured.

### Duplicate GitHub release

If `build-release.yml` and `desktop-release.yml` both try to create a release for the
same tag, `softprops/action-gh-release` will update the existing release rather than
fail — this is expected behaviour.
