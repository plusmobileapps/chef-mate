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
                implementation(libs.kotest.assertions)
                implementation(project(":client:testing"))
            }
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}
