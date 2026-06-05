package com.plusmobileapps.chefmate.convention

import com.plusmobileapps.chefmate.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("org.jetbrains.compose.hot-reload")
            }

            val kotlin = extensions.getByType<KotlinMultiplatformExtension>()

            // Configure compose extension if needed
            extensions.configure<ComposeExtension> {
                // Configuration if needed
            }

            extensions.configure<ComposeCompilerGradlePluginExtension> {
                // Declare known-immutable types (read-only collections, TextData) stable so Models
                // exposing them become skippable app-wide. See compose-stability.conf for the list
                // and the safety rationale.
                stabilityConfigurationFiles.add(
                    rootProject.layout.projectDirectory.file("compose-stability.conf")
                )

                // Opt-in stability/skippability reports for verification: build with
                // -PcomposeMetrics=true and inspect build/compose-metrics. Off by default.
                if (findProperty("composeMetrics") == "true") {
                    val dir = rootProject.layout.buildDirectory.dir("compose-metrics")
                    metricsDestination.set(dir)
                    reportsDestination.set(dir)
                }
            }

            afterEvaluate {
                kotlin.sourceSets.apply {
                    val commonMain = getByName("commonMain")
                    val androidMain = findByName("androidMain")
                    val jvmMain = findByName("jvmMain")
                    val compose = ComposePlugin.Dependencies(project)

                    commonMain.dependencies {
                        // Immutable collections are used in Compose-facing Models so the compiler
                        // (which natively treats kotlinx.collections.immutable types as stable) can
                        // mark those Models stable/skippable. Exposed as `api` because the types
                        // appear in public Model signatures.
                        api(libs.kotlinx.collections.immutable)
                        implementation(libs.compose.runtime)
                        implementation(libs.compose.foundation)
                        implementation(libs.compose.material.icons.extended)
                        implementation(libs.compose.ui)
                        implementation(libs.compose.components.resources)
                        implementation(libs.compose.ui.tooling.preview)
                        implementation(libs.compose.material.expressive)
                    }

                    androidMain?.dependencies {
                        implementation(libs.compose.ui.tooling.preview)
                        implementation(libs.androidx.activity.compose)
                        // The new com.android.kotlin.multiplatform.library plugin doesn't
                        // expose per-build-type configurations (no `debugImplementation`),
                        // so add the preview-rendering runtime to the main android source
                        // set. It only ships with the Android variant.
                        implementation(compose.uiTooling)
                    }
                    jvmMain?.dependencies {
                        implementation(compose.desktop.currentOs)
                        implementation(libs.kotlinx.coroutinesSwing)
                    }
                }
            }
        }
    }
}
