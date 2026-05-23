plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.settings.root.public)
            implementation(projects.client.settings.public)
            implementation(projects.client.bottomnav.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.settings.root.impl"
    enableDi = true
    enableTesting = true
}
