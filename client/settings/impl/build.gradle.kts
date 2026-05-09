plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.settings.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(projects.client.auth.data.public)
            implementation(projects.client.auth.usecase.public)
            implementation(projects.client.browser.public)
        }
        commonTest.dependencies {
            implementation(projects.client.auth.data.testing)
            implementation(projects.client.browser.testing)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.settings.impl"
    enableDi = true
    enableTesting = true
}
