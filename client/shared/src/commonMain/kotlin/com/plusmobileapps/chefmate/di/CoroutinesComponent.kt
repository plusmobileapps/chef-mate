package com.plusmobileapps.chefmate.di

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Qualifier
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import kotlin.coroutines.CoroutineContext

@Qualifier
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE,
)
annotation class IO

@Qualifier
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE,
)
annotation class Main

@Qualifier
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE,
)
annotation class CPU

@Qualifier
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE,
)
annotation class Unconfined

@ContributesTo(AppScope::class)
@SingleIn(AppScope::class)
interface CoroutinesComponent {
    @Provides
    @SingleIn(AppScope::class)
    fun io(): @IO CoroutineContext = Dispatchers.IO

    @Provides
    @SingleIn(AppScope::class)
    fun main(): @Main CoroutineContext = Dispatchers.Main

    @Provides
    @SingleIn(AppScope::class)
    fun default(): @CPU CoroutineContext = Dispatchers.Default

    @Provides
    @SingleIn(AppScope::class)
    fun unconfined(): @Unconfined CoroutineContext = Dispatchers.Unconfined
}
