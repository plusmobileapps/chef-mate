package com.plusmobileapps.chefmate.convention

import com.plusmobileapps.chefmate.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin

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

            kotlin.sourceSets.apply {
                val commonMain = getByName("commonMain")
                val androidMain = getByName("androidMain")
                val jvmMain = getByName("jvmMain")
                val compose = ComposePlugin.Dependencies(project)

                commonMain.dependencies {
                    implementation(compose.runtime)
                    implementation(compose.foundation)
                    implementation(compose.materialIconsExtended)
                    implementation(compose.ui)
                    implementation(compose.components.resources)
                    implementation(compose.components.uiToolingPreview)
                    implementation(libs.compose.material.expressive)
                }

                androidMain.dependencies {
                    implementation(compose.preview)
                    implementation(libs.androidx.activity.compose)
                }
                jvmMain.dependencies {
                    implementation(compose.desktop.currentOs)
                    implementation(libs.kotlinx.coroutinesSwing)
                }
            }

            // Add debug-specific dependencies
            dependencies {
                val compose = ComposePlugin.Dependencies(project)
                "debugImplementation"(compose.uiTooling)
            }
        }
    }
}
