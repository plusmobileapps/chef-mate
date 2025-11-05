plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.grocery.data.public)
            implementation(projects.client.grocery.core.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.database)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.grocery.core.impl"
    enableDi = true
    enableTesting = true
}
