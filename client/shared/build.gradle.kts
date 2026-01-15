import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    alias(libs.plugins.kmpLibrary)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.arkivanov.decompose.core)
            api(libs.kotlin.coroutines.core)
            implementation(libs.essenty.lifecycle.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

plusLibrary {
    namespace = "com.plusmobileapps.chefmate.shared"
    enableDi = true
}

// Load properties from local.properties
val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { load(it) }
        }
    }

// Read Supabase credentials from local.properties or environment variables
val supabaseUrl =
    localProperties.getProperty("supabase.url")
        ?: System.getenv("SUPABASE_URL")
        ?: "https://your-project-id.supabase.co"

val supabaseKey =
    localProperties.getProperty("supabase.key")
        ?: System.getenv("SUPABASE_KEY")
        ?: "your-anon-public-key"

buildkonfig {
    packageName = "com.plusmobileapps.chefmate.buildconfig"
    objectName = "BuildConfig"
    exposeObjectWithName = "BuildConfig"

    defaultConfigs {
        buildConfigField(STRING, "SUPABASE_URL", supabaseUrl)
        buildConfigField(STRING, "SUPABASE_KEY", supabaseKey)
    }
}
