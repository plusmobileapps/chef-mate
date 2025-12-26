plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.arkivanov.decompose.core)
            api(libs.kotlin.coroutines.core)
            api(projects.client.shared)
            api(projects.client.text.public)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.auth.ui"
}
