package com.plusmobileapps.chefmate.convention

import com.plusmobileapps.chefmate.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class TestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.applyTesting()
    }
}
fun Project.applyTesting() {
    pluginManager.apply(libs.plugins.ksp.get().pluginId)
    pluginManager.apply(libs.plugins.kotest.get().pluginId)
    pluginManager.apply(libs.plugins.mokkery.get().pluginId)

    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.apply {
            val commonTest = getByName("commonTest")
            val jvmTest = getByName("jvmTest")
            val androidUnitTest = getByName("androidUnitTest")
            val iosMain = getByName("iosMain")

            commonTest.dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlin.coroutines.test)
                implementation(libs.turbine)
                implementation(libs.kotest.framework)
                implementation(libs.kotest.assertions)
                implementation(libs.kotest.properties)
                implementation(project(":client:testing"))
            }
            jvmTest.dependencies {
                implementation(libs.kotest.runner.junit5)
            }
            androidUnitTest.dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }
    }

    // Configure all Test tasks to use JUnit 5
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        filter {
            isFailOnNoMatchingTests = false
        }

        // Ensure tests are always run
        outputs.upToDateWhen { false }
    }
}
