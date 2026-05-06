plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.cook.public)
            api(projects.client.ui.robots)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.cook.robots" }
