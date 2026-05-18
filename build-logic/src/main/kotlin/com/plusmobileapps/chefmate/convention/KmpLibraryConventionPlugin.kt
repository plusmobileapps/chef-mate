package com.plusmobileapps.chefmate.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.plusmobileapps.chefmate.libs
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
                    applyMetro()
                }

                if (plusLibraryExtension.enableTesting) {
                    applyTesting(enableDatabaseTesting = plusLibraryExtension.enableDatabaseTesting)
                }
            }

            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                // AGP 9: the new KMP-aware Android library plugin replaces
                // `com.android.library` for multiplatform modules. It registers the
                // Android target itself and exposes its DSL inside `kotlin { }`
                // under the `android` extension.
                apply("com.android.kotlin.multiplatform.library")
            }

            target.applyKtfmt()

            // Defer namespace resolution until after the module's build.gradle.kts has
            // evaluated. compileSdk, minSdk, withHostTest, and jvmTarget don't depend on
            // user input and are safe to set eagerly, but `namespace` requires the
            // per-module value from `plusLibrary { ... }`, which isn't available yet.
            val androidComponents =
                extensions.getByType(KotlinMultiplatformAndroidComponentsExtension::class.java)
            androidComponents.finalizeDsl { extension ->
                extension.namespace =
                    plusLibraryExtension.namespace
                        ?: throw IllegalStateException(
                            """
                    Please set the namespace for the module $name in the plusMobile extension in the module's build.gradle.kts file.
                    Example:
                    plusLibrary {
                        namespace = "com.plusmobileapps.chefmate.${project.name}"
                    }
                """
                                .trimIndent()
                        )
            }

            extensions.configure<KotlinMultiplatformExtension> {
                // Configure the Android library target via the new plugin's DSL,
                // which is exposed on the Kotlin multiplatform extension as `android`.
                extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                    compileSdk = libs.versions.android.compileSdk.get().toInt()
                    minSdk = libs.versions.android.minSdk.get().toInt()

                    // Pin Android JVM bytecode level. iOS targets are unaffected.
                    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)

                    // Compose Multiplatform's resources system (strings.xml in
                    // commonMain/composeResources/) bundles its compiled `.cvr` files into
                    // the Android AAR via the Android asset/resource pipeline. The new
                    // KMP plugin defaults `androidResources.enable` to `false` (vs `true`
                    // for plain `com.android.library`), which silently drops those
                    // resources and makes `stringResource(...)` throw MissingResourceException
                    // at runtime. Re-enable it so consumers can resolve common strings.
                    androidResources.enable = true

                    // Tests that previously lived in `androidUnitTest` now live in
                    // `androidHostTest` (host-side JVM tests); instrumented tests live in
                    // `androidDeviceTest`. Opt into the host-test compilation so existing
                    // test source sets continue to compile.
                    withHostTest {}
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
                jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }

                // Remove once kotlin.time is stable in 2.3.0
                compilerOptions.optIn.add("kotlin.time.ExperimentalTime")

                // Default source sets configuration
                sourceSets.apply {
                    val commonMain = getByName("commonMain")
                    val androidMain = getByName("androidMain")
                    val jvmMain = getByName("jvmMain")
                    val iosMain = create("iosMain") {}

                    // Add dependencies to source sets
                    commonMain.dependencies {
                        // Common dependencies can be added here if needed
                        implementation(libs.kermit)
                    }

                    androidMain.dependencies {
                        // Android-specific dependencies can be added here
                    }

                    jvmMain.dependencies {
                        // JVM-specific dependencies can be added here
                    }

                    iosMain.dependencies {}

                    getByName("commonTest").dependencies { implementation(libs.kotlin.test) }
                }
            }
        }
    }
}
