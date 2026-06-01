plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets { commonMain.dependencies { implementation(projects.client.recipe.list.public) } }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.list.robots"
    uiTest = true
}
