package com.plusmobileapps.chefmate.browser

object BrowserTestTags {
    const val LANDING_SCREEN = "BrowserLandingScreen"
    const val SELECT_ENGINE_SCREEN = "BrowserSelectEngineScreen"

    /** Row for a single engine in the picker; suffix with [SearchEngine.id]. */
    const val ENGINE_OPTION_PREFIX = "BrowserEngineOption_"

    /** The engine dropdown control on the landing screen. */
    const val LANDING_ENGINE_DROPDOWN = "BrowserLandingEngineDropdown"

    fun engineOption(engine: SearchEngine): String = "$ENGINE_OPTION_PREFIX${engine.id}"
}
