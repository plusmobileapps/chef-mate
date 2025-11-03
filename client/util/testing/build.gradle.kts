plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.util.public)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.util.testing"
}
