plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets { commonMain.dependencies { implementation(projects.client.onboarding.public) } }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.onboarding.robots"
    uiTest = true
}
