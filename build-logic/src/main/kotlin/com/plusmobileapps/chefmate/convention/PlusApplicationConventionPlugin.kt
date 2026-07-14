package com.plusmobileapps.chefmate.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.plusmobileapps.chefmate.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for standalone feature **demo** apps (`plusApplication`), e.g.
 * `:client:settings:demo`. Produces a tiny Compose Multiplatform application targeting Android +
 * JVM desktop that renders a single feature in isolation, so a feature can be built and run without
 * the full `:client:composeApp`.
 *
 * Reuses the shared `compose` convention for Compose setup and, by default, the `metro` convention
 * so demos can wire the real BLoCs through a small per-demo dependency graph.
 */
class PlusApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = extensions.create<PlusApplicationExtension>("plusApplication")

            afterEvaluate {
                if (extension.enableDi) applyMetro()
                if (extension.enableTesting) applyTesting()
            }

            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply(libs.plugins.androidApplication.get().pluginId)
                apply(libs.plugins.compose.get().pluginId)
            }

            applyKtfmt()

            extensions.configure<KotlinMultiplatformExtension> {
                androidTarget { compilerOptions.jvmTarget.set(JvmTarget.JVM_11) }
                jvm()
                compilerOptions.optIn.add("kotlin.time.ExperimentalTime")
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = libs.versions.android.compileSdk.get().toInt()

                defaultConfig {
                    minSdk = libs.versions.android.minSdk.get().toInt()
                    targetSdk = libs.versions.android.targetSdk.get().toInt()
                    versionCode = 1
                    versionName = "1.0"
                }

                packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_11
                    targetCompatibility = JavaVersion.VERSION_11
                }
            }

            // namespace / applicationId come from the plusApplication extension, which isn't
            // populated until the module's build.gradle.kts has been evaluated — defer reading
            // them to finalizeDsl (same approach as KmpLibraryConventionPlugin).
            extensions.getByType(ApplicationAndroidComponentsExtension::class.java).finalizeDsl {
                app ->
                val ns =
                    extension.namespace
                        ?: throw IllegalStateException(
                            """
                        Please set the namespace for the demo app $name in the plusApplication extension in the module's build.gradle.kts file.
                        Example:
                        plusApplication {
                            namespace = "com.plusmobileapps.chefmate.$name.demo"
                        }
                    """
                                .trimIndent()
                        )
                app.namespace = ns
                app.defaultConfig.applicationId = extension.applicationId ?: ns
            }
        }
    }
}
