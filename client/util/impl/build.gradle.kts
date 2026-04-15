plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.util.public)
            implementation(projects.client.shared)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.util.impl"
    enableDi = true
    enableTesting = true
}
