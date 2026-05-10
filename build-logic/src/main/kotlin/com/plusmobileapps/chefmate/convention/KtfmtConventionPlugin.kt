package com.plusmobileapps.chefmate.convention

import com.ncorti.ktfmt.gradle.KtfmtExtension
import com.ncorti.ktfmt.gradle.KtfmtPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class KtfmtConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.applyKtfmt()
    }
}

fun Project.applyKtfmt() {
    apply<KtfmtPlugin>()

    configure<KtfmtExtension> {
        kotlinLangStyle()
        // Exclude all build outputs — BuildKonfig writes to build/buildkonfig/, and the rest of
        // build/ is generated code that we don't author.
        srcSetPathExclusionPattern.set(Regex(".*(generated|/build/).*"))
    }
}
