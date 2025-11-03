plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.coroutines.core)
            api(libs.arkivanov.decompose.core)
            api(projects.client.shared)
            implementation(compose.components.resources)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.list"
}
