plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.notifications.public)
            implementation(projects.client.notifications.data.public)
            implementation(projects.client.text.public)
            implementation(projects.client.ui.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(projects.client.auth.data.public)
            implementation(projects.client.toast.public)
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(projects.client.notifications.data.testing)
            implementation(projects.client.auth.data.testing)
            implementation(projects.client.toast.testing)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.notifications.impl"
    enableDi = true
    enableTesting = true
}
