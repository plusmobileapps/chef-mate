plugins {
    alias(libs.plugins.kmpLibrary)
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

plusMobile {
    namespace = "com.plusmobileapps.chefmate.shared"
}
