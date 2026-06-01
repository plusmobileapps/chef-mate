plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin { sourceSets { commonMain.dependencies { implementation(projects.client.aichat.public) } } }

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.aichat.robots"
    uiTest = true
}
