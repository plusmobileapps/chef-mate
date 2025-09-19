plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.shared)
            api(libs.arkivanov.decompose.core)
            api(libs.kotlin.coroutines.test)
            api(libs.essenty.lifecycle.coroutines)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.testing"
}
