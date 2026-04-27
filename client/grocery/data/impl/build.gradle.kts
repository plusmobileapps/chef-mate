plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all { linkerOpts("-lsqlite3") }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.database.core)
            implementation(projects.client.grocery.data.public)
            implementation(projects.client.shared)
            implementation(projects.client.util.public)
            implementation(projects.client.auth.data.public)
            implementation(libs.supabase.client)
            implementation(libs.supabase.postgrest)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(projects.client.auth.data.testing)
            implementation(projects.client.database.testing)
            implementation(projects.client.grocery.data.testing)
            implementation(projects.client.util.testing)
        }
        jvmMain.dependencies { implementation(libs.ktor.client.cio) }
        androidMain.dependencies { implementation(libs.ktor.client.cio) }
        iosMain.dependencies { implementation(libs.ktor.client.darwin) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.grocery.data.impl"
    enableDi = true
    enableTesting = true
}
