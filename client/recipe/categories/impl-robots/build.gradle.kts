plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies { implementation(projects.client.recipe.categories.public) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.categories.robots"
    uiTest = true
}
