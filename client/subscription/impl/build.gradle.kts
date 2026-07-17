plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.subscription.public)
            implementation(projects.client.subscription.data.public)
            implementation(projects.client.text.public)
            implementation(projects.client.ui.public)
            implementation(projects.client.shared)
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.arkivanov.decompose.compose.extensions)
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(projects.client.subscription.data.testing)
            implementation(libs.kotlin.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.subscription.impl"
    enableDi = true
    enableTesting = true
}
