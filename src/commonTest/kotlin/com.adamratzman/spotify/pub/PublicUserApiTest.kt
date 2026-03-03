/* Spotify Web API, Kotlin Wrapper; MIT License, 2017-2022; Original author: Adam Ratzman */
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.adamratzman.spotify.pub

import com.adamratzman.spotify.AbstractTest
import com.adamratzman.spotify.GenericSpotifyApi
import com.adamratzman.spotify.runTestOnDefaultDispatcher
import com.adamratzman.spotify.utils.catch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PublicUserApiTest : AbstractTest<GenericSpotifyApi>() {
    @Ignore // requires extended quota mode
    @Test
    fun testPublicUser(): TestResult = runTestOnDefaultDispatcher {
        buildApi(::testPublicUser.name)

        assertTrue(catch { api.users.getProfile("adamratzman1")!!.followers?.total } != null)
        assertNull(api.users.getProfile("ejwkfjwkerfjkwerjkfjkwerfjkjksdfjkasdf"))
    }
}
