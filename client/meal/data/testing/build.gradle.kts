plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.meal.data.public)
            implementation(projects.client.shared)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.meal.data.testing" }
