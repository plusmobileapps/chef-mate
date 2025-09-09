plugins {
    `kotlin-dsl`
}

group = "com.plusmobileapps.chefmate.buildlogic"

// Add this block to expose your plugin to the main build
gradlePlugin {
    plugins {
        create("kotlinInject") {
            id = "com.plusmobileapps.chefmate.kotlin-inject"
            implementationClass = "com.plusmobileapps.chefmate.KotlinInjectConventionPlugin"
        }
    }
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.android.gradle.plugin)
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${libs.versions.ksp.get()}")
}