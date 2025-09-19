plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.plusTesting)
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
}
