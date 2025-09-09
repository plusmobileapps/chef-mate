import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinInject)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.shared)
            implementation(projects.client.database)
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.kotlin.coroutines.core)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlin.coroutines.test)
        }
        androidMain.dependencies {
            implementation(compose.preview)
        }
    }
}

android {
    namespace = "com.plusmobileapps.chefmate.grocerylist"
}
