plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.recipe.data.public)
            implementation(projects.client.recipe.core.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.core.impl"
    enableDi = true
    enableTesting = true
}
