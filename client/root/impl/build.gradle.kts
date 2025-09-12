plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.plusTesting)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.root.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(projects.client.grocery.public)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.root.impl"
    enableDi = true
}
