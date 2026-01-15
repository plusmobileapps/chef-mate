import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinInject)
    alias(libs.plugins.compose)
    alias(libs.plugins.plusKtlint)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts.add("-lsqlite3")
            export(libs.arkivanov.decompose.core)
            export(libs.essenty.lifecycle)
            export(libs.essenty.backhandler)
            export(projects.client.root.public)
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
            api(projects.client.grocery.data.impl)
            api(projects.client.grocery.core.impl)
            api(projects.client.grocery.core.public)
            implementation(libs.kotlinx.serialization.json)
            api(projects.client.database)
            api(projects.client.root.public)
            api(projects.client.root.impl)
            api(projects.client.bottomnav.impl)
            api(projects.client.recipe.data.impl)
            api(projects.client.recipe.list.impl)
            api(projects.client.recipe.core.impl)
            api(projects.client.util.impl)
            api(projects.client.settings.impl)
            api(libs.napier)
            implementation(libs.supabase.client)
            implementation(libs.supabase.auth)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
            implementation(libs.logback)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.plusmobileapps.chefmate"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.plusmobileapps.chefmate"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

compose.desktop {
    application {
        mainClass = "com.plusmobileapps.chefmate.MainKt"

        // Pass deep link URI as argument when app is launched via URL scheme
        args += listOf()

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Chef Mate"
            packageVersion = "1.0.0"
            description = "Chef Mate - Your AI Cooking Assistant"
            vendor = "Plus Mobile Apps"

            modules("java.sql", "java.naming")

            // macOS configuration
            macOS {
                bundleID = "com.plusmobileapps.chefmate"
                dockName = "Chef Mate"

                infoPlist {
                    extraKeysRawXml = macExtraPlistKeys
                }
            }

            // Linux configuration
            linux {
                packageName = "chef-mate"
                shortcut = true
                debMaintainer = "support@plusmobileapps.com"
                menuGroup = "Utility"
                appCategory = "Utility"

                // The .desktop file will be generated with MimeType for URL scheme
                // Custom .desktop file is placed in resources and will be used
            }

            // Windows configuration
            windows {
                shortcut = true
                menu = true
                menuGroup = "Chef Mate"
                upgradeUuid = "18159995-d967-4CD2-8885-77BFE3B59F98"

                // Register URL protocol via registry (handled by jpackage installer)
                // The installer will create registry entries for chefmate:// scheme
            }
        }
    }
}

val macExtraPlistKeys: String
    get() = """
      <key>CFBundleURLTypes</key>
      <array>
        <dict>
          <key>CFBundleURLName</key>
          <string>Chef Mate deep link</string>
          <key>CFBundleURLSchemes</key>
          <array>
            <string>chefmate</string>
          </array>
        </dict>
      </array>
    """
