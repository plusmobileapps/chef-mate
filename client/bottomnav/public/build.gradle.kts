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
            implementation(projects.client.recipe.list.public)
            implementation(projects.client.grocery.public)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.bottomnav"
}
