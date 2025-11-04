plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.database)
            implementation(projects.client.grocery.data.public)
            implementation(projects.client.shared)
            implementation(projects.client.util.public)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.grocery.data.impl"
    enableDi = true
    enableTesting = true
}
