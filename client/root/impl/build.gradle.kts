plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.root.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(projects.client.bottomnav.public)
            implementation(projects.client.grocery.core.public)
            implementation(projects.client.recipe.core.public)
            implementation(projects.client.auth.ui.public)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.root.impl"
    enableDi = true
    enableTesting = true
}
