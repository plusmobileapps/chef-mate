plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.shared)
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.kotlin.coroutines.test)
            implementation(libs.essenty.lifecycle.coroutines)
            implementation(libs.kotest.framework)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.testing"
}
