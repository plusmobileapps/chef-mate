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

    sourceSets {
        commonMain.dependencies {
            api(libs.arkivanov.decompose.core)
            api(libs.arkivanov.decompose.compose.extensions)
            api(libs.essenty.lifecycle)
            api(libs.essenty.backhandler)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            api(projects.client.shared)
            api(projects.client.auth.data.impl)
            api(projects.client.auth.data.public)
            api(projects.client.auth.ui.impl)
            api(projects.client.auth.usecase.impl)
            api(projects.client.cook.impl)
            api(projects.client.cook.public)
            api(projects.client.featureflag.impl)
            api(projects.client.featureflag.public)
            api(projects.client.grocery.data.impl)
            api(projects.client.grocery.core.impl)
            api(projects.client.grocery.core.public)
            api(projects.client.meal.data.impl)
            api(projects.client.meal.core.impl)
            implementation(libs.kotlinx.serialization.json)
            api(projects.client.database.core)
            api(projects.client.root.public)
            api(projects.client.root.impl)
            api(projects.client.bottomnav.impl)
            api(projects.client.browser.impl)
            api(projects.client.recipe.data.impl)
            api(projects.client.recipe.list.impl)
            api(projects.client.recipe.core.impl)
            api(projects.client.util.impl)
            api(projects.client.settings.impl)
            api(projects.client.developerSettings.impl)
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
            implementation(projects.client.recipe.core.implRobots)
            implementation(projects.client.recipe.list.implRobots)
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
        versionCode = 63
        versionName = "1.4.3"
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

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Chef Mate"
            packageVersion = "1.4.3"
            description = "Chef Mate - Your AI Cooking Assistant"
            vendor = "Plus Mobile Apps"

            modules("java.sql", "java.naming", "jdk.jsobject")

            // macOS configuration
            macOS {
                packageVersion = "1.4.3"
                bundleID = "com.plusmobileapps.chefmate"
                dockName = "Chef Mate"

                iconFile.set(project.file("src/jvmMain/resources/app-icon.icns"))
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
