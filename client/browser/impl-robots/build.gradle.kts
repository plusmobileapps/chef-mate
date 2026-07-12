plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets { commonMain.dependencies { implementation(projects.client.browser.public) } }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.browser.robots"
    uiTest = true
}
