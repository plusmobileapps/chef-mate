plugins {
    alias(libs.plugins.plusApplication)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.onboarding.impl)
            implementation(projects.client.onboarding.public)
            // shared: AppScope, Settings, @Main dispatcher (CoroutinesComponent)
            implementation(projects.client.shared)
            implementation(projects.client.ui.public) // ChefMateTheme + ComposeScreen
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.arkivanov.decompose.compose.extensions)
        }
    }
}

plusApplication { namespace = "com.plusmobileapps.chefmate.onboarding.demo" }

compose.desktop { application { mainClass = "com.plusmobileapps.chefmate.onboarding.demo.MainKt" } }
