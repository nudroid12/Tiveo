package com.vueo.shared.core.sync

/** Public project identity only. User credentials are never bundled. */
internal object VueoSyncConfig {
    const val URL = ""
    const val KEY = ""
    const val EMAIL_REDIRECT = "virio://account/confirmed"
    val categories = linkedMapOf(
        "profiles" to "Profiles", "favorites" to "Favorites",
        "progress" to "Continue Watching", "history" to "Watched History",
        "addons" to "Installed Addons & Providers",
        "integrations" to "API Keys", "trakt" to "Trakt Account",
    )
}
