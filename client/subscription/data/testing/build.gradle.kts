plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies { implementation(projects.client.subscription.data.public) }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.subscription.data.testing" }
