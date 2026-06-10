plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin { sourceSets { commonMain.dependencies { implementation(projects.client.profile.public) } } }

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.profile.robots"
    uiTest = true
}
