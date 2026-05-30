plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.arkivanov.decompose.core)
            api(projects.client.text.public)
            implementation(projects.client.shared)
            api(projects.client.ui.public)
            implementation(projects.client.util.public)
            implementation(compose.components.resources)
        }
    }
}

compose {
    resources {
        publicResClass = true
        // The default package derived from the module path (…recipe.import.public…) contains the
        // `import` hard keyword, which can't appear in a Kotlin import statement. Pin an explicit
        // keyword-free package for the generated Res class.
        packageOfResClass = "com.plusmobileapps.chefmate.recipe.importer.generated.resources"
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.recipe.importer" }
