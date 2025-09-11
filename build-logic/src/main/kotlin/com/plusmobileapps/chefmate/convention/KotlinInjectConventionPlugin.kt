package com.plusmobileapps.chefmate.convention

import com.google.devtools.ksp.gradle.KspAATask
import com.plusmobileapps.chefmate.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KotlinInjectConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.applyKotlinInject()
    }
}

fun Project.applyKotlinInject() {
    with(this) {
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
                    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
                    dependencies {
                        implementation(libs.kotlininject.core.runtime)
                        implementation(libs.kotlininject.anvil.runtime)
                        implementation(libs.kotlininject.anvil.runtime.optional)
                    }
                }
            }
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
            add("kspCommonMainMetadata", libs.kotlininject.core.compiler)
            add("commonMainImplementation", libs.kotlininject.core.runtime)
            targets.forEach {
                add(it, libs.kotlininject.core.compiler)
            }

            // kotlin-inject-anvil
            add("commonMainImplementation", libs.kotlininject.anvil.runtime)
            add("commonMainImplementation", libs.kotlininject.anvil.runtime.optional)
            targets.forEach {
                add(it, libs.kotlininject.anvil.compiler)
            }

            // kotlin-inject-anvil-extensions
            add(
                "commonMainImplementation",
                libs.kotlininject.anvil.extensions.assisted.factory.runtime
            )
            targets.forEach {
                add(
                    it,
                    libs.kotlininject.anvil.extensions.assisted.factory.compiler
                )
            }
        }

        tasks.withType<KspAATask>().configureEach {
            if (name != "kspCommonMainKotlinMetadata") {
                dependsOn("kspCommonMainKotlinMetadata")
            }
        }
    }
}