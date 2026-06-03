plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.recipebook.data.public)
            implementation(projects.client.shared)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.recipebook.data.testing" }
