plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation($projectDirectory)
            implementation(projects.client.shared)
        }
    }
}

plusLibrary {
    namespace = "$namespace"
}
