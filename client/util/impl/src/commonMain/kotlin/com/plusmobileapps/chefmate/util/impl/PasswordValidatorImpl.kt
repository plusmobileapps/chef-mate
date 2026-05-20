package com.plusmobileapps.chefmate.util.impl

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.util.PasswordValidator
import com.plusmobileapps.chefmate.util.PasswordValidator.Requirement
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class PasswordValidatorImpl : PasswordValidator {
    override fun validate(password: String): PasswordValidator.Result {
        val missing = buildSet {
            if (password.length < MIN_LENGTH) add(Requirement.MinLength)
            if (!password.any(Char::isLowerCase)) add(Requirement.Lowercase)
            if (!password.any(Char::isUpperCase)) add(Requirement.Uppercase)
            if (!password.any(Char::isDigit)) add(Requirement.Digit)
            if (!password.any { !it.isLetterOrDigit() && !it.isWhitespace() }) {
                add(Requirement.Symbol)
            }
        }
        return if (missing.isEmpty()) {
            PasswordValidator.Result.Valid
        } else {
            PasswordValidator.Result.Invalid(missing)
        }
    }

    companion object {
        private const val MIN_LENGTH = 6
    }
}
