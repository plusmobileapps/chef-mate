plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies { implementation(projects.client.notifications.public) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.notifications.robots"
    uiTest = true
}
