plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.recipe.data.public)
            implementation(projects.client.shared)
            implementation(projects.client.database)
            api(projects.client.util.public)
            implementation(projects.client.auth.data.public)
            implementation(libs.supabase.client)
            implementation(libs.supabase.postgrest)
            implementation(libs.kotlinx.serialization.json)
        }
        jvmMain.dependencies { implementation(libs.ktor.client.cio) }
        androidMain.dependencies { implementation(libs.ktor.client.cio) }
        iosMain.dependencies { implementation(libs.ktor.client.darwin) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.data.impl"
    enableDi = true
    enableTesting = true
}
