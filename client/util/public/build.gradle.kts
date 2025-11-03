plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.util"
}
