plugins {
    `kotlin-dsl`
}

group = "com.plusmobileapps.chefmate.buildlogic"

// Add this block to expose your plugin to the main build
gradlePlugin {
    plugins {
        create("kotlinInject") {
            id = "com.plusmobileapps.chefmate.kotlin-inject"
            implementationClass = "com.plusmobileapps.chefmate.convention.KotlinInjectConventionPlugin"
        }
        create("kmpLibrary") {
            id = "com.plusmobileapps.chefmate.kmp-library"
            implementationClass = "com.plusmobileapps.chefmate.convention.KmpLibraryConventionPlugin"
        }
        create("compose") {
            id = "com.plusmobileapps.chefmate.compose"
            implementationClass = "com.plusmobileapps.chefmate.convention.ComposeConventionPlugin"
        }

        create("ktlint") {
            id = "com.plusmobileapps.chefmate.ktlint"
            implementationClass = "com.plusmobileapps.chefmate.convention.KtlintConventionPlugin"
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
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.compose.multiplatform.gradle.plugin)
    compileOnly(libs.ktlint.gradle.plugin)

    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${libs.versions.ksp.get()}")
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}