plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.arkivanov.decompose.core)
            api(projects.client.shared)
            api(projects.client.text.public)
            implementation(projects.client.ui.public)
            implementation(compose.components.resources)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.devsettings" }
