plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
    // ProfileBloc.Props is a navigation Configuration payload, so it needs a generated serializer.
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.arkivanov.decompose.core)
            implementation(projects.client.shared)
            api(projects.client.text.public)
            api(projects.client.util.public)
            api(projects.client.profile.data.public)
            api(projects.client.recipe.data.public)
            implementation(projects.client.ui.public)
            implementation(compose.components.resources)
        }
    }
}

compose { resources { publicResClass = true } }

plusLibrary { namespace = "com.plusmobileapps.chefmate.profile" }
