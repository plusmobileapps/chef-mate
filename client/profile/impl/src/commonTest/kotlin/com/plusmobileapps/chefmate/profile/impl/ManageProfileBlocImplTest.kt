@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.profile.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.ChefMateUser
import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.testing.FakeProfilePhotoStorage
import com.plusmobileapps.chefmate.auth.usecase.DeleteAccountUseCase
import com.plusmobileapps.chefmate.profile.ManageProfileBloc
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import com.plusmobileapps.chefmate.util.PickedImage
import io.kotest.matchers.shouldBe
import kotlin.test.Test
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
    private var deleteCalled = false
    private val deleteAccountUseCase = DeleteAccountUseCase {
        deleteCalled = true
        Result.success(Unit)
    }

    private val bloc by lazy {
        ManageProfileBlocImpl(
            context = context,
            output = output,
            viewModelFactory = {
                ManageProfileViewModel(
                    mainContext = context.mainContext,
                    authenticationRepository = authRepository,
                    profilePhotoStorage = photoStorage,
                    deleteAccountUseCase = deleteAccountUseCase,
                )
            },
        )
    }

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
    fun When_delete_confirmed_Then_account_deleted_and_output_emitted() = runTest {
        bloc.onDeleteAccountClicked()
        bloc.onDeleteConfirmed()

        deleteCalled shouldBe true
        output.lastValue shouldBe ManageProfileBloc.Output.AccountDeleted
    }
}
