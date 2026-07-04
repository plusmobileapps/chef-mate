plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.grocery.autocomplete.public)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.grocery.autocomplete.robots"
    uiTest = true
}
