plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinInject)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.root.api)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(projects.client.groceryList)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

plusMobile {
    namespace = "com.plusmobileapps.chefmate.root.impl"
}