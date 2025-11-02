package com.plusmobileapps.chefmate.convention

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import com.plusmobileapps.chefmate.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Create plusMobile extension for configuration
            val plusLibraryExtension = extensions.create<PlusLibraryExtension>("plusLibrary")

            // Add afterEvaluate hook to catch property changes
            afterEvaluate {
                if (plusLibraryExtension.enableDi) {
                    applyKotlinInject()
                }

                if (plusLibraryExtension.enableTesting) {
                    applyTesting()
                }
            }

            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.library")
            }

            target.applyKtlint()

            val androidComponents =
                extensions.getByType(LibraryAndroidComponentsExtension::class.java)

            androidComponents.finalizeDsl {
                extensions.configure<LibraryExtension> {
                    // Use the configured namespace or fall back to the default
                    namespace = plusLibraryExtension.namespace ?: throw IllegalStateException("""
                    Please set the namespace for the module $name in the plusMobile extension in the module's build.gradle.kts file.
                    Example:
                    plusLibrary {
                        namespace = "com.plusmobileapps.chefmate.${project.name}"
                    }
                """.trimIndent())
                    compileSdk = libs.versions.android.compileSdk.get().toInt()

                    compileOptions {
                        sourceCompatibility = JavaVersion.VERSION_11
                        targetCompatibility = JavaVersion.VERSION_11
                    }

                    defaultConfig {
                        minSdk = libs.versions.android.minSdk.get().toInt()
                    }
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
                compilerOptions.optIn.add("kotlin.time.ExperimentalTime")
                
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
                        implementation(libs.kotlin.test)
                    }
                }
            }
        }
    }
}