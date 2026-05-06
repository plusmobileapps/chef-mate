plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class) api(compose.uiTest)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.ui.robots" }
