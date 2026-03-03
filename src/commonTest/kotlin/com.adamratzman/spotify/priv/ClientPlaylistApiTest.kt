/* Spotify Web API, Kotlin Wrapper; MIT License, 2017-2022; Original author: Adam Ratzman */
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.adamratzman.spotify.priv

import com.adamratzman.spotify.AbstractTest
import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.models.Playlist
import com.adamratzman.spotify.models.SimplePlaylist
import com.adamratzman.spotify.models.toTrackUri
import com.adamratzman.spotify.runTestOnDefaultDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.TestResult
import kotlin.test.*

class ClientPlaylistApiTest : AbstractTest<SpotifyClientApi>() {
    var createdPlaylist: Playlist? = null
    var playlistsBefore: List<SimplePlaylist>? = null

    private suspend fun init() {
        playlistsBefore = api.playlists.getClientPlaylists().getAllItemsNotNull()
        createdPlaylist = api.playlists.createClientPlaylist("this is a test playlist", "description")
    }

    private suspend fun tearDown() {
        if (createdPlaylist != null) {
            coroutineScope {
                val playlistIds = api.playlists.getClientPlaylists().getAllItemsNotNull()
                    .filter { it.name == "this is a test playlist" }
                    .map { it.id }
                val deleteIds = api.following.isFollowingPlaylists(*playlistIds.toTypedArray())
                    .mapIndexed { index, bool ->
                        playlistIds[index] to bool
                    }
                    .filter { it.second }
                    .map { it.first }
                api.playlists.deleteClientPlaylists(*deleteIds.toTypedArray())
            }
        }

        coroutineScope {
            api.playlists.getClientPlaylists(limit = 50).getAllItemsNotNull()
                .filter { it.name == "test playlist" }
                .map { playlist -> async { api.playlists.deleteClientPlaylist(playlist.id) } }
                .awaitAll()
        }
    }

    @Test
    fun testGetClientPlaylists(): TestResult = runTestOnDefaultDispatcher {
        buildApi<SpotifyClientApi>(::testGetClientPlaylists.name)
        if (!isApiInitialized()) return@runTestOnDefaultDispatcher

        init()

        assertEquals(
            api.playlists.getClientPlaylists().getAllItemsNotNull().size - 1,
            playlistsBefore!!.size
        )

        tearDown()
    }

    @Test
    fun testAddAndRemoveChunkedTracks(): TestResult = runTestOnDefaultDispatcher {
        buildApi<SpotifyClientApi>(::testAddAndRemoveChunkedTracks.name)
        if (!isApiInitialized()) return@runTestOnDefaultDispatcher

        init()

        // Getting Spotify-owned playlists is extended-quota-restricted (returns null)
//        val usTop50Uri = "spotify:playlist:37i9dQZEVXbLRQDuF5jeBp"
//        val globalTop50Uri = "spotify:playlist:37i9dQZEVXbMDoHDwVN2tF"
//        val globalViral50Uri = "spotify:playlist:37i9dQZEVXbLiRSasKsNU9"
        val playlistUri = "spotify:playlist:4zUEn5Obw0OHZpxXkbeVwg"

        val tracks = listOf(
//            async { api.playlists.getPlaylist(usTop50Uri)!!.items.getAllItemsNotNull() },
//            async { api.playlists.getPlaylist(globalTop50Uri)!!.items.getAllItemsNotNull() },
//            async { api.playlists.getPlaylist(globalViral50Uri)!!.items.getAllItemsNotNull() }
            async { api.playlists.getPlaylist(playlistUri)!!.items.getAllItemsNotNull() }
        ).awaitAll().flatten().mapNotNull { it.item?.uri }

        api.spotifyApiOptions.allowBulkRequests = true

        suspend fun calculatePlaylistSize(): Int? = api.playlists.getClientPlaylist(createdPlaylist!!.id)!!.toFullPlaylist()!!.items.size
        val sizeBefore = calculatePlaylistSize() ?: 0
        api.playlists.addPlayablesToClientPlaylist(createdPlaylist!!.id, playables = tracks.toTypedArray())
        assertEquals(sizeBefore + tracks.size, calculatePlaylistSize())
        api.playlists.removePlayablesFromClientPlaylist(createdPlaylist!!.id, playables = tracks.toTypedArray())
        assertEquals(sizeBefore, calculatePlaylistSize())

        api.spotifyApiOptions.allowBulkRequests = false

        tearDown()
    }

