package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.notifications.robots.notifications
import com.plusmobileapps.chefmate.recipe.bottomnav.robots.bottomNav
import com.plusmobileapps.chefmate.settings.robots.more
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class NotificationsNavigationUiTest {

    @Test
    fun opening_notifications_from_more_tab_shows_the_screen() = runRootBlocTest {
        bottomNav().clickMoreTab()
        more().awaitDisplayed().clickNotificationsRow()

        notifications().awaitDisplayed().assertDisplayed()
    }
}
