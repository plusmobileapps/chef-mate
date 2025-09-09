package com.plusmobileapps.chefmate

import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.library")
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.configure<LibraryExtension> {
                namespace = "com.plusmobileapps.chefmate.${project.name}"
                compileSdk = libs.findVersion("android.compileSdk").get().toString().toInt()

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_11
                    targetCompatibility = JavaVersion.VERSION_11
                }

                defaultConfig {
                    minSdk = libs.findVersion("android.minSdk").get().toString().toInt()
                }
            }

            extensions.configure<KotlinMultiplatformExtension> {
                // Configure Android target
                androidTarget {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_11)
                    }
                }

                // Configure iOS targets
                listOf(
                    iosArm64(),
                    iosSimulatorArm64()
                ).forEach { iosTarget ->
                    // iOS-specific configurations can be added here if needed
                    iosTarget.binaries.framework {
                        baseName = target.name.replaceFirstChar { it.uppercase() }
                        isStatic = true
                    }
                }

                // Configure JVM target
                jvm()

                // Default source sets configuration
                sourceSets.apply {
                    getByName("commonMain").dependencies {
                        // Common dependencies can be added here if needed
                    }

                    getByName("commonTest").dependencies {
                        implementation(kotlin("test"))
                    }

                    getByName("androidMain").dependencies {
                        // Android-specific dependencies can be added here
                    }

                    val iosMain = create("iosMain")
                    iosMain.dependsOn(getByName("commonMain"))

                    getByName("jvmMain").dependencies {
                        // JVM-specific dependencies can be added here
                    }
                }
            }
        }
    }
}