@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.profile.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.ChefMateUser
import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.testing.FakeProfilePhotoStorage
import com.plusmobileapps.chefmate.auth.usecase.DeleteAccountUseCase
import com.plusmobileapps.chefmate.profile.ManageProfileBloc
import com.plusmobileapps.chefmate.profile.data.SocialProfile
import com.plusmobileapps.chefmate.profile.data.testing.FakeProfileRepository
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import com.plusmobileapps.chefmate.util.PickedImage
import io.kotest.matchers.shouldBe
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

class ManageProfileBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<ManageProfileBloc.Output>()
    private val authRepository =
        FakeAuthenticationRepository().apply {
            setState(
                AuthState.Authenticated(
                    ChefMateUser(
                        userId = "id-1",
                        userName = "Original Name",
                        userEmail = "chef@example.com",
                        userProfileImageUrl = null,
                    )
                )
            )
        }
    private val photoStorage = FakeProfilePhotoStorage()
    private val profileRepository = FakeProfileRepository().apply { currentUserId = "id-1" }
    private var deleteCalled = false
    private val deleteAccountUseCase = DeleteAccountUseCase {
        deleteCalled = true
        Result.success(Unit)
    }

    private val bloc by lazy { buildBloc() }

    /**
     * Builds a fresh bloc. Tests that seed [profileRepository] first use this directly, since the
     * profile load happens in the ViewModel's init.
     *
     * [mainContext] defaults to the context's own dispatcher, which has its own scheduler. Tests
     * that need to advance past the handle-availability debounce must pass
     * `UnconfinedTestDispatcher(testScheduler)` so `advanceUntilIdle` actually reaches it.
     */
    private fun buildBloc(mainContext: CoroutineContext = context.mainContext): ManageProfileBloc =
        ManageProfileBlocImpl(
            context = TestBlocContext.create(),
            output = output,
            viewModelFactory = {
                ManageProfileViewModel(
                    mainContext = mainContext,
                    authenticationRepository = authRepository,
                    profilePhotoStorage = photoStorage,
                    profileRepository = profileRepository,
                    deleteAccountUseCase = deleteAccountUseCase,
                )
            },
        )

    @Test
    fun When_opened_Then_state_is_seeded_from_signed_in_user() {
        bloc.state.value.displayName shouldBe "Original Name"
        bloc.state.value.email shouldBe "chef@example.com"
    }

    @Test
    fun When_name_changed_and_saved_Then_profile_is_updated_and_back_emitted() = runTest {
        bloc.onDisplayNameChanged("New Name")
        bloc.onSaveClicked()

        authRepository.lastUpdatedDisplayName shouldBe "New Name"
        output.lastValue shouldBe ManageProfileBloc.Output.Back
    }

    @Test
    fun When_photo_picked_and_saved_Then_photo_uploaded_and_url_sent() = runTest {
        photoStorage.uploadResult = "https://example.com/avatars/id-1/avatar.png"
        bloc.onPhotoPicked(PickedImage(bytes = byteArrayOf(1, 2, 3), fileExtension = "png"))
        bloc.onSaveClicked()

        photoStorage.lastUploadedBytes shouldBe byteArrayOf(1, 2, 3)
        authRepository.lastUpdatedAvatarUrl shouldBe "https://example.com/avatars/id-1/avatar.png"
    }

    @Test
    fun When_existing_photo_replaced_and_saved_Then_old_photo_deleted() = runTest {
        authRepository.setState(
            AuthState.Authenticated(
                ChefMateUser(
                    userId = "id-1",
                    userName = "Original Name",
                    userEmail = "chef@example.com",
                    userProfileImageUrl = "https://example.com/avatars/id-1/old.png",
                )
            )
        )
        photoStorage.uploadResult = "https://example.com/avatars/id-1/new.png"
        bloc.onPhotoPicked(PickedImage(bytes = byteArrayOf(1, 2, 3), fileExtension = "png"))
        bloc.onSaveClicked()

        photoStorage.deletedUrls shouldBe listOf("https://example.com/avatars/id-1/old.png")
    }

    @Test
    fun When_first_photo_added_and_saved_Then_nothing_deleted() = runTest {
        photoStorage.uploadResult = "https://example.com/avatars/id-1/new.png"
        bloc.onPhotoPicked(PickedImage(bytes = byteArrayOf(1, 2, 3), fileExtension = "png"))
        bloc.onSaveClicked()

        photoStorage.deletedUrls.isEmpty() shouldBe true
    }

    @Test
    fun When_only_name_changed_and_saved_Then_existing_photo_kept() = runTest {
        authRepository.setState(
            AuthState.Authenticated(
                ChefMateUser(
                    userId = "id-1",
                    userName = "Original Name",
                    userEmail = "chef@example.com",
                    userProfileImageUrl = "https://example.com/avatars/id-1/old.png",
                )
            )
        )
        bloc.onDisplayNameChanged("New Name")
        bloc.onSaveClicked()

        photoStorage.deletedUrls.isEmpty() shouldBe true
    }

    @Test
    fun When_save_fails_Then_save_error_is_surfaced() = runTest {
        authRepository.updateProfileResult = Result.failure(RuntimeException("boom"))

        bloc.state.test {
            awaitItem() // seeded
            bloc.onSaveClicked()

            var item = awaitItem()
            while (item.saveError == null) item = awaitItem()
            item.isSaving shouldBe false
            cancelAndIgnoreRemainingEvents()
        }
        output.values.isEmpty() shouldBe true
    }

    @Test
    fun When_delete_clicked_Then_dialog_is_shown() = runTest {
        bloc.state.test {
            awaitItem()
            bloc.onDeleteAccountClicked()
            awaitItem().showDeleteDialog shouldBe true
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun When_delete_dismissed_Then_dialog_is_hidden() = runTest {
        bloc.state.test {
            awaitItem()
            bloc.onDeleteAccountClicked()
            awaitItem().showDeleteDialog shouldBe true
            bloc.onDeleteDismissed()
            awaitItem().showDeleteDialog shouldBe false
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun When_delete_confirmed_with_matching_email_Then_account_deleted_and_output_emitted() =
        runTest {
            bloc.onDeleteAccountClicked()
            bloc.onDeleteConfirmationChanged("chef@example.com")
            bloc.onDeleteConfirmed()

            deleteCalled shouldBe true
            output.lastValue shouldBe ManageProfileBloc.Output.AccountDeleted
        }

    @Test
    fun When_confirmation_matches_email_ignoring_case_and_whitespace_Then_delete_is_enabled() =
        runTest {
            bloc.onDeleteAccountClicked()
            bloc.onDeleteConfirmationChanged("  CHEF@example.com  ")

            bloc.state.value.canConfirmDelete shouldBe true
        }

    @Test
    fun When_confirmation_does_not_match_email_Then_delete_is_blocked() = runTest {
        bloc.onDeleteAccountClicked()
        bloc.onDeleteConfirmationChanged("wrong@example.com")
        bloc.state.value.canConfirmDelete shouldBe false

        bloc.onDeleteConfirmed()

        deleteCalled shouldBe false
        output.values.isEmpty() shouldBe true
    }

    @Test
    fun When_delete_dismissed_Then_confirmation_is_cleared() = runTest {
        bloc.state.test {
            awaitItem()
            bloc.onDeleteAccountClicked()
            awaitItem().showDeleteDialog shouldBe true
            bloc.onDeleteConfirmationChanged("chef@example.com")
            awaitItem().deleteConfirmation shouldBe "chef@example.com"
            bloc.onDeleteDismissed()
            awaitItem().deleteConfirmation shouldBe ""
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun When_handle_typed_Then_it_is_normalized() = runTest {
        bloc.onHandleChanged("  @JuliaChild  ")

        bloc.state.value.handle shouldBe "juliachild"
    }

    @Test
    fun When_handle_is_malformed_Then_it_is_rejected_without_a_server_call() = runTest {
        bloc.onHandleChanged("ab")

        bloc.state.value.handleStatus shouldBe ManageProfileBloc.HandleStatus.InvalidFormat
        bloc.state.value.canSave shouldBe false
    }

    @Test
    fun When_handle_is_free_Then_it_is_available_and_save_is_enabled() = runTest {
        val bloc = buildBloc(UnconfinedTestDispatcher(testScheduler))

        bloc.onHandleChanged("juliachild")
        advanceUntilIdle()

        bloc.state.value.handleStatus shouldBe ManageProfileBloc.HandleStatus.Available
        bloc.state.value.canSave shouldBe true
    }

    @Test
    fun When_handle_is_taken_Then_save_is_blocked() = runTest {
        profileRepository.addProfile(socialProfile(id = "someone-else", handle = "juliachild"))
        val bloc = buildBloc(UnconfinedTestDispatcher(testScheduler))

        bloc.onHandleChanged("juliachild")
        advanceUntilIdle()

        bloc.state.value.handleStatus shouldBe ManageProfileBloc.HandleStatus.Taken
        bloc.state.value.canSave shouldBe false
    }

    @Test
    fun When_saving_with_a_handle_Then_the_profile_is_claimed() = runTest {
        val bloc = buildBloc(UnconfinedTestDispatcher(testScheduler))

        bloc.onHandleChanged("juliachild")
        bloc.onBioChanged("French cooking, demystified.")
        advanceUntilIdle()

        bloc.onSaveClicked()
        advanceUntilIdle()

        profileRepository.lastClaimedHandle shouldBe "juliachild"
        profileRepository.profileFor("juliachild")?.bio shouldBe "French cooking, demystified."
        bloc.state.value.isHandleClaimed shouldBe true
        output.lastValue shouldBe ManageProfileBloc.Output.Back
    }

    @Test
    fun When_saving_without_a_handle_Then_no_profile_is_created() = runTest {
        // A public profile is opt-in — changing only your display name must not mint one.
        bloc.onDisplayNameChanged("New Name")
        bloc.onSaveClicked()
        advanceUntilIdle()

        profileRepository.claimHandleCallCount shouldBe 0
        output.lastValue shouldBe ManageProfileBloc.Output.Back
    }

    @Test
    fun When_the_handle_is_claimed_between_check_and_save_Then_an_error_is_shown() = runTest {
        val bloc = buildBloc(UnconfinedTestDispatcher(testScheduler))

        bloc.onHandleChanged("juliachild")
        advanceUntilIdle()
        bloc.state.value.canSave shouldBe true
        // Someone else wins the race after the availability check passed.
        profileRepository.addProfile(socialProfile(id = "someone-else", handle = "juliachild"))

        bloc.onSaveClicked()
        advanceUntilIdle()

        bloc.state.value.handleStatus shouldBe ManageProfileBloc.HandleStatus.Taken
        (bloc.state.value.saveError != null) shouldBe true
        bloc.state.value.isHandleClaimed shouldBe false
        output.values.isEmpty() shouldBe true
    }

    @Test
    fun When_a_profile_already_exists_Then_the_handle_loads_locked() = runTest {
        profileRepository.addProfile(
            socialProfile(id = "id-1", handle = "juliachild", bio = "Bon appétit.")
        )
        // Rebuild so the bloc's init-time load sees the seeded profile.
        val loaded = buildBloc(UnconfinedTestDispatcher(testScheduler))
        advanceUntilIdle()

        loaded.state.value.handle shouldBe "juliachild"
        loaded.state.value.isHandleClaimed shouldBe true
        loaded.state.value.bio shouldBe "Bon appétit."

        // Handles are permanent, so edits are ignored rather than silently failing on save.
        loaded.onHandleChanged("someoneelse")
        loaded.state.value.handle shouldBe "juliachild"
    }

    private fun socialProfile(id: String, handle: String, bio: String = "") =
        SocialProfile(
            id = id,
            handle = handle,
            displayName = "Julia Child",
            bio = bio,
            avatarUrl = null,
        )
}
