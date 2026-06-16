package com.plusmobileapps.chefmate.convention

import com.plusmobileapps.chefmate.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.project

class MetroConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.applyMetro()
    }
}

fun Project.applyMetro() {
    pluginManager.apply(libs.plugins.metro.get().pluginId)
    pluginManager.apply(libs.plugins.ksp.get().pluginId)

    dependencies.apply {
        // The published `assisted-factory-runtime` has no wasmJs variant, so the annotation comes
        // from the in-repo `:client:metro-assisted-factory-runtime` module (same FQN, all targets).
        // The KSP compiler is a JVM processor and works for every target, including kspWasmJs.
        add(
            "commonMainImplementation",
            project.dependencies.project(":client:metro-assisted-factory-runtime"),
        )
        listOf("kspAndroid", "kspJvm", "kspIosArm64", "kspIosSimulatorArm64", "kspWasmJs")
            .forEach { config ->
                add(config, libs.metroExtensions.assistedFactory.compiler)
            }
    }
}
