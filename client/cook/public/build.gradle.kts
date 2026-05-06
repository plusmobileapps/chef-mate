plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.recipe.data.public)
            api(projects.client.text.public)
            api(libs.arkivanov.decompose.core)
            api(libs.kotlin.coroutines.core)
            api(projects.client.shared)
            api(projects.client.ui.public)
            implementation(libs.arkivanov.decompose.compose.extensions)
            implementation(libs.kotlinx.serialization.json)
            implementation(compose.components.resources)
        }
        commonTest.dependencies { implementation(projects.client.cook.implRobots) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.cook"
    enableComposeUiTest = true
}
