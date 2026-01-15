plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.settings.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(projects.client.auth.data.public)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.settings.impl"
    enableDi = true
    enableTesting = true
}
