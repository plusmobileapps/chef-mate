@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.util.impl

import com.plusmobileapps.chefmate.util.PasswordValidator
import com.plusmobileapps.chefmate.util.PasswordValidator.Requirement
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PasswordValidatorImplTest {

    private val validator = PasswordValidatorImpl()

    @Test
    fun When_password_meets_all_requirements_Then_returns_Valid() {
        validator.validate("Abc123!@") shouldBe PasswordValidator.Result.Valid
    }

    @Test
    fun When_password_is_empty_Then_all_requirements_missing() {
        val result = validator.validate("")

        result shouldBe
            PasswordValidator.Result.Invalid(
                setOf(
                    Requirement.MinLength,
                    Requirement.Lowercase,
                    Requirement.Uppercase,
                    Requirement.Digit,
                    Requirement.Symbol,
                )
            )
    }

    @Test
    fun When_only_lowercase_missing_Then_only_lowercase_returned() {
        // 6+ chars, uppercase, digit, symbol — but no lowercase.
        val result = validator.validate("ABC123!")

        result shouldBe PasswordValidator.Result.Invalid(setOf(Requirement.Lowercase))
    }

    @Test
    fun When_only_uppercase_missing_Then_only_uppercase_returned() {
        val result = validator.validate("abc123!")

        result shouldBe PasswordValidator.Result.Invalid(setOf(Requirement.Uppercase))
    }

    @Test
    fun When_only_digit_missing_Then_only_digit_returned() {
        val result = validator.validate("Abcdef!")

        result shouldBe PasswordValidator.Result.Invalid(setOf(Requirement.Digit))
    }

    @Test
    fun When_only_symbol_missing_Then_only_symbol_returned() {
        val result = validator.validate("Abc1234")

        result shouldBe PasswordValidator.Result.Invalid(setOf(Requirement.Symbol))
    }

    @Test
    fun When_password_shorter_than_six_Then_min_length_missing() {
        // 5 chars but has all four character classes — only MinLength should fail.
        val result = validator.validate("Ab1!c")

        result shouldBe PasswordValidator.Result.Invalid(setOf(Requirement.MinLength))
    }

    @Test
    fun Whitespace_alone_does_not_count_as_a_symbol() {
        // Length OK, lowercase OK, uppercase OK, digit OK — but the only non-letter-digit char
        // is whitespace, which is excluded from the symbol class.
        val result = validator.validate("Abc 123")

        result shouldBe PasswordValidator.Result.Invalid(setOf(Requirement.Symbol))
    }
}
