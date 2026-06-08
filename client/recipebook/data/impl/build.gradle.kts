plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.recipebook.data.public)
            implementation(projects.client.recipe.data.public)
            implementation(projects.client.shared)
            implementation(projects.client.database.core)
            api(projects.client.util.public)
            implementation(projects.client.auth.data.public)
            implementation(libs.multiplatform.settings)
            implementation(libs.supabase.client)
            implementation(libs.supabase.postgrest)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(projects.client.auth.data.testing)
            implementation(projects.client.recipebook.data.testing)
            implementation(projects.client.util.testing)
            implementation(libs.multiplatform.settings.test)
        }
        jvmMain.dependencies { implementation(libs.ktor.client.cio) }
        androidMain.dependencies { implementation(libs.ktor.client.cio) }
        iosMain.dependencies { implementation(libs.ktor.client.darwin) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipebook.data.impl"
    enableDi = true
    enableTesting = true
    enableDatabaseTesting = true
}
