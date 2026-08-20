plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets { commonMain.dependencies { implementation(projects.client.family.core.public) } }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.family.robots"
    uiTest = true
}
