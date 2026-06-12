plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(compose.components.resources)
            api(libs.kotlinx.collections.immutable)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.text" }
