import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinInject)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.sqldelight.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlin.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.drivers.android)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.drivers.native)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.drivers.jvm)
        }
    }
}

android {
    namespace = "com.plusmobileapps.chefmate.client.database"
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.plusmobileapps.chefmate.database")
        }
    }
}