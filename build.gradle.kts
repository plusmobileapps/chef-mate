plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktfmt) apply false
    alias(libs.plugins.mokkery) apply false
    alias(libs.plugins.kover)
}

val coveredProjects =
    listOf(
        ":client:browser:impl",
        ":client:grocery:core:impl",
        ":client:grocery:data:public",
        ":client:meal:core:impl",
        ":client:recipe:core:impl",
        ":client:recipe:list:impl",
        ":client:root:impl",
    )

subprojects {
    if (path in coveredProjects) {
        apply(plugin = "org.jetbrains.kotlinx.kover")
    }
}

dependencies { coveredProjects.forEach { kover(project(it)) } }
