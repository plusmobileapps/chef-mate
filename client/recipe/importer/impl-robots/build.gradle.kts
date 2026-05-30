plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class) api(compose.uiTest)
            implementation(projects.client.recipe.importer.public)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.recipe.importer.robots" }
