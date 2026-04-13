package com.plusmobileapps.chefmate.browser

import chefmate.client.browser.public.generated.resources.Res
import chefmate.client.browser.public.generated.resources.browser_extraction_failed
import chefmate.client.browser.public.generated.resources.browser_recipe_saved
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.TextData

fun createRecipeSavedMessage(): TextData = ResourceString(Res.string.browser_recipe_saved)

fun createExtractionFailedMessage(): TextData = ResourceString(Res.string.browser_extraction_failed)
