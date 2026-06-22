plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.toast.public)
            implementation(projects.client.text.public)
            implementation(projects.client.ui.public)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.toast.testing" }
