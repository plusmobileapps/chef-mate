plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.auth.data.public)
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            implementation(libs.supabase.client)
            implementation(libs.supabase.auth)
            implementation(libs.supabase.postgrest)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.cio)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)
        }
        iosMain.dependencies { implementation(libs.ktor.client.darwin) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.auth.data.impl"
    enableDi = true
    enableTesting = true
}
