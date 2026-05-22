plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class) api(compose.uiTest)
            implementation(projects.client.grocery.core.public)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.grocery.core.robots" }
