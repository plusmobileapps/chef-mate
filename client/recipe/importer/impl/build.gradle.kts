plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.recipe.importer.public)
            implementation(projects.client.recipe.data.public)
            implementation(projects.client.text.public)
            implementation(projects.client.shared)
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.ksoup)
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(projects.client.recipe.data.testing)
            implementation(projects.client.recipe.importer.implRobots)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.importer.impl"
    enableDi = true
    enableTesting = true
}
