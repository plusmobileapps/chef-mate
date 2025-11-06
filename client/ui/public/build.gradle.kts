plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.text.public)
            api(libs.arkivanov.decompose.core)
            api(libs.arkivanov.decompose.compose.extensions)
            api(libs.compose.material.expressive)
            implementation(compose.components.resources)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.ui"
}
