plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.aichat.public)
            implementation(projects.client.shared)
            implementation(projects.client.database.core)
            implementation(projects.client.recipe.data.public)
            implementation(projects.client.text.public)
            implementation(projects.client.ui.public)
            implementation(projects.client.util.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.arkivanov.decompose.compose.extensions)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.multiplatform.markdown.renderer.m3)
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(projects.client.recipe.data.testing)
            implementation(projects.client.util.testing)
            implementation(libs.kotlin.coroutines.test)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.serialization.json)
            implementation(libs.turbine)
        }
        getByName("androidMain").dependencies { implementation(libs.ktor.client.cio) }
        getByName("jvmMain").dependencies { implementation(libs.ktor.client.cio) }
        getByName("iosMain").dependencies { implementation(libs.ktor.client.darwin) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.aichat.impl"
    enableDi = true
    enableTesting = true
    enableDatabaseTesting = true
}
