plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.recipebook.edit.public)
            implementation(projects.client.recipebook.data.public)
            implementation(projects.client.text.public)
            implementation(projects.client.shared)
            implementation(libs.arkivanov.decompose.core)
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(projects.client.recipebook.data.testing)
            implementation(projects.client.recipebook.edit.implRobots)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipebook.edit.impl"
    enableDi = true
    enableTesting = true
}
