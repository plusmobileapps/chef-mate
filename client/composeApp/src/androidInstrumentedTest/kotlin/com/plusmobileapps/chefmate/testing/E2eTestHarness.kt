package com.plusmobileapps.chefmate.testing

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.platform.app.InstrumentationRegistry
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.plusmobileapps.chefmate.App
import com.plusmobileapps.chefmate.DefaultBlocContext
import dev.zacsweers.metro.createGraphFactory

/**
 * Bundles the per-test setup for an end-to-end UI test:
 * - wipes the on-device chefmate DB so each test starts clean,
 * - builds a [TestAndroidApplicationComponent] (with [FakeRecipeRemoteDataSource] replacing the
 *   real remote),
 * - exposes the graph so the test can seed data / stub the remote,
 * - provides [launchApp] which builds the real RootBloc and renders [App] on the supplied
 *   [ComposeContentTestRule].
 *
 * Usage:
 * ```
 * @get:Rule val composeRule = createComposeRule()
 *
 * @Test fun example() {
 *     val harness = E2eTestHarness(composeRule)
 *     runBlocking { harness.component.recipeRepository.createRecipe(...) }
 *     harness.launchApp()
 *     RecipeListRobot(composeRule).openRecipe("...")
 * }
 * ```
 */
class E2eTestHarness(private val composeRule: ComposeContentTestRule) {
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val lifecycle = LifecycleRegistry()

    val component: TestAndroidApplicationComponent

    init {
        targetContext.deleteDatabase(DATABASE_NAME)
        component =
            createGraphFactory<TestAndroidApplicationComponent.Factory>().create(targetContext)
    }

    /** The fake exposed for stubbing remote responses before [launchApp]. */
    val fakeRecipeRemoteDataSource: FakeRecipeRemoteDataSource
        get() = component.fakeRecipeRemoteDataSource

    /** Builds the real RootBloc and renders [App] in the test's compose host. */
    fun launchApp() {
        val rootBloc =
            component.rootBlocFactory.create(
                DefaultBlocContext(
                    componentContext = DefaultComponentContext(lifecycle = lifecycle)
                )
            )
        lifecycle.resume()
        composeRule.setContent { App(rootBloc) }
    }

    private companion object {
        const val DATABASE_NAME = "chefmate.db"
    }
}
