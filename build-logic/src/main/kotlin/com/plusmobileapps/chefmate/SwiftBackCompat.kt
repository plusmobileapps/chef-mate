package com.plusmobileapps.chefmate

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable

/**
 * RevenueCat's iOS code is Swift, and its object files auto-link the Swift back-compat static
 * libraries (swiftCompatibility56, swiftCompatibilityConcurrency, …) using a search path baked into
 * the published klib — an Xcode install that only exists on RevenueCat's build machine. Frameworks
 * are linked by Xcode, which supplies those libraries anyway, but Kotlin/Native test executables
 * are linked by Gradle outside Xcode and fail with "library 'swiftCompatibility56' not found".
 *
 * Point the iOS test binaries at the *active* toolchain's Swift lib dir for the target SDK. Call
 * this from any module whose iOS test binaries link RevenueCat, directly or transitively. No-op off
 * macOS so non-Apple CI is unaffected.
 */
fun Project.linkSwiftBackCompatIntoIosTestBinaries() {
    if (!System.getProperty("os.name").orEmpty().startsWith("Mac")) return
    val developerDir =
        providers.exec { commandLine("xcode-select", "-p") }.standardOutput.asText.get().trim()

    extensions.configure<KotlinMultiplatformExtension> {
        targets.withType(KotlinNativeTarget::class.java).configureEach {
            if (!name.startsWith("ios")) return@configureEach
            val sdk = if (name.contains("Simulator")) "iphonesimulator" else "iphoneos"
            val swiftLibDir = "$developerDir/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/$sdk"
            binaries.withType(TestExecutable::class.java).configureEach {
                linkerOpts.add("-L$swiftLibDir")
            }
        }
    }
}
