plugins { alias(libs.plugins.kmpLibrary) }

kotlin {
    sourceSets {
        commonMain.dependencies { api(libs.kotlinx.datetime) }
        commonTest.dependencies { implementation(libs.kotlin.test) }
    }
}

plusLibrary { namespace = "com.plusmobileapps.chefmate.util.core" }
