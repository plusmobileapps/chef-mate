@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.auth.usecase.impl

import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.auth.usecase.SignOutUseCase
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class DeleteAccountUseCaseImplTest {

    private val authenticationRepository = FakeAuthenticationRepository()
    private var signedOut = false
    private val signOutUseCase = SignOutUseCase { signedOut = true }

    private val useCase =
        DeleteAccountUseCaseImpl(
            authenticationRepository = authenticationRepository,
            signOutUseCase = signOutUseCase,
        )

    @Test
    fun When_remote_deletion_succeeds_Then_user_is_signed_out() = runTest {
        val result = useCase()

        result.isSuccess shouldBe true
        authenticationRepository.deleteAccountCallCount shouldBe 1
        signedOut shouldBe true
    }

    @Test
    fun When_remote_deletion_fails_Then_user_is_not_signed_out() = runTest {
        authenticationRepository.deleteAccountResult = Result.failure(RuntimeException("boom"))

        val result = useCase()

        result.isFailure shouldBe true
        signedOut shouldBe false
    }
}
