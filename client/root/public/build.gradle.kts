plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.arkivanov.decompose.core)
            implementation(libs.arkivanov.decompose.compose.extensions)
            implementation(projects.client.shared)
            api(projects.client.bottomnav.public)
            api(projects.client.grocery.public)
            api(projects.client.recipe.core.public)
            implementation(projects.client.ui.public)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.root"
}
