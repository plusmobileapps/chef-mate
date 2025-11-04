plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.coroutines.core)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.grocery.data"
}
