plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.recipe.data.public)
            api(projects.client.text.public)
            api(libs.kotlinx.serialization.json)

            implementation(projects.client.shared)
            implementation(projects.client.ui.public)
            implementation(libs.arkivanov.decompose.compose.extensions)
            implementation(libs.arkivanov.decompose.core)
            implementation(compose.components.resources)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.core"
}
