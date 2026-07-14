@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.notifications.impl

import app.cash.turbine.test
import chefmate.client.notifications.public.generated.resources.Res
import chefmate.client.notifications.public.generated.resources.notifications_accept_error
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.ChefMateUser
import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.grocery.data.ListRole
import com.plusmobileapps.chefmate.notifications.NotificationsBloc
import com.plusmobileapps.chefmate.notifications.data.AppNotification
import com.plusmobileapps.chefmate.notifications.data.testing.FakeNotificationsRepository
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRole
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.toast.testing.FakeToastService
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class NotificationsBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<NotificationsBloc.Output>()
    private val groceryInvite =
        AppNotification.GroceryInvite("g1", "Weeknight Dinners", ListRole.EDITOR)
    private val recipeBookInvite =
        AppNotification.RecipeBookInvite("b1", "Holiday Baking", RecipeBookRole.EDITOR)

    private val notificationsRepository =
        FakeNotificationsRepository(initial = listOf(groceryInvite, recipeBookInvite))
    private val authRepository =
        FakeAuthenticationRepository().apply {
            setState(
                AuthState.Authenticated(
                    ChefMateUser(
                        userId = "id-1",
                        userName = "Chef",
                        userEmail = "chef@example.com",
                        userProfileImageUrl = null,
                    )
                )
            )
        }
    private val toastService = FakeToastService()

    private val bloc by lazy {
        NotificationsBlocImpl(
            context = context,
            output = output,
            viewModelFactory = {
                NotificationsViewModel(
                    mainContext = context.mainContext,
                    repository = notificationsRepository,
                    authenticationRepository = authRepository,
                    toastService = toastService,
                )
            },
        )
    }

    @Test
    fun When_opened_Then_state_lists_pending_notifications_for_signed_in_user() = runTest {
        bloc.state.test {
            var item = awaitItem()
            while (item.isLoading) item = awaitItem()
            item.notifications shouldBe listOf(groceryInvite, recipeBookInvite)
            item.isSignedIn shouldBe true
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun When_opened_Then_repository_is_refreshed() {
        bloc // instantiate
        notificationsRepository.refreshCount shouldBe 1
    }

    @Test
    fun When_accept_Then_routed_to_repository() = runTest {
        bloc.onAccept(groceryInvite)

        notificationsRepository.accepted shouldBe listOf(groceryInvite)
    }

    @Test
    fun When_decline_Then_routed_to_repository() = runTest {
        bloc.onDecline(recipeBookInvite)

        notificationsRepository.declined shouldBe listOf(recipeBookInvite)
    }

    @Test
    fun When_accept_fails_Then_error_toast_is_shown() = runTest {
        notificationsRepository.failWith = RuntimeException("boom")

        bloc.onAccept(groceryInvite)

        toastService.shown shouldBe listOf(Res.string.notifications_accept_error.asTextData())
    }

    @Test
    fun When_back_Then_output_emitted() {
        bloc.onBack()

        output.lastValue shouldBe NotificationsBloc.Output.Back
    }

    @Test
    fun When_sign_in_clicked_Then_open_sign_in_output_emitted() {
        bloc.onSignInClicked()

        output.lastValue shouldBe NotificationsBloc.Output.OpenSignIn
    }

    @Test
    fun When_sign_up_clicked_Then_open_sign_up_output_emitted() {
        bloc.onSignUpClicked()

        output.lastValue shouldBe NotificationsBloc.Output.OpenSignUp
    }

    @Test
    fun When_anonymous_Then_state_is_signed_out() = runTest {
        authRepository.setState(
            AuthState.Authenticated(
                ChefMateUser(
                    userId = "anon",
                    userName = "",
                    userEmail = "",
                    userProfileImageUrl = null,
                    isAnonymous = true,
                )
            )
        )

        bloc.state.test {
            var item = awaitItem()
            while (item.isLoading) item = awaitItem()
            item.isSignedIn shouldBe false
            cancelAndIgnoreRemainingEvents()
        }
    }
}
