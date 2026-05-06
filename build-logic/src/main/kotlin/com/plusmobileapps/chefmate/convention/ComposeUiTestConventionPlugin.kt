package com.plusmobileapps.chefmate.convention

import com.android.build.gradle.LibraryExtension
import com.plusmobileapps.chefmate.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

/**
 * Wires Compose multiplatform UI testing into a KMP feature module so `runComposeUiTest`-based
 * tests in `commonTest` run on:
 * - JVM via `jvmTest` (using `compose.desktop.uiTestJUnit4`)
 * - iOS via `iosSimulatorArm64Test` (using `compose.uiTest`)
 * - Android via `connectedDebugAndroidTest` on the emulator (using
 *   `androidx.compose.ui:ui-test-junit4-android`)
 *
 * On Android this routes `commonTest` to `androidInstrumentedTest` instead of `androidUnitTest`, so
 * the per-screen tests run against a real emulator rather than failing in `testDebugUnitTest`
 * (which has no Robolectric/ instrumentation wiring). Other modules that don't opt in are
 * unaffected.
 */
fun Project.applyComposeUiTest() {
    extensions.configure<KotlinMultiplatformExtension> {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        androidTarget { instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test) }

        sourceSets.apply {
            val commonTest = getByName("commonTest")
            val jvmTest = getByName("jvmTest")
            val compose = ComposePlugin.Dependencies(project)

            @OptIn(ExperimentalComposeLibrary::class)
            commonTest.dependencies { implementation(compose.uiTest) }
            jvmTest.dependencies { implementation(compose.desktop.uiTestJUnit4) }
        }
    }

    extensions.configure<LibraryExtension> {
        defaultConfig { testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
    }

    dependencies {
        "androidTestImplementation"(libs.androidx.compose.ui.test.junit4.android)
        "androidTestImplementation"(libs.androidx.test.runner)
        "debugImplementation"(libs.androidx.compose.ui.test.manifest)
    }

    // `instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)` adds
    // commonTest to androidInstrumentedTest but does NOT remove it from
    // androidUnitTest. `runComposeUiTest`-based tests crash in androidUnitTest
    // (no Robolectric / instrumentation), so disable the unit-test tasks here —
    // the same tests run on the emulator via connectedDebugAndroidTest.
    tasks
        .matching { it.name == "testDebugUnitTest" || it.name == "testReleaseUnitTest" }
        .configureEach { enabled = false }
}
