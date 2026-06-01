plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.recipe.exporter.public)
            implementation(projects.client.recipe.data.public)
            implementation(projects.client.text.public)
            implementation(projects.client.shared)
            implementation(libs.arkivanov.decompose.core)
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(projects.client.recipe.data.testing)
            implementation(projects.client.recipe.exporter.implRobots)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.exporter.impl"
    enableDi = true
    enableTesting = true
}
