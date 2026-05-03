plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.settings.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(projects.client.auth.data.public)
            implementation(projects.client.browser.public)
            implementation(projects.client.grocery.data.public)
            implementation(projects.client.recipe.data.public)
        }
        commonTest.dependencies {
            implementation(projects.client.auth.data.testing)
            implementation(projects.client.grocery.data.testing)
            implementation(projects.client.recipe.data.testing)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.settings.impl"
    enableDi = true
    enableTesting = true
}
