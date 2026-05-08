import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
            api(projects.client.text.public)
            implementation(compose.components.resources)
            implementation(compose.ui)
        }

        androidMain.dependencies {
            implementation(libs.androidx.annotation)
            implementation(libs.androidx.activity.compose)
        }
    }
}

compose { resources { publicResClass = true } }

plusLibrary { namespace = "com.plusmobileapps.chefmate.util" }
