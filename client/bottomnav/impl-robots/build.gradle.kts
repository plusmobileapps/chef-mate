plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets { commonMain.dependencies { implementation(projects.client.bottomnav.public) } }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.bottomnav.robots"
    uiTest = true
}
