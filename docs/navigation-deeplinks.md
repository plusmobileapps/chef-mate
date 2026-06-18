# Navigation Deeplinks

`chefmate://` deeplinks let you cold-start the app directly on a specific screen, which speeds up iteration on screens that are normally several taps deep.

These are **cold-start only**: the deeplink is parsed once during app launch and used as the initial Decompose stack. To re-target a different screen, fully kill the app and re-launch it (see platform instructions below). Process-death restore keeps the user's saved stack — the deeplink does **not** override it.

For the unrelated Supabase email-verification flow (`chefmate://auth/...`), see [DEEP_LINKING_SETUP.md](DEEP_LINKING_SETUP.md).

## Supported URIs

| URI | Lands on |
|---|---|
| `chefmate://recipe/{id}` | Recipe detail for the given recipe id (`Long`) |
| `chefmate://groceries` | Bottom-nav `Groceries` tab |
| `chefmate://meal-planner` | Bottom-nav `Meals` tab |
| `chefmate://settings` | App settings root screen (the destination reached via Settings tab → "App Settings") |
| `chefmate://signin` | Authentication screen in sign-in mode |
| `chefmate://signup` | Authentication screen in sign-up mode |

Unknown URIs, missing/invalid path segments (`chefmate://recipe`, `chefmate://recipe/abc`), and non-`chefmate://` URIs all fall back to `DeepLink.None` and the app launches normally on the recipes tab.

## Android

The `chefmate://` scheme is registered via an `ACTION_VIEW` intent-filter on `MainActivity` in `client/androidApp/src/main/AndroidManifest.xml`. `MainActivity.onCreate` reads `intent.data` and passes the parsed `DeepLink` to `buildRootBloc`.

```bash
# Force-stop first so a fresh cold start picks up the new intent
adb shell am force-stop com.plusmobileapps.chefmate

# Launch with a deeplink
adb shell am start -W -a android.intent.action.VIEW -d "chefmate://recipe/1"
adb shell am start -W -a android.intent.action.VIEW -d "chefmate://groceries"
adb shell am start -W -a android.intent.action.VIEW -d "chefmate://meal-planner"
adb shell am start -W -a android.intent.action.VIEW -d "chefmate://settings"
adb shell am start -W -a android.intent.action.VIEW -d "chefmate://signin"
adb shell am start -W -a android.intent.action.VIEW -d "chefmate://signup"
```

Without `force-stop`, Android delivers the intent to the already-running activity via `onNewIntent`, which currently only handles share intents — the navigation deeplink is ignored.

## Desktop (JVM)

`main(args: Array<String>)` takes the first CLI argument and parses it as a deeplink.

```bash
./gradlew :client:composeApp:run --args="chefmate://recipe/1"
./gradlew :client:composeApp:run --args="chefmate://groceries"
./gradlew :client:composeApp:run --args="chefmate://meal-planner"
./gradlew :client:composeApp:run --args="chefmate://settings"
./gradlew :client:composeApp:run --args="chefmate://signin"
./gradlew :client:composeApp:run --args="chefmate://signup"
```

## iOS

The `chefmate` URL scheme is registered in `iosApp/iosApp/Info.plist` under `CFBundleURLTypes`. `AppDelegate.application(_:didFinishLaunchingWithOptions:)` captures `launchOptions[.url]` (excluding the existing `chefmate://import?url=...` share flow) and forwards the URL string to `RootBlocProvider.buildRootBloc(..., deepLinkUrl:)`.

```bash
# Boot the simulator first if it isn't running
xcrun simctl boot "iPhone 15"   # adjust to your installed simulator

# Cold-start with a deeplink (simctl terminate ensures the next launch is cold)
xcrun simctl terminate booted com.plusmobileapps.chefmate
xcrun simctl openurl booted "chefmate://recipe/1"
xcrun simctl openurl booted "chefmate://groceries"
xcrun simctl openurl booted "chefmate://meal-planner"
xcrun simctl openurl booted "chefmate://settings"
xcrun simctl openurl booted "chefmate://signin"
xcrun simctl openurl booted "chefmate://signup"
```

The `chefmate://import?url=...` share flow still goes through the existing `onOpenURL` handler in `ContentView.swift` and is unaffected.

## Adding a new deeplink

1. Add a new `data object` or `data class` to `DeepLink` in `client/root/public/src/commonMain/kotlin/com/plusmobileapps/chefmate/root/DeepLink.kt` and a matching branch in `DeepLink.parse`.
2. Extend `RootBlocImpl.initialStackFor` in `client/root/impl/...` to map the new variant to a `Configuration` (or list of configurations).
3. If the target lives behind the bottom nav, also update the `initialBottomNavTab` mapping.
4. Add coverage in `DeepLinkTest` (parser) and `RootBlocTest` (initial stack).

Platform entry points already forward any parsed deeplink, so no changes are required on Android/JVM/iOS for additional URIs.
