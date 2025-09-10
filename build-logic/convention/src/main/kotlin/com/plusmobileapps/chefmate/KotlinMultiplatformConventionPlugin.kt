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

                // Configure iOS targets with explicit source set assignment
                val iosArm64Target = iosArm64()
                val iosSimulatorArm64Target = iosSimulatorArm64()

                // Configure framework for iOS
                listOf(iosArm64Target, iosSimulatorArm64Target).forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = target.name.replaceFirstChar { it.uppercase() }
                        isStatic = true
                    }
                }

                // Configure JVM target
                jvm()

                // Default source sets configuration
                sourceSets.apply {
                    val commonMain = getByName("commonMain")
                    val androidMain = getByName("androidMain")
                    val jvmMain = getByName("jvmMain")
                    val iosMain = create("iosMain") {
                    }

                    // Add dependencies to source sets
                    commonMain.dependencies {
                        // Common dependencies can be added here if needed
                    }

                    androidMain.dependencies {
                        // Android-specific dependencies can be added here
                    }

                    jvmMain.dependencies {
                        // JVM-specific dependencies can be added here
                    }

                    iosMain.dependencies {

                    }

                    getByName("commonTest").dependencies {
                        implementation(kotlin("test"))
                    }
                }
            }
        }
    }
}