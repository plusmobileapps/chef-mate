plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.grocery.data.public)
            implementation(projects.client.grocery.core.public)
            implementation(projects.client.shared)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.database.core)
        }
        commonTest.dependencies { implementation(libs.multiplatform.settings.test) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.grocery.core.impl"
    enableDi = true
    enableTesting = true
}
