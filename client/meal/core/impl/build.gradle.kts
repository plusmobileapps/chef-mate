plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.meal.core.public)
            implementation(projects.client.meal.data.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(projects.client.util.public)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.meal.core.impl"
    enableDi = true
    enableTesting = true
}
