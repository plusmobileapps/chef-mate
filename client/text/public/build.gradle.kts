plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(compose.components.resources)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.text"
}
