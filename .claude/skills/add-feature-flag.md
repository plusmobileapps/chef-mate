# add-feature-flag

Add a new feature flag to the ChefMate KMP project — registers it in code, inserts the Supabase row, and wires it into the Bloc or ViewModel that needs it.

## Required input

Ask the user (if not provided):

1. **Flag name** — short, snake_case key, e.g. `show_onboarding_v2`. This becomes `FeatureFlag.key` and the Supabase row primary key.
2. **Flag type** — `Boolean` (on/off) or `String` (arbitrary text value).
3. **Default value** — what the app uses when offline, before the first refresh, or when no row exists. Pick the *safer* outcome (usually `false` for boolean, `""` for string).
4. **Description** — one sentence shown in the dev-settings Feature Flags screen.
5. **Where it's consumed** — which Bloc or ViewModel should read the flag.

## Where the pieces live

| Concern | Path |
|---|---|
| Flag registry (add the object here) | `client/featureflag/public/src/commonMain/kotlin/com/plusmobileapps/chefmate/featureflag/FeatureFlag.kt` |
| Public API (`FeatureFlags`, `isEnabled`) | `client/featureflag/public/src/commonMain/kotlin/com/plusmobileapps/chefmate/featureflag/FeatureFlags.kt` |
| Fake for tests | `client/featureflag/testing/src/commonMain/kotlin/com/plusmobileapps/chefmate/featureflag/testing/FakeFeatureFlags.kt` |
| Supabase table docs + SQL | `docs/feature-flags.md` |

## Step 1 — Register the flag in code

Open `FeatureFlag.kt` and add a new `object` inside `FeatureFlagRegistry`:

**Boolean flag:**
```kotlin
object ShowOnboardingV2 : BooleanFlag(
    key = "show_onboarding_v2",
    defaultValue = false,
    description = "Show the redesigned onboarding flow.",
)
```

**String flag:**
```kotlin
object HomeBannerText : StringFlag(
    key = "home_banner_text",
    defaultValue = "",
    description = "Optional banner copy shown on the home screen.",
)
```

Then append it to `FeatureFlagRegistry.all`:
```kotlin
val all: List<FeatureFlag<*>> = listOf(CookModeV2, HomeBannerText, ShowOnboardingV2)
```

`all` drives the dev-settings Feature Flags screen — every flag in the list gets a row there automatically.

## Step 2 — Insert the Supabase row

Run this in the Supabase SQL editor (Table Editor works too):

**Boolean flag:**
```sql
insert into feature_flags (key, value_type, value, enabled, rollout_percent)
values ('show_onboarding_v2', 'bool', 'false', true, 0);
```

**String flag:**
```sql
insert into feature_flags (key, value_type, value, enabled, rollout_percent)
values ('home_banner_text', 'string', '', true, 0);
```

`rollout_percent = 0` means nobody gets the non-default value yet. Bump it when ready to roll out:
```sql
update feature_flags set rollout_percent = 100 where key = 'show_onboarding_v2';
```

## Step 3 — Read the flag in a Bloc or ViewModel

Inject `FeatureFlags` and expose a `StateFlow`:

**Boolean (most common):**
```kotlin
@Inject
class MyViewModel(
    @Main mainContext: CoroutineContext,
    featureFlags: FeatureFlags,
    // … other deps
) : ViewModel(mainContext) {

    val showOnboardingV2: StateFlow<Boolean> =
        featureFlags.isEnabled(FeatureFlagRegistry.ShowOnboardingV2)
}
```

**String:**
```kotlin
val bannerText: StateFlow<String> =
    featureFlags.valueOf(FeatureFlagRegistry.HomeBannerText)
```

Then surface it through the Bloc's `Model` as usual (`mapState`, `collectAsState`, etc.).

## Step 4 — Update tests that construct the ViewModel / Bloc

Any test that instantiates the ViewModel or Bloc now needs `FakeFeatureFlags`. The fake defaults everything to the flag's `defaultValue`, so existing tests require no change unless they specifically need a different value:

```kotlin
// default — flag returns its in-code defaultValue
val vm = MyViewModel(mainContext, FakeFeatureFlags(), …)

// override for a specific test
val fake = FakeFeatureFlags(mapOf(FeatureFlagRegistry.ShowOnboardingV2 to true))
val vm = MyViewModel(mainContext, fake, …)

// change the value mid-test (triggers StateFlow update)
fake.set(FeatureFlagRegistry.ShowOnboardingV2, false)
```

## Gotchas

- **`key` must match exactly** — the Supabase `key` column is the primary key and is case-sensitive. A mismatch means the client silently returns `defaultValue` for every user.
- **`value_type` must agree with the Kotlin type** — `BooleanFlag` expects `value_type = 'bool'`; `StringFlag` expects `'string'`. A mismatch is treated as a missing row (falls back to `defaultValue`).
- **`rollout_percent = 0` on insert** — always start at 0 and ramp deliberately. Starting at 100 is a one-way door until you edit the row.
- **Changes propagate on cold start** — there is no live poll. To test immediately on a device, use Developer Settings → Feature Flags to force-override the flag without changing the remote row.
- **`all` list drives the dev UI** — if you forget to append to `FeatureFlagRegistry.all`, the flag won't appear in the Feature Flags screen and can't be overridden from dev settings.
- **`FakeFeatureFlags` is type-safe** — `fake.set(flag, value)` is generic; the compiler rejects `fake.set(BooleanFlag, "string")`. If you see a type error there, you're setting the wrong value type.

## Verification checklist

1. `./gradlew :client:featureflag:public:compileKotlinMetadata` — registry change compiles.
2. `./gradlew :client:composeApp:compileDebugKotlinAndroid` — end-to-end compile check.
3. `./gradlew :client:<consuming-module>:impl:test` — existing tests still pass.
4. `./gradlew ktfmtFormat` — keep formatting clean.
5. In a debug build, open Developer Settings → Feature Flags and confirm the new flag appears in the list with the correct default.
6. Force-override it ON/OFF and confirm the consuming screen reacts correctly.
