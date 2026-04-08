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
}
