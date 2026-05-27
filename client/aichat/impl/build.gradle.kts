plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.aichat.public)
            implementation(projects.client.shared)
            implementation(projects.client.database.core)
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
        }
        commonTest.dependencies { implementation(libs.kotlin.coroutines.test) }
        getByName("androidMain").dependencies { implementation(libs.ktor.client.cio) }
        getByName("jvmMain").dependencies { implementation(libs.ktor.client.cio) }
        getByName("iosMain").dependencies { implementation(libs.ktor.client.darwin) }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.aichat.impl"
    enableDi = true
    enableTesting = true
}
