package com.plusmobileapps.chefmate.convention

import org.gradle.api.Project

/**
 * Helper functions for diagnosing plugin issues
 */
object PluginDiagnostics {
    fun logDiStatus(project: Project, extension: PlusLibraryExtension) {
        project.logger.lifecycle("Project: ${project.name} - DI Enabled: ${extension.enableDi}")
    }
}
