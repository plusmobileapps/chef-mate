plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies { implementation(projects.client.recipebook.edit.public) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipebook.edit.robots"
    uiTest = true
}
