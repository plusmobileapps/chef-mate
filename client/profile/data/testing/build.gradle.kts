plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.profile.data.public)
            implementation(projects.client.shared)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.profile.data.testing" }
