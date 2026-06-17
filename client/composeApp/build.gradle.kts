import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
    // Applied without an explicit version: AGP is already on the build classpath (via the
    // :client:androidApp application module and the KMP library convention plugin), so an
    // aliased version request would fail the "plugin already on the classpath" check.
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.metro)
    alias(libs.plugins.compose)
    alias(libs.plugins.plusKtfmt)
}

kotlin {
    // AGP 9: the Android target is now a KMP-aware library (com.android.application was
    // extracted into the standalone :client:androidApp module). The app's manifest,
    // resources, signing, and entry points live there; this module ships only shared code.
    androidLibrary {
        // Distinct from the app module's `com.plusmobileapps.chefmate` namespace — Android
        // requires every module/library to have a unique namespace. Only scopes this
        // library's generated R/BuildConfig; the shared Kotlin code keeps its own packages.
        namespace = "com.plusmobileapps.chefmate.composeapp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        // Pin Android JVM bytecode level (iOS/JVM targets configured separately below).
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)

        // Compose Multiplatform resources (strings.xml under commonMain/composeResources)
        // are bundled into the AAR via the Android resource pipeline. The new KMP Android
        // library plugin defaults this to false, which would drop them at runtime.
        androidResources.enable = true

        // The robot UI flows live in commonTest with their android `actual`s (test DI
        // graph, in-memory database) in androidDeviceTest; both run on-device. No host
        // (JVM) unit tests exist for this module, so androidHostTest stays disabled (was
        // unitTestVariant=empty). androidDeviceTest is wired to commonTest below.
        withDeviceTest {}
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
            api(projects.client.grocery.data.impl)
            api(projects.client.grocery.core.impl)
            api(projects.client.grocery.core.public)
            api(projects.client.meal.data.impl)
            api(projects.client.meal.core.impl)
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
            implementation(projects.client.featureflag.testing)
            implementation(projects.client.recipe.categories.implRobots)
            implementation(projects.client.grocery.core.implRobots)
            implementation(projects.client.recipe.core.implRobots)
            implementation(projects.client.recipe.list.implRobots)
            implementation(projects.client.settings.implRobots)
            implementation(projects.client.settings.root.implRobots)
            implementation(projects.client.profile.implRobots)
            implementation(projects.client.onboarding.implRobots)
        }
        val jvmTest by getting {
            dependencies {
                implementation(compose.desktop.uiTestJUnit4)
                implementation(compose.desktop.currentOs)
            }
        }
        val androidDeviceTest by getting {
            // TODO(agp9): the new KMP Android library plugin places androidDeviceTest in its
            // own instrumented source-set tree, so the shared robot UI flows in commonTest
            // (and their android actuals here) are not yet routed into the on-device test the
            // way the old `instrumentedTestVariant.sourceSetTree.set(test)` did. The straight
            // `dependsOn(commonTest)` and `withDeviceTestBuilder { sourceSetTreeName = "test" }`
            // forms both fail expect/actual resolution; needs finalizing + on-device run.
            dependencies {
                implementation(libs.sqldelight.drivers.android)
                implementation(libs.androidx.test.core)
                implementation(libs.androidx.compose.ui.test.junit4.android)
                implementation(libs.androidx.compose.ui.test.manifest)
            }
        }
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
            packageVersion = "1.8.7"
            description = "Chef Mate - Your AI Cooking Assistant"
            vendor = "Plus Mobile Apps"

            modules("java.sql", "java.naming", "jdk.jsobject")

            // macOS configuration
            macOS {
                packageVersion = "1.8.7"
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
