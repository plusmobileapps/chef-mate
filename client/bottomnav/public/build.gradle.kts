plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.arkivanov.decompose.compose.extensions)
            implementation(projects.client.shared)
            api(projects.client.grocery.public)
            implementation(compose.components.resources)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.bottomnav"
}
