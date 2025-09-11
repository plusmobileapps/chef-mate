package com.plusmobileapps.chefmate.convention

import com.plusmobileapps.chefmate.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class TestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.applyTesting()
    }
}
fun Project.applyTesting() {
    pluginManager.apply(libs.plugins.kotest.get().pluginId)
    pluginManager.apply(libs.plugins.mokkery.get().pluginId)

    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.apply {
            val commonTest = getByName("commonTest")
            commonTest.dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlin.coroutines.test)
                implementation(libs.turbine)
                implementation(libs.kotest.framework)
                implementation(libs.kotest.assertions)
                implementation(libs.kotest.properties)
                implementation(project(":client:testing"))
            }
        }
    }
}