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
        srcSetPathExclusionPattern.set(Regex(".*generated.*"))
    }
}
