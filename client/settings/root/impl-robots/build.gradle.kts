plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets { commonMain.dependencies { implementation(projects.client.settings.root.public) } }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.settings.root.robots"
    uiTest = true
}
