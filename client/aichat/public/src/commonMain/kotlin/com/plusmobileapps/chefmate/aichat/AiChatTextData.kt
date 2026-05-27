package com.plusmobileapps.chefmate.aichat

import chefmate.client.aichat.public.generated.resources.Res
import chefmate.client.aichat.public.generated.resources.aichat_error_generic
import chefmate.client.aichat.public.generated.resources.aichat_error_no_api_key
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.TextData

val AiChatNoApiKeyError: TextData = ResourceString(Res.string.aichat_error_no_api_key)

val AiChatGenericError: TextData = ResourceString(Res.string.aichat_error_generic)
