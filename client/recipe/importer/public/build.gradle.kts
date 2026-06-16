plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.arkivanov.decompose.core)
            api(projects.client.recipebook.data.public)
            api(projects.client.text.public)
            implementation(projects.client.shared)
            api(projects.client.ui.public)
            implementation(projects.client.util.public)
            implementation(compose.components.resources)
        }
    }
}

compose { resources { publicResClass = true } }

plusLibrary { namespace = "com.plusmobileapps.chefmate.recipe.importer" }
