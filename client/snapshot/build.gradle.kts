plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.paparazzi)
    alias(libs.plugins.plusKtfmt)
}

android {
    namespace = "com.plusmobileapps.chefmate.snapshot"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-opt-in=kotlin.time.ExperimentalTime"
    }
}

dependencies {
    // Brings in Compose + ChefMateTheme (via ui:public) transitively
    testImplementation(projects.client.cook.public)
    testImplementation(projects.client.ui.public)
}
