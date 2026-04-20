# bump-version

Bump the version and/or build number for all three client platforms (Android, JVM desktop, iOS) in lockstep.

## Required Input

The user must specify the bump type. Ask if not provided.

| Mode | What changes |
|---|---|
| `patch` | Semantic version patch component +1; build numbers stay or are set per below |
| `minor` | Semantic version minor +1, patch resets to 0 |
| `major` | Semantic version major +1, minor and patch reset to 0 |
| `build` | Build number only (+1 to versionCode / CURRENT_PROJECT_VERSION); semantic version unchanged |

Default when the user says "bump" with no qualifier: **patch**.

## Version files

All three platforms are updated together. One file holds Android + JVM; a second holds iOS.

| Platform | File | Fields |
|---|---|---|
| Android | `client/composeApp/build.gradle.kts` | `versionCode` (integer), `versionName` (quoted string) |
| JVM desktop | `client/composeApp/build.gradle.kts` | `packageVersion` (quoted string — no separate build number concept) |
| iOS | `iosApp/Configuration/Config.xcconfig` | `CURRENT_PROJECT_VERSION` (integer), `MARKETING_VERSION` (bare string) |

### Conventions

- The project uses `v{major}.{minor}.{patch}` git tags as the authoritative version record.
- `versionCode` (Android) and `CURRENT_PROJECT_VERSION` (iOS) are kept in sync and equal to the patch component of the semantic version. For a `build`-only bump they increment independently.
- `versionName`, `packageVersion`, and `MARKETING_VERSION` all carry the same `{major}.{minor}.{patch}` string.

## Instructions

### Step 1: Determine current version

```sh
# Read the latest tag — this is the authoritative current version
git tag --sort=-v:refname | head -1
# e.g. → v0.1.15
```

Parse `major`, `minor`, `patch` from the tag (strip the leading `v`).

### Step 2: Calculate new values

**For `patch` bump** (e.g. 0.1.15 → 0.1.16):
```
new_patch   = patch + 1
new_version = "{major}.{minor}.{new_patch}"
new_build   = new_patch       # build number tracks patch
```

**For `minor` bump** (e.g. 0.1.15 → 0.2.0):
```
new_minor   = minor + 1
new_version = "{major}.{new_minor}.0"
new_build   = 0               # reset on minor bump; increment before first release
```

**For `major` bump** (e.g. 0.1.15 → 1.0.0):
```
new_major   = major + 1
new_version = "{new_major}.0.0"
new_build   = 0
```

**For `build`-only bump** (e.g. build 15 → 16, version stays 0.1.15):
```
new_build   = current_build + 1   # read from versionCode in build.gradle.kts
new_version = unchanged
```

To read the current build number when doing a build-only bump:
```sh
grep 'versionCode' client/composeApp/build.gradle.kts
```

### Step 3: Edit `client/composeApp/build.gradle.kts`

Locate the `android { defaultConfig { ... } }` block and the `nativeDistributions { ... }` block.

For a **semantic version bump**, update all three lines:
```kotlin
// android defaultConfig
versionCode = {new_build}
versionName = "{new_version}"

// compose.desktop nativeDistributions
packageVersion = "{new_version}"
```

For a **build-only bump**, update only `versionCode`:
```kotlin
versionCode = {new_build}
```

### Step 4: Edit `iosApp/Configuration/Config.xcconfig`

For a **semantic version bump**:
```
CURRENT_PROJECT_VERSION={new_build}
MARKETING_VERSION={new_version}
```

For a **build-only bump**, update only `CURRENT_PROJECT_VERSION`:
```
CURRENT_PROJECT_VERSION={new_build}
```

### Step 5: Verify consistency

After editing, run these greps and confirm the values are what you expect:

```sh
grep -n 'versionCode\|versionName\|packageVersion' client/composeApp/build.gradle.kts
grep -n 'CURRENT_PROJECT_VERSION\|MARKETING_VERSION' iosApp/Configuration/Config.xcconfig
```

All semantic version strings must match. Build numbers on Android and iOS must match (except during a build-only bump initiated from a state where they were already out of sync — flag that to the user).

### Step 6: Commit

```sh
git add client/composeApp/build.gradle.kts iosApp/Configuration/Config.xcconfig
git commit -m "chore: bump version to {new_version} (build {new_build})"
```

For a build-only bump:
```sh
git commit -m "chore: bump build number to {new_build}"
```

### Step 7: Tag (semantic bumps only)

```sh
git tag v{new_version}
```

Do **not** create a tag for build-only bumps — tags mark public releases.

Push both the branch and the tag:
```sh
git push
git push origin v{new_version}
```

Report to the user: old version, new version, new build number, files touched, and the new tag.
