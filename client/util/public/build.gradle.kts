import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.androidx.annotation)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.util"
}
