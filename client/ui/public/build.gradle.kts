plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.screenshot)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.text.public)
            api(libs.arkivanov.decompose.core)
            api(libs.arkivanov.decompose.compose.extensions)
            api(libs.compose.material.expressive)
            api(libs.coil.compose)
            api(libs.coil.network.ktor3)
            implementation(compose.components.resources)
        }
    }
}

compose { resources { publicResClass = true } }

android { experimentalProperties["android.experimental.enableScreenshotTest"] = true }

dependencies {
    screenshotTestImplementation(libs.screenshot.validation.api)
    screenshotTestImplementation(libs.androidx.compose.ui.tooling)
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.ui" }
