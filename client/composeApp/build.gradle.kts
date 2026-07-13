import java.util.Properties
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

fun osClassifier(): String {
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch")
    return when {
        osName.contains("mac") || osName.contains("darwin") -> {
            if (osArch == "aarch64") "mac-aarch64" else "mac"
        }

        osName.contains("win") -> "win"
        osName.contains("linux") -> {
            if (osArch == "aarch64") "linux-aarch64" else "linux"
        }

        else -> error("Unsupported OS: $osName")
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.metro)
    alias(libs.plugins.compose)
    alias(libs.plugins.plusKtfmt)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)

            // commonTest is shared with the Android instrumented test variant
            // (connectedDebugAndroidTest), not the unit test variant (testDebugUnitTest).
            // unitTestVariant gets its own (empty) tree so commonTest UI tests don't get
            // dragged into testDebugUnitTest, where Robolectric isn't initialised.
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            unitTestVariant.sourceSetTree.set(KotlinSourceSetTree.unitTest)
        }
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts.add("-lsqlite3")
            export(libs.arkivanov.decompose.core)
            export(libs.essenty.lifecycle)
            export(libs.essenty.backhandler)
            export(projects.client.root.public)
            export(projects.client.shared)
        }
        iosTarget.binaries.withType(
            org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable::class.java
        ) {
            linkerOpts.add("-lsqlite3")
        }
    }

    jvm()

    // Acknowledge the still-Beta expect/actual classes feature (KT-61573) so modules with
    // `expect`/`actual` classes (e.g. BugsnagStartup) don't warn on every compile. Drop once
    // the feature is stabilized. Mirrors the same flag in KmpLibraryConventionPlugin.
    compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }

    sourceSets {
        commonMain.dependencies {
            api(libs.arkivanov.decompose.core)
            api(libs.arkivanov.decompose.compose.extensions)
            api(libs.essenty.lifecycle)
            api(libs.essenty.backhandler)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            api(projects.client.shared)
            api(projects.client.aichat.impl)
            api(projects.client.aichat.public)
            api(projects.client.auth.data.impl)
            api(projects.client.auth.data.public)
            api(projects.client.auth.ui.impl)
            api(projects.client.auth.usecase.impl)
            api(projects.client.cook.impl)
            api(projects.client.cook.public)
            api(projects.client.featureflag.impl)
            api(projects.client.featureflag.public)
            api(projects.client.grocery.autocomplete.impl)
            api(projects.client.grocery.autocomplete.public)
            api(projects.client.grocery.data.impl)
            api(projects.client.grocery.core.impl)
            api(projects.client.grocery.core.public)
            api(projects.client.meal.data.impl)
            api(projects.client.meal.core.impl)
            api(projects.client.notifications.data.impl)
            api(projects.client.notifications.impl)
            api(projects.client.onboarding.impl)
            api(projects.client.onboarding.public)
            implementation(libs.kotlinx.serialization.json)
            api(projects.client.database.core)
            api(projects.client.root.public)
            api(projects.client.root.impl)
            api(projects.client.bottomnav.impl)
            api(projects.client.browser.impl)
            api(projects.client.recipe.categories.impl)
            api(projects.client.recipe.data.impl)
            api(projects.client.recipe.list.impl)
            api(projects.client.recipe.core.impl)
            api(projects.client.recipe.exporter.impl)
            api(projects.client.recipe.importer.impl)
            api(projects.client.recipebook.data.impl)
            api(projects.client.recipebook.edit.impl)
            api(projects.client.profile.impl)
            api(projects.client.util.impl)
            api(projects.client.settings.impl)
            api(projects.client.settings.root.impl)
            api(projects.client.developerSettings.impl)
            api(projects.client.toast.impl)
            api(projects.client.toast.public)
            api(libs.kermit)
            implementation(libs.supabase.client)
            implementation(libs.supabase.auth)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.cio)
            implementation(libs.bugsnag.kmp)
            implementation(libs.kermit.bugsnag)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
            implementation(libs.logback)
            implementation(libs.bugsnag.java)
            val fxClassifier = osClassifier()
            val fxVersion = libs.versions.openjfx.get()
            implementation("org.openjfx:javafx-base:$fxVersion:$fxClassifier")
            implementation("org.openjfx:javafx-controls:$fxVersion:$fxClassifier")
            implementation("org.openjfx:javafx-graphics:$fxVersion:$fxClassifier")
            implementation("org.openjfx:javafx-media:$fxVersion:$fxClassifier")
            implementation("org.openjfx:javafx-swing:$fxVersion:$fxClassifier")
            implementation("org.openjfx:javafx-web:$fxVersion:$fxClassifier")
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.bugsnag.kmp)
            implementation(libs.kermit.bugsnag)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(projects.client.testing)
            implementation(projects.client.database.testing)
            implementation(projects.client.auth.data.testing)
            implementation(projects.client.aichat.implRobots)
            implementation(projects.client.bottomnav.implRobots)
            implementation(projects.client.browser.implRobots)
            implementation(projects.client.featureflag.testing)
            implementation(projects.client.recipe.categories.implRobots)
            implementation(projects.client.grocery.autocomplete.implRobots)
            implementation(projects.client.grocery.core.implRobots)
            implementation(projects.client.recipe.core.implRobots)
            implementation(projects.client.recipe.list.implRobots)
            implementation(projects.client.settings.implRobots)
            implementation(projects.client.settings.root.implRobots)
            implementation(projects.client.profile.implRobots)
            implementation(projects.client.notifications.implRobots)
            implementation(projects.client.onboarding.implRobots)
        }
        val jvmTest by getting {
            dependencies {
                implementation(compose.desktop.uiTestJUnit4)
                implementation(compose.desktop.currentOs)
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.sqldelight.drivers.android)
                implementation(libs.androidx.test.core)
            }
        }
    }
}

