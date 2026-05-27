plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.recipe.data.public)
            api(projects.client.text.public)
            api(libs.arkivanov.decompose.core)
            api(libs.kotlin.coroutines.core)
            api(projects.client.shared)
            api(projects.client.ui.public)
            implementation(compose.components.resources)
        }
    }
}

compose { resources { publicResClass = true } }

plusLibrary { namespace = "com.plusmobileapps.chefmate.aichat" }
