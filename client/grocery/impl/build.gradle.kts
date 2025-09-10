plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinInject)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.grocery.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.database)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

plusMobile {
    namespace = "com.plusmobileapps.chefmate.grocery.impl"
}