plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.recipe.data.public)
            implementation(projects.client.shared)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.recipebook.data" }
