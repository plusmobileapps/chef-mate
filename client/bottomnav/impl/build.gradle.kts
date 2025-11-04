plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.bottomnav.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(libs.kotlinx.serialization.json)
            api(projects.client.grocery.core.public)
            api(projects.client.recipe.list.public)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.bottomnav.impl"
    enableDi = true
    enableTesting = true
}
