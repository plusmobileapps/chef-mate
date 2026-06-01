plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies { implementation(projects.client.recipe.importer.public) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.importer.robots"
    uiTest = true
}
