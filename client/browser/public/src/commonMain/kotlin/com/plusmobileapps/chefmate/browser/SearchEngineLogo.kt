package com.plusmobileapps.chefmate.browser

import chefmate.client.browser.public.generated.resources.Res
import chefmate.client.browser.public.generated.resources.bing
import chefmate.client.browser.public.generated.resources.brave
import chefmate.client.browser.public.generated.resources.duckduckgo
import chefmate.client.browser.public.generated.resources.google
import org.jetbrains.compose.resources.DrawableResource

/** The brand logo drawable for a [SearchEngine], used by the picker and landing dropdown. */
val SearchEngine.logo: DrawableResource
    get() =
        when (this) {
            SearchEngine.GOOGLE -> Res.drawable.google
            SearchEngine.DUCK_DUCK_GO -> Res.drawable.duckduckgo
            SearchEngine.BING -> Res.drawable.bing
            SearchEngine.BRAVE -> Res.drawable.brave
        }
