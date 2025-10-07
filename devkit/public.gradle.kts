plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
        }
    }
}

plusLibrary {
    namespace = "$namespace"
    enableDi = true
    enableTesting = true
}
