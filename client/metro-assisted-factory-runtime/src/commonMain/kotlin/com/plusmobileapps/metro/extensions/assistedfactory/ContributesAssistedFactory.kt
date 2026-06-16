package com.plusmobileapps.metro.extensions.assistedfactory

import kotlin.reflect.KClass

/**
 * In-repo re-declaration of `metro-extensions`' assisted-factory annotation, matching the original
 * fully-qualified name and members. See this module's build.gradle.kts for why it exists.
 *
 * Placed on a BLoC impl (alongside `@AssistedInject`), the metro-extensions KSP processor generates
 * the Metro `@AssistedFactory` and the `@ContributesBinding` bridge to the public `Factory`.
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
annotation class ContributesAssistedFactory(val scope: KClass<*>, val assistedFactory: KClass<*>)
