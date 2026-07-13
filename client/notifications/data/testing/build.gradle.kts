plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.notifications.data.public)
            implementation(projects.client.shared)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.notifications.data.testing" }
