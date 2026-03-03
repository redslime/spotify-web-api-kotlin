package com.adamratzman.spotify.annotations

/**
 * The underlying endpoint or field is only available in Extended Quota Mode after March 9, 2026.
 * It is **not available** in development mode!
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Requires Extended Quota Mode after March 9, 2026"
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
public annotation class SpotifyExtendedQuota