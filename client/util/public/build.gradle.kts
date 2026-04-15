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
        }

        androidMain.dependencies {
            implementation(libs.androidx.annotation)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.util"
}
