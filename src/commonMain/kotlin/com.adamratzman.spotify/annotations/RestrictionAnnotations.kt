package com.adamratzman.spotify.annotations

/**
 * The underlying endpoint or field is only available in Extended Quota Mode.
 * It is **not available** in development mode!
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "Requires Extended Quota Mode"
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
public annotation class SpotifyExtendedQuota