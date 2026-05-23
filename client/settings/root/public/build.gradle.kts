plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.arkivanov.decompose.core)
            api(libs.arkivanov.decompose.compose.extensions)
            api(projects.client.settings.public)
            api(projects.client.bottomnav.public)
            implementation(projects.client.shared)
            implementation(projects.client.ui.public)
            implementation(compose.components.resources)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.settings.root" }
