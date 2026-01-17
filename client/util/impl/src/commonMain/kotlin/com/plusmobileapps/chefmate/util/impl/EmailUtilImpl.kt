package com.plusmobileapps.chefmate.util.impl

import com.plusmobileapps.chefmate.util.EmailUtil
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@Inject
@ContributesBinding(AppScope::class, boundType = EmailUtil::class)
@SingleIn(AppScope::class)
class EmailUtilImpl : EmailUtil {
    private val emailAddressRegex =
        Regex(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+",
        )

    override fun isValidEmail(email: String): Boolean = email.matches(emailAddressRegex)
}
