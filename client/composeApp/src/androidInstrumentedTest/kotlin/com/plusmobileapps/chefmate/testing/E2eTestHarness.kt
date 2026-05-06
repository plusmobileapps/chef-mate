@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.testing

import androidx.compose.ui.test.ComposeUiTest
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
 *   [ComposeUiTest].
 *
 * Usage (inside `runComposeUiTest { ... }`):
 * ```
 * @Test fun example() = runComposeUiTest {
 *     val harness = E2eTestHarness(this)
 *     runBlocking { harness.component.recipeRepository.createRecipe(...) }
 *     harness.launchApp()
 *     RecipeListRobot(this).openRecipe("...")
 * }
 * ```
 */
class E2eTestHarness(private val test: ComposeUiTest) {
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
        test.setContent { App(rootBloc) }
    }

    private companion object {
        const val DATABASE_NAME = "chefmate.db"
    }
}
