plugins { `kotlin-dsl` }

group = "com.plusmobileapps.chefmate.buildlogic"

// Add this block to expose your plugin to the main build
gradlePlugin {
    plugins {
        create("metro") {
            id = "com.plusmobileapps.chefmate.metro"
            implementationClass = "com.plusmobileapps.chefmate.convention.MetroConventionPlugin"
        }
        create("kmpLibrary") {
            id = "com.plusmobileapps.chefmate.kmp-library"
            implementationClass =
                "com.plusmobileapps.chefmate.convention.KmpLibraryConventionPlugin"
        }
        create("compose") {
            id = "com.plusmobileapps.chefmate.compose"
            implementationClass = "com.plusmobileapps.chefmate.convention.ComposeConventionPlugin"
        }
        create("plusApplication") {
            id = "com.plusmobileapps.chefmate.application"
            implementationClass =
                "com.plusmobileapps.chefmate.convention.PlusApplicationConventionPlugin"
        }

        create("ktfmt") {
            id = "com.plusmobileapps.chefmate.ktfmt"
            implementationClass = "com.plusmobileapps.chefmate.convention.KtfmtConventionPlugin"
        }

        create("testing") {
            id = "com.plusmobileapps.chefmate.testing"
            implementationClass = "com.plusmobileapps.chefmate.convention.TestingConventionPlugin"
        }
    }
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.kotlin.compose.compiler.gradle.plugin)
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.android.kmp.library.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.compose.multiplatform.gradle.plugin)
    compileOnly(libs.ktfmt.gradle.plugin)

    implementation(
        "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${libs.versions.ksp.get()}"
    )
    // Metro Gradle plugin for DI - loaded at runtime, not compiled against
    runtimeOnly("dev.zacsweers.metro:gradle-plugin:${libs.versions.metro.get()}")
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
