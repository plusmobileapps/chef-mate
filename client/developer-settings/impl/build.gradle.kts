plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.developerSettings.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(projects.client.auth.data.public)
            implementation(projects.client.auth.usecase.public)
            implementation(projects.client.recipe.data.public)
        }
        commonTest.dependencies { implementation(projects.client.auth.data.testing) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.devsettings.impl"
    enableDi = true
    enableTesting = true
}
