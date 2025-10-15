plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.arkivanov.decompose.core)
            api(libs.kotlin.coroutines.core)
            implementation(projects.client.shared)
            implementation(projects.client.recipe.data.public)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.list"
    enableDi = true
    enableTesting = true
}
