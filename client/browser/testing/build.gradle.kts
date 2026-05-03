plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies { implementation(projects.client.browser.public) }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.browser.testing" }
