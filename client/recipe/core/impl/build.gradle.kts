plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.recipe.core.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.core.impl"
    enableDi = true
    enableTesting = true
}
