plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.recipe.categories.public)
            implementation(projects.client.recipe.data.public)
            implementation(projects.client.text.public)
            implementation(projects.client.ui.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.arkivanov.decompose.compose.extensions)
            implementation(projects.client.shared)
            implementation(compose.components.resources)
        }
        commonTest.dependencies { implementation(projects.client.recipe.data.testing) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.categories.impl"
    enableDi = true
    enableTesting = true
}
