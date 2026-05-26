plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.auth.data.public)
            implementation(projects.client.auth.ui.public)
            implementation(projects.client.auth.usecase.public)
            implementation(projects.client.util.public)
            implementation(projects.client.text.public)
            implementation(projects.client.ui.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.arkivanov.decompose.compose.extensions)
            implementation(projects.client.shared)
            implementation(compose.components.resources)
        }
        commonTest.dependencies { implementation(projects.client.auth.data.testing) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.auth.ui.impl"
    enableDi = true
    enableTesting = true
}
