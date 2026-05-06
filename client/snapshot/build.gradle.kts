plugins {
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.paparazzi)
    alias(libs.plugins.plusKtfmt)
}

kotlin {
    androidLibrary {
        namespace = "com.plusmobileapps.chefmate.snapshot"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compilerOptions { freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime") }

    sourceSets {
        androidUnitTest.dependencies {
            // Brings in Compose + ChefMateTheme (via ui:public) transitively
            implementation(projects.client.cook.public)
            implementation(projects.client.ui.public)
        }
    }
}
