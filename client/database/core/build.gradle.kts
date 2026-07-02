plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.sqldelight)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.sqldelight.coroutines)
            implementation(projects.client.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlin.coroutines.test)
        }
        androidMain.dependencies { implementation(libs.sqldelight.drivers.android) }
        // Shared by iOS + watchOS via the appleMain hierarchy. The native SQLite driver and the
        // DriverFactory apple actual both support watchOS unchanged.
        appleMain.dependencies { implementation(libs.sqldelight.drivers.native) }
        jvmMain.dependencies { implementation(libs.sqldelight.drivers.jvm) }
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.plusmobileapps.chefmate.database")
            dialect(libs.sqldelight.dialect.sqlite335)
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/schema"))
            verifyMigrations.set(true)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.database"
    enableDi = true
    enableWatch = true
}
