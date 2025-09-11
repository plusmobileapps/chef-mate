package com.plusmobileapps.chefmate

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the
import org.gradle.plugin.use.PluginDependency
import java.util.Locale
import java.util.Properties

internal inline fun <reified T> PluginManager.apply() = apply(T::class.java)

internal fun PluginContainer.apply(plugin: Provider<PluginDependency>): Plugin<*> {
    return apply(plugin.get().pluginId)
}

internal fun String.capitalizeUS() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
}

internal val Project.libs
    get() = this.the<LibrariesForLibs>()

internal val Project.namespace
    get() = "com.plusmobileapps.chefmate." + run {
        val fullPath: List<String> = path.split(":")
        when {
            fullPath.last() == "public" -> fullPath[fullPath.lastIndex - 1]
            else -> fullPath.takeLast(2).joinToString(".")
        }
    }

internal fun Project.apply(plugin: Provider<PluginDependency>) =
    pluginManager.apply(plugin.get().pluginId)