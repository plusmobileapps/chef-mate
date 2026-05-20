package com.plusmobileapps.chefmate.util

interface PasswordValidator {
    fun validate(password: String): Result

    sealed class Result {
        data object Valid : Result()

        data class Invalid(val missing: Set<Requirement>) : Result()
    }

    /** Order of declaration is the order requirements appear in user-facing messages. */
    enum class Requirement {
        MinLength,
        Lowercase,
        Uppercase,
        Digit,
        Symbol,
    }
}
