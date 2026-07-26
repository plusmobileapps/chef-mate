plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.recipe.list.public)
            implementation(projects.client.text.public)
            implementation(projects.client.ui.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.arkivanov.decompose.compose.extensions)
            implementation(projects.client.shared)
            implementation(projects.client.recipe.data.public)
            implementation(projects.client.recipebook.data.public)
            implementation(projects.client.cook.public)
            implementation(projects.client.toast.public)
            implementation(projects.client.util.public)
            implementation(projects.client.featureflag.public)
            implementation(libs.multiplatform.settings)
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(projects.client.recipe.data.testing)
            implementation(projects.client.featureflag.testing)
            implementation(projects.client.recipebook.data.testing)
            implementation(projects.client.toast.testing)
            implementation(libs.multiplatform.settings.test)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.list.impl"
    enableDi = true
    enableTesting = true
}
