@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.family.core.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.ChefMateUser
import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.family.core.FamilyBloc
import com.plusmobileapps.chefmate.family.data.AlreadyInFamilyException
import com.plusmobileapps.chefmate.family.data.Family
import com.plusmobileapps.chefmate.family.data.FamilyInvite
import com.plusmobileapps.chefmate.family.data.FamilyMember
import com.plusmobileapps.chefmate.family.data.testing.FakeFamilyRepository
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

class FamilyBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<FamilyBloc.Output>()

    private val familyState = MutableStateFlow<Family?>(null)
    private val membersState = MutableStateFlow<List<FamilyMember>>(emptyList())
    private val invitesState = MutableStateFlow<List<FamilyInvite>>(emptyList())
    private val repository = FakeFamilyRepository(familyState, membersState, invitesState)

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

    private fun bloc() =
        FamilyBlocImpl(
            context = context,
            output = output,
            viewModelFactory = {
                FamilyViewModel(
                    mainContext = kotlinx.coroutines.Dispatchers.Unconfined,
                    repository = repository,
                    authenticationRepository = authRepository,
                )
            },
        )

    @Test
    fun When_not_in_a_family_Then_the_model_has_no_family_and_no_members() = runTest {
        bloc().state.test {
            val model = awaitItem()
            model.isLoading shouldBe false
            model.family shouldBe null
            model.members shouldBe emptyList()
            model.isOwner shouldBe false
        }
    }

    @Test
    fun When_anonymous_Then_the_screen_reports_signed_out() = runTest {
        authRepository.setAnonymous()

        bloc().state.test { awaitItem().isSignedIn shouldBe false }
    }

    @Test
    fun When_creating_a_family_Then_the_name_field_is_cleared() = runTest {
        val bloc = bloc()

        bloc.state.test {
            awaitItem()
            bloc.onNewFamilyNameChanged("The Hendersons")
            awaitItem().newFamilyName shouldBe "The Hendersons"

            bloc.onCreateFamilyClicked()

            // isCreating flips true then back to false with the field cleared.
            skipItems(1)
            val created = awaitItem()
            created.newFamilyName shouldBe ""
            created.family shouldNotBe null
        }
    }

    @Test
    fun When_creating_fails_because_already_in_a_family_Then_a_specific_error_is_shown() = runTest {
        repository.errorToThrow = AlreadyInFamilyException()
        val bloc = bloc()

        bloc.state.test {
            awaitItem()
            bloc.onNewFamilyNameChanged("Second Family")
            awaitItem()

            bloc.onCreateFamilyClicked()
            skipItems(1)

            val failed = awaitItem()
            failed.createError shouldNotBe null
            failed.isCreating shouldBe false
            // The typed name is kept so the user can act on the error without retyping.
            failed.newFamilyName shouldBe "Second Family"
        }
    }

    @Test
    fun When_the_user_owns_the_family_Then_owner_controls_are_enabled() = runTest {
        familyState.value = Family.Sample
        membersState.value = FamilyMember.Samples

        bloc().state.test {
            val model = awaitItem()
            model.isOwner shouldBe true
            model.members.size shouldBe FamilyMember.Samples.size
        }
    }

    @Test
    fun When_the_user_is_a_member_Then_they_are_not_the_owner() = runTest {
        familyState.value = Family.Sample.copy(isOwnedByCurrentUser = false)

        bloc().state.test { awaitItem().isOwner shouldBe false }
    }

    @Test
    fun When_inviting_Then_the_email_field_is_cleared_and_the_invite_is_sent() = runTest {
        familyState.value = Family.Sample
        val bloc = bloc()

        bloc.state.test {
            awaitItem()
            bloc.onInviteEmailChanged("alex@example.com")
            awaitItem()

            bloc.onInviteClicked()
            skipItems(1)

            awaitItem().inviteEmail shouldBe ""
        }
        repository.invitedEmails shouldBe listOf("alex@example.com")
    }

    @Test
    fun When_renaming_Then_the_inline_field_opens_seeded_with_the_current_name() = runTest {
        familyState.value = Family.Sample
        val bloc = bloc()

        bloc.state.test {
            awaitItem()
            bloc.onRenameClicked()
            awaitItem().editingName shouldBe Family.Sample.name

            bloc.onRenameCancelled()
            awaitItem().editingName shouldBe null
        }
    }

    @Test
    fun When_removing_a_member_Then_confirmation_is_required_first() = runTest {
        familyState.value = Family.Sample
        membersState.value = FamilyMember.Samples
        val bloc = bloc()

        bloc.state.test {
            awaitItem()
            bloc.onRemoveMemberClicked("member-2")
            awaitItem().removingMember?.email shouldBe FamilyMember.SampleMember.email

            bloc.onConfirmRemoveMember()
            awaitItem().removingMember shouldBe null
            // The member list then re-emits without them.
            awaitItem().members.none { it.id == "member-2" } shouldBe true
        }
        repository.removedMemberIds shouldBe listOf("member-2")
    }

    @Test
    fun When_dismissing_the_remove_dialog_Then_nothing_is_removed() = runTest {
        familyState.value = Family.Sample
        membersState.value = FamilyMember.Samples
        val bloc = bloc()

        bloc.state.test {
            awaitItem()
            bloc.onRemoveMemberClicked("member-2")
            awaitItem()

            bloc.onDismissRemoveMember()
            awaitItem().removingMember shouldBe null
        }
        repository.removedMemberIds shouldBe emptyList()
    }

    @Test
    fun When_leaving_the_family_Then_it_is_confirmed_before_acting() = runTest {
        familyState.value = Family.Sample.copy(isOwnedByCurrentUser = false)
        val bloc = bloc()

        bloc.state.test {
            awaitItem()
            bloc.onLeaveFamilyClicked()
            awaitItem().pendingFamilyAction shouldBe FamilyBloc.FamilyAction.LEAVE

            bloc.onConfirmFamilyAction()
            skipItems(1)
            awaitItem().family shouldBe null
        }
    }

    @Test
    fun When_back_is_clicked_Then_the_bloc_outputs_Back() = runTest {
        bloc().onBack()

        output.values.last() shouldBe FamilyBloc.Output.Back
    }

    @Test
    fun When_signed_out_and_sign_in_is_tapped_Then_the_auth_flow_is_requested() = runTest {
        bloc().onSignInClicked()

        output.values.last() shouldBe FamilyBloc.Output.OpenSignIn
    }
}
