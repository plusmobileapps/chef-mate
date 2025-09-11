package com.plusmobileapps.chefmate.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.KtlintPlugin

class KtlintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.applyKtlint()
    }
}

fun Project.applyKtlint() {
    apply<KtlintPlugin>()

    configure<KtlintExtension> {
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
        }
    }
}