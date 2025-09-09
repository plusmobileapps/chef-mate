package com.plusmobileapps.chefmate

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

class KotlinInjectConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            // Apply KSP plugin first
            pluginManager.apply("com.google.devtools.ksp")

            // Configure Kotlin Multiplatform
            extensions.configure<KotlinMultiplatformExtension> {

                androidTarget {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_11)
                    }
                }

                iosArm64()
                iosSimulatorArm64()

                jvm()

                // Configure source sets
                sourceSets.apply {
                    getByName("commonMain") {
                        dependencies {
                            implementation(libs.findLibrary("kotlininject-core-runtime").get())
                            implementation(libs.findLibrary("kotlininject-anvil-runtime").get())
                            implementation(
                                libs.findLibrary("kotlininject-anvil-runtime-optional").get()
                            )
                        }
                    }
                }

                // Apply KSP configuration
                configureCommonMainKsp()
            }

            // Add dependencies after KSP plugin is applied
            dependencies {
                val targets = listOf(
                    "kspAndroid",
                    "kspIosArm64",
                    "kspIosSimulatorArm64",
                    "kspJvm"
                )

                // kotlin-inject
                add("kspCommonMainMetadata", libs.findLibrary("kotlininject-core-compiler").get())
                add("commonMainImplementation", libs.findLibrary("kotlininject-core-runtime").get())
                targets.forEach {
                    add(it, libs.findLibrary("kotlininject-core-compiler").get())
                }

                // kotlin-inject-anvil
                add(
                    "commonMainImplementation",
                    libs.findLibrary("kotlininject-anvil-runtime").get()
                )
                add(
                    "commonMainImplementation",
                    libs.findLibrary("kotlininject-anvil-runtime-optional").get()
                )
                targets.forEach {
                    add(it, libs.findLibrary("kotlininject-anvil-compiler").get())
                }

                // kotlin-inject-anvil-extensions
                add(
                    "commonMainImplementation",
                    libs.findLibrary("kotlininject-anvil-extensions-assisted-factory-runtime").get()
                )
                targets.forEach {
                    add(
                        it,
                        libs.findLibrary("kotlininject-anvil-extensions-assisted-factory-compiler")
                            .get()
                    )
                }
            }
        }
    }

    // Extension function to configure KSP for commonMain
    private fun KotlinMultiplatformExtension.configureCommonMainKsp() {
        sourceSets.named("commonMain").configure {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }

        project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
            if (name != "kspCommonMainKotlinMetadata") {
                dependsOn("kspCommonMainKotlinMetadata")
            }
        }

        // Add explicit dependency for KSP Android tasks
        project.tasks.matching { it.name.startsWith("ksp") && it.name.contains("Android") }
            .configureEach {
                dependsOn("kspCommonMainKotlinMetadata")
            }

        // Add explicit dependency for KSP iOS tasks
        project.tasks.matching { it.name.startsWith("ksp") && it.name.contains("Ios") }
            .configureEach {
                dependsOn("kspCommonMainKotlinMetadata")
            }

        // Add explicit dependency for KSP JVM tasks
        project.tasks.matching { it.name.startsWith("ksp") && it.name.contains("Jvm") }
            .configureEach {
                dependsOn("kspCommonMainKotlinMetadata")
            }
    }
}