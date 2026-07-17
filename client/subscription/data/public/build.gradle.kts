plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets { commonMain.dependencies { api(libs.kotlin.coroutines.core) } }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.subscription.data" }
