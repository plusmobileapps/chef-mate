package com.plusmobileapps.chefmate.convention

import com.plusmobileapps.chefmate.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.ExperimentalComposeLibrary
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

            afterEvaluate {
                kotlin.sourceSets.apply {
                    val commonMain = getByName("commonMain")
                    val androidMain = findByName("androidMain")
                    val jvmMain = findByName("jvmMain")
                    val commonTest = findByName("commonTest")
                    val jvmTest = findByName("jvmTest")
                    val compose = ComposePlugin.Dependencies(project)

                    commonMain.dependencies {
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
                    }
                    jvmMain?.dependencies {
                        implementation(compose.desktop.currentOs)
                        implementation(libs.kotlinx.coroutinesSwing)
                    }

                    @OptIn(ExperimentalComposeLibrary::class)
                    commonTest?.dependencies { implementation(compose.uiTest) }
                    jvmTest?.dependencies { implementation(compose.desktop.uiTestJUnit4) }
                }

                // Add debug-specific dependencies
                dependencies {
                    val compose = ComposePlugin.Dependencies(project)
                    "debugImplementation"(compose.uiTooling)
                }
            }
        }
    }
}
