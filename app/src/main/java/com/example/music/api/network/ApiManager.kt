package com.example.music.network

import android.app.Activity
import android.net.Uri
import com.example.music.network.utility.RequestNetwork
import com.example.music.network.utility.RequestNetworkController

class ApiManager(activity: Activity) {

    private val requestNetwork = RequestNetwork(activity)

    companion object {
        private const val BASE_URL = "https://meloapi.vercel.app/api/"
        private const val SEARCH_URL = "${BASE_URL}search"
        private const val SONGS = "/songs"
        private const val ALBUMS = "/albums"
        private const val ARTISTS = "/artists"
        private const val PLAYLISTS = "/playlists"
        private const val SONGS_URL = "${BASE_URL}songs"
        private const val ALBUMS_URL = "${BASE_URL}albums"
        private const val ARTISTS_URL = "${BASE_URL}artists"
        private const val PLAYLISTS_URL = "${BASE_URL}playlists"
    }

    fun globalSearch(text: String, listener: RequestNetwork.RequestListener) {
        val queryMap = hashMapOf<String, Any>(
            "query" to Uri.encode(text)
        )
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, SEARCH_URL, "", listener)
    }

    fun searchSongs(
        query: String,
        page: Int? = null,
        limit: Int? = null,
        listener: RequestNetwork.RequestListener
    ) {
        val queryMap = hashMapOf<String, Any>("query" to Uri.encode(query))
        page?.let { queryMap["page"] = it }
        limit?.let { queryMap["limit"] = it }
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, SEARCH_URL + SONGS, "", listener)
    }

    fun searchAlbums(
        query: String,
        page: Int? = null,
        limit: Int? = null,
        listener: RequestNetwork.RequestListener
    ) {
        val queryMap = hashMapOf<String, Any>("query" to Uri.encode(query))
        page?.let { queryMap["page"] = it }
        limit?.let { queryMap["limit"] = it }
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, SEARCH_URL + ALBUMS, "", listener)
    }

    fun searchArtists(
        query: String,
        page: Int? = null,
        limit: Int? = null,
        listener: RequestNetwork.RequestListener
    ) {
        val queryMap = hashMapOf<String, Any>("query" to Uri.encode(query))
        page?.let { queryMap["page"] = it }
        limit?.let { queryMap["limit"] = it }
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, SEARCH_URL + ARTISTS, "", listener)
    }

    fun searchPlaylists(
        query: String,
        page: Int? = null,
        limit: Int? = null,
        listener: RequestNetwork.RequestListener
    ) {
        val queryMap = hashMapOf<String, Any>("query" to Uri.encode(query))
        page?.let { queryMap["page"] = it }
        limit?.let { queryMap["limit"] = it }
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, SEARCH_URL + PLAYLISTS, "", listener)
    }

    fun retrieveSongsByIds(ids: String, listener: RequestNetwork.RequestListener) {
        val queryMap = hashMapOf<String, Any>("ids" to ids)
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, SONGS_URL, "", listener)
    }

    fun retrieveSongByLink(link: String, listener: RequestNetwork.RequestListener) {
        val queryMap = hashMapOf<String, Any>("link" to link)
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, SONGS_URL, "", listener)
    }

    fun retrieveSongById(id: String, lyrics: Boolean? = null, listener: RequestNetwork.RequestListener) {
        val queryMap = hashMapOf<String, Any>()
        lyrics?.let { queryMap["lyrics"] = it }
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, "$SONGS_URL/$id", "", listener)
    }

    fun retrieveLyricsById(id: String, listener: RequestNetwork.RequestListener) {
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, "$SONGS_URL/$id/lyrics", "", listener)
    }

    fun retrieveSongSuggestions(id: String, limit: Int? = null, listener: RequestNetwork.RequestListener) {
        val queryMap = hashMapOf<String, Any>()
        limit?.let { queryMap["limit"] = it }
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, "$SONGS_URL/$id/suggestions", "", listener)
    }

    fun retrieveAlbumById(id: String, listener: RequestNetwork.RequestListener) {
        val queryMap = hashMapOf<String, Any>("id" to id)
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, ALBUMS_URL, "", listener)
    }

    fun retrieveAlbumByLink(link: String, listener: RequestNetwork.RequestListener) {
        val queryMap = hashMapOf<String, Any>("link" to link)
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, ALBUMS_URL, "", listener)
    }

    fun retrieveArtistsById(
        id: String,
        page: Int? = null,
        songCount: Int? = null,
        albumCount: Int? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        listener: RequestNetwork.RequestListener
    ) {
        val queryMap = hashMapOf<String, Any>("id" to id)
        page?.let { queryMap["page"] = it }
        songCount?.let { queryMap["songCount"] = it }
        albumCount?.let { queryMap["albumCount"] = it }
        sortBy?.let { queryMap["sortBy"] = it }
        sortOrder?.let { queryMap["sortOrder"] = it }
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, ARTISTS_URL, "", listener)
    }

    fun retrieveArtistsByLink(
        link: String,
        page: Int? = null,
        songCount: Int? = null,
        albumCount: Int? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        listener: RequestNetwork.RequestListener
    ) {
        val queryMap = hashMapOf<String, Any>("link" to link)
        page?.let { queryMap["page"] = it }
        songCount?.let { queryMap["songCount"] = it }
        albumCount?.let { queryMap["albumCount"] = it }
        sortBy?.let { queryMap["sortBy"] = it }
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, ARTISTS_URL, "", listener)
    }

    /**
     * @param sortBy (popularity | latest | alphabetical)
     * @param sortOrder (asc | desc)
     */
    fun retrieveArtistById(
        id: String,
        page: Int? = null,
        songCount: Int? = null,
        albumCount: Int? = null,
        sortBy: SortBy? = null,
        sortOrder: SortOrder? = null,
        listener: RequestNetwork.RequestListener
    ) {
        val queryMap = hashMapOf<String, Any>()
        page?.let { queryMap["page"] = it }
        songCount?.let { queryMap["songCount"] = it }
        albumCount?.let { queryMap["albumCount"] = it }
        sortBy?.let { queryMap["sortBy"] = it.name }
        sortOrder?.let { queryMap["sortOrder"] = it.name }
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, "$ARTISTS_URL/$id", "", listener)
    }

    fun retrieveArtistSongs(
        id: String,
        page: Int? = null,
        sortBy: SortBy? = null,
        sortOrder: SortOrder? = null,
        listener: RequestNetwork.RequestListener
    ) {
        val queryMap = hashMapOf<String, Any>()
        page?.let { queryMap["page"] = it }
        sortBy?.let { queryMap["sortBy"] = it.name }
        sortOrder?.let { queryMap["sortOrder"] = it.name }
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, "$ARTISTS_URL/$id/songs", "", listener)
    }

    fun retrieveArtistAlbums(
        id: String,
        page: Int? = null,
        sortBy: SortBy? = null,
        sortOrder: SortOrder? = null,
        listener: RequestNetwork.RequestListener
    ) {
        val queryMap = hashMapOf<String, Any>()
        page?.let { queryMap["page"] = it }
        sortBy?.let { queryMap["sortBy"] = it.name }
        sortOrder?.let { queryMap["sortOrder"] = it.name }
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, "$ARTISTS_URL/$id/albums", "", listener)
    }

    fun retrievePlaylistById(
        id: String,
        page: Int? = null,
        limit: Int? = null,
        listener: RequestNetwork.RequestListener
    ) {
        val queryMap = hashMapOf<String, Any>("id" to id)
        page?.let { queryMap["page"] = it }
        limit?.let { queryMap["limit"] = it }
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, PLAYLISTS_URL, "", listener)
    }

    fun retrievePlaylistByLink(
        link: String,
        page: Int? = null,
        limit: Int? = null,
        listener: RequestNetwork.RequestListener
    ) {
        val queryMap = hashMapOf<String, Any>("link" to link)
        page?.let { queryMap["page"] = it }
        limit?.let { queryMap["limit"] = it }
        requestNetwork.setParams(queryMap, RequestNetworkController.REQUEST_PARAM)
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, PLAYLISTS_URL, "", listener)
    }

    enum class SortBy {
        popularity,
        latest,
        alphabetical
    }

    enum class SortOrder {
        asc,
        desc
    }
}
