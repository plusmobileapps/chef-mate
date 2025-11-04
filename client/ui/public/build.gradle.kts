plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.arkivanov.decompose.core)
            api(libs.arkivanov.decompose.compose.extensions)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.ui"
}
