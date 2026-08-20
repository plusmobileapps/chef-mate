plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.shared)
            api(projects.client.recipe.data.public)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.profile.data" }
