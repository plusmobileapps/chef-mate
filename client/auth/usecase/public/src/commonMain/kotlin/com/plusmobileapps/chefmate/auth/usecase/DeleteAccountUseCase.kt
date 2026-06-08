package com.plusmobileapps.chefmate.auth.usecase

/**
 * Deletes the signed-in user's remote account, then signs out and wipes all local data. Returns a
 * failure if the remote deletion fails so callers can surface an error and leave the user signed
 * in.
 */
fun interface DeleteAccountUseCase {
    suspend operator fun invoke(): Result<Unit>
}
