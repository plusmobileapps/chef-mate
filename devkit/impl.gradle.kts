plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation($)
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
