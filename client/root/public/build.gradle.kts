plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.bottomnav.public)
            api(projects.client.grocery.core.public)
            api(projects.client.recipe.core.public)
            api(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(projects.client.ui.public)
            implementation(libs.arkivanov.decompose.compose.extensions)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.root"
}
