plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class) api(compose.uiTest)
            implementation(projects.client.settings.root.public)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.settings.root.robots" }
