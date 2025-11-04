plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.recipe.data.public)
            implementation(projects.client.recipe.core.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(libs.kotlinx.serialization.json)
            implementation(projects.client.text.public)
            implementation(projects.client.grocery.data.public)
            implementation(projects.client.util.public)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.core.impl"
    enableDi = true
    enableTesting = true
}
