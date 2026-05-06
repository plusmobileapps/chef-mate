package com.plusmobileapps.chefmate.testing

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.plusmobileapps.chefmate.ApplicationComponent
import com.plusmobileapps.chefmate.client.database.DriverFactory
import com.plusmobileapps.chefmate.cook.data.CookingSessionRepository
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.plusmobileapps.chefmate.util.DateTimeFormatterUtil
import com.plusmobileapps.chefmate.util.DateTimeFormatterUtilImpl
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Test-only DI graph mirroring the production AndroidApplicationComponent. Compiled in
 * androidInstrumentedTest so [FakeRecipeRemoteDataSource]'s `replaces` contribution overrides the
 * production Supabase remote binding.
 *
 * Exposes the repositories that the e2e test needs to seed before rendering the app.
 */
@DependencyGraph(AppScope::class)
@SingleIn(AppScope::class)
abstract class TestAndroidApplicationComponent : ApplicationComponent {
    abstract val recipeRepository: RecipeRepository
    abstract val cookingSessionRepository: CookingSessionRepository

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides context: Context): TestAndroidApplicationComponent
    }

    @Provides fun driverFactory(context: Context): DriverFactory = DriverFactory(context = context)

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @SingleIn(AppScope::class)
    fun provideDateTimeFormatterUtil(): DateTimeFormatterUtil = DateTimeFormatterUtilImpl()
}
