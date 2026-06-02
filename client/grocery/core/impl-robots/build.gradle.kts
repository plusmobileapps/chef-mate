plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets { commonMain.dependencies { implementation(projects.client.grocery.core.public) } }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.grocery.core.robots"
    uiTest = true
}
