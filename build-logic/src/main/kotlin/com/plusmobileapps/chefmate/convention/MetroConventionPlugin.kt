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
        listOf("kspAndroid", "kspJvm", "kspIosArm64", "kspIosSimulatorArm64").forEach { config ->
            add(config, libs.metroExtensions.assistedFactory.compiler)
        }
    }
}
