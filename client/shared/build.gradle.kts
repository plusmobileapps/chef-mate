import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
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
            api(libs.kermit)
            api(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.noarg)
            implementation(libs.essenty.lifecycle.coroutines)
        }
        commonTest.dependencies { implementation(libs.kotlin.test) }
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

val supabaseTestingUrl =
    localProperties.getProperty("supabase.testing.url")
        ?: System.getenv("SUPABASE_TESTING_URL")
        ?: supabaseUrl

val supabaseTestingKey =
    localProperties.getProperty("supabase.testing.key")
        ?: System.getenv("SUPABASE_TESTING_KEY")
        ?: supabaseKey

val bugsnagApiKey =
    localProperties.getProperty("bugsnag.apiKey") ?: System.getenv("BUGSNAG_API_KEY") ?: ""

// Collect test users from CHEF_MATE_USER_<n> / CHEF_MATE_USER_PASSWORD_<n> (or local.properties
// chefmate.user.<n> / chefmate.user.password.<n>) by incrementing n until a pair is missing.
// Serialize as "email1|password1;email2|password2".
val testUsersSerialized = buildString {
    var index = 1
    while (true) {
        val email =
            localProperties.getProperty("chefmate.user.$index")
                ?: System.getenv("CHEF_MATE_USER_$index")
        val password =
            localProperties.getProperty("chefmate.user.password.$index")
                ?: System.getenv("CHEF_MATE_USER_PASSWORD_$index")
        if (email.isNullOrBlank() || password.isNullOrBlank()) break
        if (index > 1) append(';')
        append(email).append('|').append(password)
        index++
    }
}

// Detect debug builds by inspecting requested Gradle task names. Defaults to debug when no
// release-flavoured task is requested (e.g. during IDE sync), which is the safe choice for
// developer-only UI gating.
val isDebugBuildFlag: Boolean =
    gradle.startParameter.taskNames.none { taskName ->
        taskName.contains("Release", ignoreCase = true) ||
            taskName.contains("bundleRelease", ignoreCase = true)
    }

buildkonfig {
    packageName = "com.plusmobileapps.chefmate.buildconfig"
    objectName = "BuildConfig"
    exposeObjectWithName = "BuildConfig"

    defaultConfigs {
        buildConfigField(STRING, "SUPABASE_URL", supabaseUrl)
        buildConfigField(STRING, "SUPABASE_KEY", supabaseKey)
        buildConfigField(STRING, "SUPABASE_PROD_URL", supabaseUrl)
        buildConfigField(STRING, "SUPABASE_PROD_KEY", supabaseKey)
        buildConfigField(STRING, "SUPABASE_TESTING_URL", supabaseTestingUrl)
        buildConfigField(STRING, "SUPABASE_TESTING_KEY", supabaseTestingKey)
        buildConfigField(STRING, "BUGSNAG_API_KEY", bugsnagApiKey)
        buildConfigField(STRING, "TEST_USERS", testUsersSerialized)
        buildConfigField(BOOLEAN, "IS_DEBUG", isDebugBuildFlag.toString())
    }
}
