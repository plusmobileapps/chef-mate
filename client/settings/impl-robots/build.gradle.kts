plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets { commonMain.dependencies { implementation(projects.client.settings.public) } }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.settings.robots"
    uiTest = true
}
