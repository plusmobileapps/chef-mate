plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.recipe.data.public)
            api(projects.client.text.public)
            api(libs.arkivanov.decompose.core)
            api(libs.kotlin.coroutines.core)
            api(projects.client.shared)
            api(projects.client.ui.public)
            implementation(libs.arkivanov.decompose.compose.extensions)
            implementation(libs.kotlinx.serialization.json)
            implementation(compose.components.resources)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.cook" }

// Compose multiplatform UI tests in commonTest run on JVM and iOS only.
// runComposeUiTest on Android requires Robolectric + ui-test-junit4-android,
// which we have not wired up yet — skip Android unit tests for this module
// (it has no Android-specific source) until that infrastructure lands.
afterEvaluate {
    tasks
        .matching { it.name == "testDebugUnitTest" || it.name == "testReleaseUnitTest" }
        .configureEach { enabled = false }
}
