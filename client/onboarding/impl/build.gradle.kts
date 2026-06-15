plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.onboarding.public)
            implementation(projects.client.text.public)
            implementation(projects.client.ui.public)
            implementation(projects.client.shared)
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.arkivanov.decompose.compose.extensions)
            implementation(compose.components.resources)
        }
        commonTest.dependencies { implementation(libs.multiplatform.settings.test) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.onboarding.impl"
    enableDi = true
    enableTesting = true
}
