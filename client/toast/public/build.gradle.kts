plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.text.public)
            api(projects.client.ui.public)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.toast" }
