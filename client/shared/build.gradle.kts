import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.kotlinInject)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.arkivanov.decompose.core)
            api(libs.kotlin.coroutines.core)
            implementation(libs.essenty.lifecycle.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.plusmobileapps.chefmate.shared"
}
