plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.shared)
            api(libs.kotlinx.serialization.json)
        }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.recipe.data" }
