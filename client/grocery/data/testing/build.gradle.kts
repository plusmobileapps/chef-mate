plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.grocery.data.public)
            implementation(projects.client.shared)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.grocery.data.testing"
}
