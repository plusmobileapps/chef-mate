plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies { api(projects.client.database.core) }
        jvmMain.dependencies { implementation(libs.sqldelight.drivers.jvm) }
        androidMain.dependencies { implementation(libs.sqldelight.drivers.jvm) }
        iosMain.dependencies { implementation(libs.sqldelight.drivers.native) }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.database.testing" }
