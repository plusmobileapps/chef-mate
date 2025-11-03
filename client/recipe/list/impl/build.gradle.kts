plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.recipe.list.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(projects.client.recipe.data.public)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.list.impl"
    enableDi = true
    enableTesting = true
}
