plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            api(projects.client.text.public)
            implementation(projects.client.ui.public)
            implementation(compose.components.resources)
        }
        getByName("androidMain").dependencies {
            implementation(libs.multiplatform.webview)
        }
        getByName("iosMain").dependencies {
            implementation(libs.multiplatform.webview)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.browser"
}
