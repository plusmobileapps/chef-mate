package com.plusmobileapps.chefmate.browser.impl

import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.browser.BrowserLandingBloc
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.CoachMarkController
import com.plusmobileapps.chefmate.di.CoachMarkId
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = BrowserLandingBloc.Factory::class,
)
class BrowserLandingBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<BrowserLandingBloc.Output>,
    private val coachMarkController: CoachMarkController,
) : BrowserLandingBloc, BlocContext by context {

    override val state: StateFlow<BrowserLandingBloc.Model> =
        coachMarkController.activeCoachMark.mapState {
            BrowserLandingBloc.Model(activeCoachMark = it)
        }

    init {
        coachMarkController.request(CoachMarkId.BROWSER_SEARCH)
        // Leaving the screen without dismissing frees the queue so other coach marks can show.
        lifecycle.doOnDestroy { coachMarkController.release(CoachMarkId.BROWSER_SEARCH) }
    }

    override fun onSearchFieldFocused() {
        dismissCoachMark(CoachMarkId.BROWSER_SEARCH)
        output.onNext(BrowserLandingBloc.Output.OpenEditQuery)
    }

    override fun onCoachMarkDismissed(id: String) {
        dismissCoachMark(id)
    }

    /** Mark a coach mark seen, but only if it's the one currently showing. */
    private fun dismissCoachMark(id: String) {
        if (coachMarkController.activeCoachMark.value == id) {
            coachMarkController.dismiss(id)
        }
    }
}
