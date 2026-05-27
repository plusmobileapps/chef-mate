plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.featureflag.public)
            implementation(projects.client.text.public)
            implementation(projects.client.ui.public)
            implementation(projects.client.shared)
            implementation(projects.client.auth.data.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.arkivanov.decompose.compose.extensions)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.supabase.client)
            implementation(libs.supabase.postgrest)
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(libs.multiplatform.settings.test)
            implementation(libs.kotlin.coroutines.test)
            implementation(libs.kotest.assertions)
            implementation(libs.turbine)
            implementation(projects.client.auth.data.testing)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.featureflag.impl"
    enableDi = true
    enableTesting = true
}
