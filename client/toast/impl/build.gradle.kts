plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.toast.public)
            implementation(projects.client.text.public)
            implementation(projects.client.ui.public)
            implementation(projects.client.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.coroutines.test)
            implementation(libs.kotest.assertions)
            implementation(libs.turbine)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.toast.impl"
    enableDi = true
    enableTesting = true
}
