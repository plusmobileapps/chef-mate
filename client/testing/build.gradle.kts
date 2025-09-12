plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(kotlin("test-common"))
            api(kotlin("test-annotations-common"))
            api(projects.client.shared)
            api(libs.arkivanov.decompose.core)
            api(libs.kotlin.coroutines.test)
            api(libs.essenty.lifecycle.coroutines)
            api(libs.kotest.framework)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.testing"
}
