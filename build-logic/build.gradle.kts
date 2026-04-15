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

// Force kotlin-stdlib to match the Kotlin version embedded in Gradle so that
// plugin dependencies (which may transitively pull in a newer stdlib) don't
// cause "incompatible metadata version" errors during build-logic compilation.
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (
            requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin-stdlib")
        ) {
            useVersion(embeddedKotlinVersion)
        }
    }
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.gradle.plugin)
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
