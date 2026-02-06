plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.database)
            implementation(projects.client.grocery.data.public)
            implementation(projects.client.shared)
            implementation(projects.client.util.public)
            implementation(projects.client.auth.data.public)
            implementation(libs.supabase.client)
            implementation(libs.supabase.postgrest)
            implementation(libs.kotlinx.serialization.json)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.grocery.data.impl"
    enableDi = true
    enableTesting = true
}
