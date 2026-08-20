plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.subscription.data.public)
            api(projects.client.text.public)
            api(projects.client.ui.public)
            api(projects.client.shared)
            api(libs.arkivanov.decompose.core)
            api(libs.kotlin.coroutines.core)
            implementation(compose.components.resources)
        }
    }
}

compose { resources { publicResClass = true } }

plusLibrary { namespace = "com.plusmobileapps.chefmate.subscription" }
