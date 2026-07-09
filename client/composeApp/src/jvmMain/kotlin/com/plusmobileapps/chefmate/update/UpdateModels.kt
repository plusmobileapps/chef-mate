package com.plusmobileapps.chefmate.update

import kotlinx.serialization.Serializable

/** Update feed published by the `desktop-release` workflow as `latest.json`. */
@Serializable
data class UpdateFeed(val version: String, val notesUrl: String, val downloads: Map<String, String>)

sealed interface UpdateState {
    /** No update known, or the banner was dismissed. */
    data object Idle : UpdateState

    data class Available(val version: String, val downloadUrl: String, val notesUrl: String) :
        UpdateState

    /** [fraction] is -1f when the server does not send a content length. */
    data class Downloading(val fraction: Float) : UpdateState

    data class Ready(val installerPath: String) : UpdateState
}
