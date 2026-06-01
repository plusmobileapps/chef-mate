plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies { implementation(projects.client.recipe.exporter.public) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.exporter.robots"
    uiTest = true
}
