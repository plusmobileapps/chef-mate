package com.plusmobileapps.chefmate.convention

import com.plusmobileapps.chefmate.libs
import org.gradle.api.Plugin
import org.gradle.api.Project

class MetroConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.applyMetro()
    }
}

fun Project.applyMetro() {
    pluginManager.apply(libs.plugins.metro.get().pluginId)
    pluginManager.apply(libs.plugins.ksp.get().pluginId)

    dependencies.apply {
        add("commonMainImplementation", libs.metroExtensions.assistedFactory.runtime)
        // Only wire the KSP configurations that actually exist for this module's targets.
        // KMP libraries have all four; an Android + JVM-only demo app (plusApplication) has
        // just kspAndroid/kspJvm, so guard against the missing iOS configurations.
        listOf("kspAndroid", "kspJvm", "kspIosArm64", "kspIosSimulatorArm64").forEach { config ->
            if (configurations.findByName(config) != null) {
                add(config, libs.metroExtensions.assistedFactory.compiler)
            }
        }
    }
}
