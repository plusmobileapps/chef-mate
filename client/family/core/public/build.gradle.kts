plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.arkivanov.decompose.core)
            api(projects.client.text.public)
            api(projects.client.ui.public)
            api(projects.client.family.data.public)
            implementation(projects.client.shared)
            implementation(projects.client.util.public)
            implementation(compose.components.resources)
        }
    }
}

compose { resources { publicResClass = true } }

plusLibrary { namespace = "com.plusmobileapps.chefmate.family.core" }
