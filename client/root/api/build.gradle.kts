plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.kotlinInject)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(projects.client.groceryList)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.plusmobileapps.chefmate.root"
}