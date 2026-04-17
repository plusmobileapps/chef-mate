plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies { implementation(libs.kotlin.coroutines.core) }
        commonTest.dependencies { implementation(libs.kotlin.test) }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.grocery.data" }
