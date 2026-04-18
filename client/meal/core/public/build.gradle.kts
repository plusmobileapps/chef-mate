plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.coroutines.core)
            api(libs.arkivanov.decompose.core)
            api(libs.kotlinx.datetime)
            api(projects.client.shared)
            implementation(compose.components.resources)
            implementation(projects.client.meal.data.public)
            implementation(projects.client.ui.public)
            implementation(projects.client.text.public)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.meal.core" }
