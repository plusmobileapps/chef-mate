plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.grocery.data.public)
            api(libs.arkivanov.decompose.core)
            api(libs.kotlin.coroutines.core)
            api(projects.client.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.grocery.core"
}
