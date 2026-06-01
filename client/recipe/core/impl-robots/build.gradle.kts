plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets { commonMain.dependencies { implementation(projects.client.recipe.core.public) } }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.core.robots"
    uiTest = true
}
