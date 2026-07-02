plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
            // Re-export the Compose-free date/time core (DateTimeUtil + LocalDate extensions) so
            // existing consumers that import them from `com.plusmobileapps.chefmate.util` keep
            // working unchanged.
            api(projects.client.util.core.public)
            api(projects.client.text.public)
            implementation(compose.components.resources)
            implementation(compose.ui)
        }

        androidMain.dependencies {
            implementation(libs.androidx.annotation)
            implementation(libs.androidx.activity.compose)
        }
    }
}

compose { resources { publicResClass = true } }

plusLibrary { namespace = "com.plusmobileapps.chefmate.util" }
