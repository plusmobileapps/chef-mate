plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.grocery.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.database)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.grocery.impl"
    enableDi = true
    enableTesting = true
}
