plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.recipe.data.public)
            implementation(libs.arkivanov.decompose.compose.extensions)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            api(libs.kotlinx.serialization.json)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.core"
}
