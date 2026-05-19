package com.plusmobileapps.chefmate.auth.usecase

interface SignInUseCase {
    /**
     * Number of locally-saved guest recipes that would be discarded by a sign-in. Returns 0 when
     * the current session isn't anonymous (a real-user sign-in shouldn't have guest data anyway —
     * this is the gate the UI uses to decide whether to show a "you'll lose X recipes" dialog).
     */
    suspend fun guestRecipesToDiscard(): Int

    /**
     * Wipes all local guest data (recipes, meal plans, grocery lists) and then signs in to the
     * existing account. The post-sign-in sync repopulates from the real account's cloud data.
     *
     * The wipe happens before the sign-in attempt, so a failed sign-in leaves the user as
     * anon-empty; their (still-on-server) anon recipes will re-sync the next time the auth state
     * changes — typically when they successfully sign in.
     */
    suspend operator fun invoke(email: String, password: String): Result<Unit>
}
