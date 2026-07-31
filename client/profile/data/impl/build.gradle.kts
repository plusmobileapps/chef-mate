plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.profile.data.public)
            implementation(projects.client.auth.data.public)
            implementation(projects.client.shared)
            implementation(libs.supabase.client)
            implementation(libs.supabase.auth)
            implementation(libs.supabase.postgrest)
        }
        commonTest.dependencies { implementation(projects.client.profile.data.testing) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.profile.data.impl"
    enableDi = true
    enableTesting = true
}
