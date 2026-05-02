package com.plusmobileapps.chefmate.browser

import chefmate.client.browser.public.generated.resources.Res
import chefmate.client.browser.public.generated.resources.browser_extraction_failed
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.TextData

fun createExtractionFailedMessage(): TextData = ResourceString(Res.string.browser_extraction_failed)
