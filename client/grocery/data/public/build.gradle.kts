plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies { implementation(libs.kotlin.test) }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.grocery.data" }
