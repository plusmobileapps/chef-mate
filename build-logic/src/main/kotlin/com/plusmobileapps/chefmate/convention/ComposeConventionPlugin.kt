package com.plusmobileapps.chefmate.convention

import com.plusmobileapps.chefmate.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
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
