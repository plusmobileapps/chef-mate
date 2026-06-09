plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.profile.public)
            implementation(projects.client.text.public)
            implementation(projects.client.ui.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.arkivanov.decompose.compose.extensions)
            implementation(projects.client.shared)
            implementation(projects.client.auth.data.public)
            implementation(projects.client.auth.usecase.public)
            implementation(compose.components.resources)
        }
        commonTest.dependencies { implementation(projects.client.auth.data.testing) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.profile.impl"
    enableDi = true
    enableTesting = true
}
