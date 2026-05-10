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

// Web OAuth client ID from Google Cloud Console. Used by Android Credential Manager as
// `serverClientId` so the issued ID token's audience is something Supabase will accept.
val googleWebClientId =
    localProperties.getProperty("google.webClientId") ?: System.getenv("GOOGLE_WEB_CLIENT_ID") ?: ""

// Desktop OAuth client ID + secret from Google Cloud Console (client type = "Desktop app").
// Used by the JVM loopback flow to exchange the auth code for an ID token. Desktop clients
// can ship a "secret" — Google explicitly documents it as not actually secret for this
// client type, and PKCE is the real protection.
val googleDesktopClientId =
    localProperties.getProperty("google.desktopClientId")
        ?: System.getenv("GOOGLE_DESKTOP_CLIENT_ID")
        ?: ""

val googleDesktopClientSecret =
    localProperties.getProperty("google.desktopClientSecret")
        ?: System.getenv("GOOGLE_DESKTOP_CLIENT_SECRET")
        ?: ""

// Collect test users by incrementing n until a pair is missing. Looked up in order:
// 1. local.properties at the project root (chefmate.user.<n> / chefmate.user.password.<n>)
// 2. ~/.gradle/gradle.properties or any other Gradle property source (same keys)
// 3. Environment variables (CHEF_MATE_USER_<n> / CHEF_MATE_USER_PASSWORD_<n>)
// Serialize as "email1|password1;email2|password2".
val testUsersSerialized = buildString {
    var index = 1
    while (true) {
        val email =
            localProperties.getProperty("chefmate.user.$index")
                ?: (findProperty("chefmate.user.$index") as? String)
                ?: System.getenv("CHEF_MATE_USER_$index")
        val password =
            localProperties.getProperty("chefmate.user.password.$index")
                ?: (findProperty("chefmate.user.password.$index") as? String)
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

// Single source of truth lives in client/composeApp/build.gradle.kts (versionName = "..").
// The bump-version scripts already update that file via sed; we mirror its value into
// BuildConfig so common code (e.g. feature-flag version targeting) can read it.
val composeAppGradleFile = rootProject.file("client/composeApp/build.gradle.kts")
val appVersionName =
    Regex("""versionName\s*=\s*"([^"]+)"""")
        .find(composeAppGradleFile.readText())
        ?.groupValues
        ?.get(1) ?: "0.0.0"

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
        buildConfigField(STRING, "VERSION_NAME", appVersionName)
        buildConfigField(STRING, "GOOGLE_WEB_CLIENT_ID", googleWebClientId)
        buildConfigField(STRING, "GOOGLE_DESKTOP_CLIENT_ID", googleDesktopClientId)
        buildConfigField(STRING, "GOOGLE_DESKTOP_CLIENT_SECRET", googleDesktopClientSecret)
    }
}
