/* Spotify Web API, Kotlin Wrapper; MIT License, 2017-2022; Original author: Adam Ratzman */
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.adamratzman.spotify.priv

import com.adamratzman.spotify.AbstractTest
import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.SpotifyException.BadRequestException
import com.adamratzman.spotify.runTestOnDefaultDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlin.test.*

class ClientEpisodeApiTest : AbstractTest<SpotifyClientApi>() {
    @Test
    fun testGetEpisode(): TestResult = runTestOnDefaultDispatcher {
        buildApi<SpotifyClientApi>(::testGetEpisode.name)
        if (!isApiInitialized()) return@runTestOnDefaultDispatcher

        assertNull(api.episodes.getEpisode("nonexistant episode"))
        assertNotNull(api.episodes.getEpisode("4fvIbnHhHaD8xljXI0uRXr"))
    }

    @Ignore // requires extended quota mode
    @Test
    fun testGetEpisodes(): TestResult = runTestOnDefaultDispatcher {
        buildApi<SpotifyClientApi>(::testGetEpisodes.name)
        if (!isApiInitialized()) return@runTestOnDefaultDispatcher

        assertFailsWith<BadRequestException> { api.episodes.getEpisodes("hi", "dad") }
        assertFailsWith<BadRequestException> { api.episodes.getEpisodes("4fvIbnHhHaD8xljXI0uRXr", "j")[1] }

        assertEquals(
            listOf("The Midterms Begin With a Texas-Size Showdown"),
            api.episodes.getEpisodes("4fvIbnHhHaD8xljXI0uRXr").map { it?.name }
        )
    }
}
