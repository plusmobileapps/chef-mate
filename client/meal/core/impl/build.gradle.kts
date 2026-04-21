plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.meal.core.public)
            implementation(projects.client.meal.data.public)
            implementation(projects.client.recipe.core.public)
            implementation(projects.client.recipe.data.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(projects.client.shared)
            implementation(projects.client.text.public)
            implementation(projects.client.util.public)
        }
        commonTest.dependencies { implementation(projects.client.util.testing) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.meal.core.impl"
    enableDi = true
    enableTesting = true
}
