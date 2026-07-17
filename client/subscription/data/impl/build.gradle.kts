import com.plusmobileapps.chefmate.linkSwiftBackCompatIntoIosTestBinaries

plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.client.subscription.data.public)
            implementation(projects.client.shared)
            implementation(libs.kotlin.coroutines.core)
        }
        // RevenueCat's purchases-kmp SDK ships only Android + iOS artifacts. The default source-set
        // hierarchy has no shared Android+iOS parent, so the (identical) RevenueCat gateway and
        // initializer live in both androidMain and iosMain; JVM gets a no-op actual from jvmMain.
        androidMain.dependencies {
            implementation(libs.purchases.core)
            implementation(libs.purchases.result)
        }
        getByName("iosMain").dependencies {
            implementation(libs.purchases.core)
            implementation(libs.purchases.result)
        }
    }
}

// This module owns the RevenueCat dependency, so its own iOS test binaries link the Swift code.
linkSwiftBackCompatIntoIosTestBinaries()

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.subscription.data.impl"
    enableDi = true
    enableTesting = true
}
