package com.plusmobileapps.chefmate.settings

import chefmate.client.settings.public.generated.resources.Res
import chefmate.client.settings.public.generated.resources.greeting_authenticated
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.TextData

fun createGreeting(userName: String): TextData =
    PhraseModel(
        resource = Res.string.greeting_authenticated,
        "name" to FixedString(userName),
    )
