plugins {
    alias(libs.plugins.kmpLibrary)
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

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.plusmobileapps.chefmate.database")
        }
    }
}

plusMobile {
    namespace = "com.plusmobileapps.chefmate.database"
}