plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.paparazzi)
    alias(libs.plugins.plusKtfmt)
}

apply(plugin = "org.jetbrains.kotlin.android")

androidComponents {
    finalizeDsl {
        it.namespace = "com.plusmobileapps.chefmate.grocery.core.snapshots"
        it.compileSdk = libs.versions.android.compileSdk.get().toInt()
        it.defaultConfig.minSdk = libs.versions.android.minSdk.get().toInt()
        it.compileOptions.sourceCompatibility = JavaVersion.VERSION_11
        it.compileOptions.targetCompatibility = JavaVersion.VERSION_11
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) }
}

dependencies {
    implementation(projects.client.grocery.core.public)
    implementation(projects.client.grocery.data.public)
    implementation(projects.client.ui.public)
    implementation(projects.client.shared)
    implementation(projects.client.text.public)

    testImplementation(libs.junit)
}
