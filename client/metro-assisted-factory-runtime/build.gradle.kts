// In-repo replacement for `com.plusmobileapps.metro-extensions:assisted-factory-runtime`.
//
// The published 0.1.0 runtime targets android/jvm/ios/macos/tvos/watchos but NOT wasmJs, which
// blocks the web target since `@ContributesAssistedFactory` is used in commonMain across every
// BLoC. This module re-declares that single annotation at its original fully-qualified name for
// all targets (incl. wasmJs); the KSP compiler matches by FQN, so generated factories are
// identical. `applyMetro()` wires this module in place of the published runtime.
//
// Remove this module and restore `libs.metroExtensions.assistedFactory.runtime` once a
// wasmJs-targeting release of metro-extensions is published.
plugins { alias(libs.plugins.kmpLibrary) }

// enableDi is intentionally left false so kmpLibrary does NOT apply Metro here (which would pull
// the very runtime this module replaces, creating a cycle).
plusLibrary { namespace = "com.plusmobileapps.metro.extensions.assistedfactory" }
