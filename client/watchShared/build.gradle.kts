plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Public models/contracts the facade maps from.
            implementation(projects.client.grocery.data.public)
            implementation(projects.client.auth.data.public)
            implementation(projects.client.shared)
            implementation(projects.client.util.core.public)
            // Production implementations — on the classpath so their Metro @ContributesBinding /
            // @ContributesTo contributions are aggregated into the watch DI graph.
            implementation(projects.client.grocery.data.impl)
            implementation(projects.client.auth.data.impl)
            implementation(projects.client.database.core)
            // Direct Supabase access for the WatchConnectivity session handoff.
            implementation(libs.supabase.client)
            implementation(libs.supabase.auth)
            implementation(libs.kotlin.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlin.coroutines.test)
            implementation(projects.client.auth.data.testing)
            implementation(projects.client.grocery.data.testing)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.watch.shared"
    enableDi = true
    enableTesting = true
    enableWatch = true
}