android {
    namespace = "com.plusmobileapps.chefmate"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties =
        Properties().also { props ->
            if (keystorePropertiesFile.exists()) {
                keystorePropertiesFile.reader().use { props.load(it) }
            }
        }

    signingConfigs {
        create("release") {
            storeFile =
                file(
                    System.getenv("ANDROID_KEYSTORE_FILE")
                        ?: keystoreProperties.getProperty("releaseKeyStore")
                        ?: "release.keystore"
                )
            storePassword =
                System.getenv("ANDROID_KEYSTORE_PASSWORD")
                    ?: keystoreProperties.getProperty("releaseStorePassword")
                    ?: ""
            keyAlias =
                System.getenv("ANDROID_KEY_ALIAS")
                    ?: keystoreProperties.getProperty("releaseKeyAlias")
                    ?: ""
            keyPassword =
                System.getenv("ANDROID_KEY_PASSWORD")
                    ?: keystoreProperties.getProperty("releaseKeyPassword")
                    ?: ""
        }
    }

    defaultConfig {
        applicationId = "com.plusmobileapps.chefmate"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 103
        versionName = "1.9.17"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    androidTestImplementation(libs.androidx.compose.ui.test.junit4.android)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

compose.desktop {
    application {
        mainClass = "com.plusmobileapps.chefmate.MainKt"

        // Pass deep link URI as argument when app is launched via URL scheme
        args += listOf()

        // Ship distributables via the release packaging tasks (packageReleaseDmg/Msi/Deb) so the
        // requested task name contains "Release". That marker is what flips BuildConfig.IS_DEBUG to
        // false and hides the developer-only UI — see client/shared/build.gradle.kts. Keep
        // R8/ProGuard
        // OFF: this app relies on reflection (Ktor, Supabase, kotlinx.serialization, SQLDelight,
        // Bugsnag, Decompose) and minification would strip classes those libraries look up at
        // runtime.
        buildTypes.release.proguard {
            isEnabled.set(false)
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Chef Mate"
            packageVersion = "1.9.17"
            description = "Chef Mate - Your AI Cooking Assistant"
            vendor = "Plus Mobile Apps"

            // jdk.unsupported.desktop provides jdk.swing.interop.SwingInterOpUtils, which
            // JavaFX's Swing interop (JFXPanel, used by the desktop browser's WebView) loads at
            // runtime. Without it the jlink/jpackage runtime image omits the class and the browser
            // crashes with NoClassDefFoundError the first time the cursor updates over the WebView
            // (see issue #400). Not needed for `./gradlew run`, which uses the full JDK.
            modules("java.sql", "java.naming", "jdk.jsobject", "jdk.unsupported.desktop")

            // macOS configuration
            macOS {
                packageVersion = "1.9.17"
                bundleID = "com.plusmobileapps.chefmate"
                dockName = "Chef Mate"

                iconFile.set(project.file("src/jvmMain/resources/app-icon.icns"))

                // Sign with a Developer ID Application certificate when an identity is
                // provided via env var (set in CI). Left unset for local/unsigned builds.
                // Hardened runtime + the default Compose entitlements (allow-jit,
                // unsigned-executable-memory, disable-library-validation) are applied
                // automatically when signing is enabled — required for notarization.
                signing {
                    sign.set(System.getenv("MACOS_SIGN_IDENTITY") != null)
                    identity.set(System.getenv("MACOS_SIGN_IDENTITY"))
                }
            }

            // Linux configuration
            linux {
                packageName = "chef-mate"
                shortcut = true
                debMaintainer = "support@plusmobileapps.com"
                menuGroup = "Utility"
                appCategory = "Utility"

                iconFile.set(project.file("src/jvmMain/resources/app-icon.png"))
            }

            // Windows configuration
            windows {
                shortcut = true
                menu = true
                menuGroup = "Chef Mate"
                upgradeUuid = "18159995-d967-4CD2-8885-77BFE3B59F98"
            }
        }
    }
}
