package com.plusmobileapps.chefmate.browser

import chefmate.client.browser.public.generated.resources.Res
import chefmate.client.browser.public.generated.resources.ic_engine_bing
import chefmate.client.browser.public.generated.resources.ic_engine_brave
import chefmate.client.browser.public.generated.resources.ic_engine_duckduckgo
import chefmate.client.browser.public.generated.resources.ic_engine_google
import org.jetbrains.compose.resources.DrawableResource

/** The brand logo drawable for a [SearchEngine], used by the picker and landing dropdown. */
val SearchEngine.logo: DrawableResource
    get() =
        when (this) {
            SearchEngine.GOOGLE -> Res.drawable.ic_engine_google
            SearchEngine.DUCK_DUCK_GO -> Res.drawable.ic_engine_duckduckgo
            SearchEngine.BING -> Res.drawable.ic_engine_bing
            SearchEngine.BRAVE -> Res.drawable.ic_engine_brave
        }
