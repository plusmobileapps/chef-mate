plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.util"
}
