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
        commonTest.dependencies { implementation(libs.kotlin.test) }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.recipe.data" }
