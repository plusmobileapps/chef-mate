plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.family.core.public)
            implementation(projects.client.family.data.public)
            implementation(projects.client.text.public)
            implementation(projects.client.ui.public)
            implementation(projects.client.shared)
            implementation(projects.client.auth.data.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(projects.client.family.data.testing)
            implementation(projects.client.auth.data.testing)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.family.core.impl"
    enableDi = true
    enableTesting = true
}