    @Test
    @Ignore // ignored because Spotify currently broke the ability to change `public` field
    fun testEditPlaylists(): TestResult = runTestOnDefaultDispatcher {
        buildApi<SpotifyClientApi>(::testEditPlaylists.name)
        if (!isApiInitialized()) return@runTestOnDefaultDispatcher

        init()

        api.playlists.changeClientPlaylistDetails(
            createdPlaylist!!.id,
            "test playlist",
            public = false,
            collaborative = true,
            description = "description 2"
        )

        api.playlists.addPlayablesToClientPlaylist(
            createdPlaylist!!.id,
            "3WDIhWoRWVcaHdRwMEHkkS".toTrackUri(),
            "7FjZU7XFs7P9jHI9Z0yRhK".toTrackUri()
        )

        api.playlists.uploadClientPlaylistCover(
            createdPlaylist!!.id,
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Cat03.jpg/240px-Cat03.jpg"
        )

        var updatedPlaylist = api.playlists.getClientPlaylist(createdPlaylist!!.id)!!
        assertNotNull(updatedPlaylist.toFullPlaylist())

        assertTrue(updatedPlaylist.collaborative)
        assertTrue(updatedPlaylist.public == false)
        assertEquals("test playlist", updatedPlaylist.name)
        //assertEquals("description 2", fullPlaylist.description)  <-- spotify is flaky about actually having description set
        assertTrue(updatedPlaylist.items.total == 2 && updatedPlaylist.images?.isNotEmpty() == true)

        api.playlists.reorderClientPlaylistPlayables(updatedPlaylist.id, 1, insertionPoint = 0)

        updatedPlaylist = api.playlists.getClientPlaylist(createdPlaylist!!.id)!!

        assertTrue(updatedPlaylist.toFullPlaylist()?.items?.items?.get(0)?.item?.id == "7FjZU7XFs7P9jHI9Z0yRhK")

        api.playlists.removeAllClientPlaylistPlayables(updatedPlaylist.id)

        updatedPlaylist = api.playlists.getClientPlaylist(createdPlaylist!!.id)!!

        assertTrue(updatedPlaylist.items.total == 0)

        tearDown()
    }

    @Test
    fun testRemovePlaylistPlayables(): TestResult = runTestOnDefaultDispatcher {
        buildApi<SpotifyClientApi>(::testRemovePlaylistPlayables.name)
        if (!isApiInitialized()) return@runTestOnDefaultDispatcher

        init()

        val playableUriOne = "3WDIhWoRWVcaHdRwMEHkkS".toTrackUri()
        val playableUriTwo = "7FjZU7XFs7P9jHI9Z0yRhK".toTrackUri()
        api.playlists.addPlayablesToClientPlaylist(
            createdPlaylist!!.id,
            playableUriOne,
            playableUriOne,
            playableUriTwo,
            playableUriTwo
        )

        assertTrue(api.playlists.getPlaylistTracks(createdPlaylist!!.id).items.size == 4)

        api.playlists.removePlayableFromClientPlaylist(createdPlaylist!!.id, playableUriOne)

        assertEquals(
            listOf(playableUriTwo, playableUriTwo),
            api.playlists.getPlaylistTracks(createdPlaylist!!.id).items.map { it.item?.uri }
        )

        // PlayablePositions are unsupported now (2024)
//        api.playlists.addPlayableToClientPlaylist(createdPlaylist!!.id, playableUriOne)
//
//        api.playlists.removePlayableFromClientPlaylist(
//            createdPlaylist!!.id,
//            playableUriTwo,
//            SpotifyPlayablePositions(1)
//        )
//
//        assertEquals(
//            listOf(playableUriTwo, playableUriOne),
//            api.playlists.getPlaylistTracks(createdPlaylist!!.id).items.map { it.item?.uri }
//        )

        api.playlists.setClientPlaylistPlayables(
            createdPlaylist!!.id,
            playableUriOne,
            playableUriOne,
            playableUriTwo,
            playableUriTwo
        )

        api.playlists.removePlayablesFromClientPlaylist(createdPlaylist!!.id, playableUriOne, playableUriTwo)

        assertTrue(api.playlists.getPlaylistTracks(createdPlaylist!!.id).items.isEmpty())

        // PlayablePositions are unsupported now (2024)
//        api.playlists.setClientPlaylistPlayables(
//            createdPlaylist!!.id,
//            playableUriTwo,
//            playableUriOne,
//            playableUriTwo,
//            playableUriTwo,
//            playableUriOne
//        )
//
//        api.playlists.removePlayablesFromClientPlaylist(
//            createdPlaylist!!.id,
//            Pair(playableUriOne, SpotifyPlayablePositions(4)),
//            Pair(playableUriTwo, SpotifyPlayablePositions(0))
//        )
//
//        assertEquals(
//            listOf(playableUriOne, playableUriTwo, playableUriTwo),
//            api.playlists.getPlaylistTracks(createdPlaylist!!.id).items.map { it.item?.uri }
//        )
//
//        assertFailsWith<SpotifyException.BadRequestException> {
//            api.playlists.removePlayablesFromClientPlaylist(
//                createdPlaylist!!.id,
//                Pair(playableUriOne, SpotifyPlayablePositions(3))
//            )
//        }

        tearDown()
    }
}
