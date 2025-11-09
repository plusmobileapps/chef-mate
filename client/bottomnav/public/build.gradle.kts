plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.grocery.core.public)
            api(projects.client.recipe.list.public)
            api(projects.client.settings.public)
            implementation(projects.client.shared)
            implementation(projects.client.ui.public)
            implementation(compose.components.resources)
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.arkivanov.decompose.compose.extensions)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.bottomnav"
}
