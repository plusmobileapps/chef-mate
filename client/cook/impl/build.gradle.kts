plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.cook.public)
            implementation(projects.client.recipe.data.public)
            implementation(projects.client.shared)
            implementation(projects.client.database.core)
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.multiplatform.settings)
        }
        commonTest.dependencies { implementation(libs.multiplatform.settings.test) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.cook.impl"
    enableDi = true
    enableTesting = true
}
