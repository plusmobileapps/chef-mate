import java.util.Properties

// Standalone Android application module. AGP 9 drops compatibility between
// `com.android.application` and the Kotlin Multiplatform plugin, so the app entry
// points (Application, Activity, manifest, resources, DI graph) live here and depend
// on the `:client:composeApp` KMP library for all shared code.
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.androidBuiltInKotlin)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.metro)
    alias(libs.plugins.plusKtfmt)
}

android {
    namespace = "com.plusmobileapps.chefmate"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties =
        Properties().also { props ->
            if (keystorePropertiesFile.exists()) {
                keystorePropertiesFile.reader().use { props.load(it) }
            }
        }

    signingConfigs {
        create("release") {
            storeFile =
                file(
                    System.getenv("ANDROID_KEYSTORE_FILE")
                        ?: keystoreProperties.getProperty("releaseKeyStore")
                        ?: "release.keystore"
                )
            storePassword =
                System.getenv("ANDROID_KEYSTORE_PASSWORD")
                    ?: keystoreProperties.getProperty("releaseStorePassword")
                    ?: ""
            keyAlias =
                System.getenv("ANDROID_KEY_ALIAS")
                    ?: keystoreProperties.getProperty("releaseKeyAlias")
                    ?: ""
            keyPassword =
                System.getenv("ANDROID_KEY_PASSWORD")
                    ?: keystoreProperties.getProperty("releaseKeyPassword")
                    ?: ""
        }
    }

    defaultConfig {
        applicationId = "com.plusmobileapps.chefmate"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 82
        versionName = "1.8.7"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { compose = true }
}

dependencies {
    implementation(projects.client.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(libs.arkivanov.decompose.core)
}
