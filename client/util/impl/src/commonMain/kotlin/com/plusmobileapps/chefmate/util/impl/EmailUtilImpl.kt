package com.plusmobileapps.chefmate.util.impl

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.util.EmailUtil
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@ContributesBinding(AppScope::class)
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
                ")+"
        )

    override fun isValidEmail(email: String): Boolean = email.matches(emailAddressRegex)
}
