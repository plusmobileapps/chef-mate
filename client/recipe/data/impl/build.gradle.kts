plugins {
    alias(libs.plugins.kmpLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.recipe.data.public)
            implementation(projects.client.shared)
            implementation(projects.client.database)
            api(projects.client.util.public)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.recipe.data.impl"
    enableDi = true
    enableTesting = true
}
