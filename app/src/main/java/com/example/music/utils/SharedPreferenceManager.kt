package com.example.music.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.music.records.*
import com.example.music.records.sharedpref.SavedLibraries
import com.google.gson.Gson

object SharedPreferenceManager {

    private const val PREFS_NAME = "cache"
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun clearCache() {
        sharedPreferences.edit { clear() }
    }

    fun setHomeSongsRecommended(songSearch: SongSearch) {
        val json = gson.toJson(songSearch)
        sharedPreferences.edit { putString("home_songs_recommended", json) }
    }

    fun getHomeSongsRecommended(): SongSearch? {
        val json = sharedPreferences.getString("home_songs_recommended", null)
        return json?.let { gson.fromJson(it, SongSearch::class.java) }
    }

    fun setHomeArtistsRecommended(artistsRecommended: ArtistsSearch) {
        val json = gson.toJson(artistsRecommended)
        sharedPreferences.edit { putString("home_artists_recommended", json) }
    }

    fun getHomeArtistsRecommended(): ArtistsSearch? {
        val json = sharedPreferences.getString("home_artists_recommended", null)
        return json?.let { gson.fromJson(it, ArtistsSearch::class.java) }
    }

    fun setHomeAlbumsRecommended(albumsSearch: AlbumsSearch) {
        val json = gson.toJson(albumsSearch)
        sharedPreferences.edit { putString("home_albums_recommended", json) }
    }

    fun getHomeAlbumsRecommended(): AlbumsSearch? {
        val json = sharedPreferences.getString("home_albums_recommended", null)
        return json?.let { gson.fromJson(it, AlbumsSearch::class.java) }
    }

    fun setHomePlaylistRecommended(playlistsSearch: PlaylistsSearch) {
        val json = gson.toJson(playlistsSearch)
        sharedPreferences.edit { putString("home_playlists_recommended", json) }
    }

    fun getHomePlaylistRecommended(): PlaylistsSearch? {
        val json = sharedPreferences.getString("home_playlists_recommended", null)
        return json?.let { gson.fromJson(it, PlaylistsSearch::class.java) }
    }

    fun setSongResponseById(id: String, songResponse: SongResponse) {
        val json = gson.toJson(songResponse)
        sharedPreferences.edit { putString(id, json) }
    }

    fun getSongResponseById(id: String): SongResponse? {
        val json = sharedPreferences.getString(id, null)
        return json?.let { gson.fromJson(it, SongResponse::class.java) }
    }

    fun isSongResponseById(id: String): Boolean = sharedPreferences.contains(id)

    fun setAlbumResponseById(id: String, albumSearch: AlbumSearch) {
        val json = gson.toJson(albumSearch)
        sharedPreferences.edit { putString(id, json) }
    }

    fun getAlbumResponseById(id: String): AlbumSearch? {
        val json = sharedPreferences.getString(id, null)
        return json?.let { gson.fromJson(it, AlbumSearch::class.java) }
    }

    fun setPlaylistResponseById(id: String, playlistSearch: PlaylistSearch) {
        val json = gson.toJson(playlistSearch)
        sharedPreferences.edit { putString(id, json) }
    }

    fun getPlaylistResponseById(id: String): PlaylistSearch? {
        val json = sharedPreferences.getString(id, null)
        return json?.let { gson.fromJson(it, PlaylistSearch::class.java) }
    }

    fun setTrackQuality(quality: String) {
        sharedPreferences.edit { putString("track_quality", quality) }
    }

    fun getTrackQuality(): String = sharedPreferences.getString("track_quality", "320kbps") ?: "320kbps"

    fun setSavedLibrariesData(savedLibraries: SavedLibraries) {
        val json = gson.toJson(savedLibraries)
        sharedPreferences.edit { putString("saved_libraries", json) }
    }

    fun getSavedLibrariesData(): SavedLibraries? {
        val json = sharedPreferences.getString("saved_libraries", null)
        return json?.let { gson.fromJson(it, SavedLibraries::class.java) }
    }

    fun addLibraryToSavedLibraries(library: SavedLibraries.Library) {
        val savedLibraries = getSavedLibrariesData() ?: SavedLibraries(mutableListOf())
        val updatedLists = savedLibraries.lists.toMutableList()
        updatedLists.add(library)
        setSavedLibrariesData(SavedLibraries(updatedLists))
    }

    fun removeLibraryFromSavedLibraries(index: Int) {
        val savedLibraries = getSavedLibrariesData() ?: return
        if (index in savedLibraries.lists.indices) {
            val updatedLists = savedLibraries.lists.toMutableList()
            updatedLists.removeAt(index)
            setSavedLibrariesData(SavedLibraries(updatedLists))
        }
    }

    fun setSavedLibraryDataById(id: String, library: SavedLibraries.Library) {
        val json = gson.toJson(library)
        sharedPreferences.edit { putString(id, json) }
    }

    fun getSavedLibraryDataById(id: String): SavedLibraries.Library? {
        val json = sharedPreferences.getString(id, null)
        return json?.let { gson.fromJson(it, SavedLibraries.Library::class.java) }
    }

    fun setSearchResultCache(query: String, searchResult: GlobalSearch) {
        val json = gson.toJson(searchResult)
        sharedPreferences.edit { putString("search://$query", json) }
    }

    fun getSearchResult(query: String): GlobalSearch? {
        val key = "search://$query"
        val json = sharedPreferences.getString(key, null)
        return json?.let { gson.fromJson(it, GlobalSearch::class.java) }
    }

    fun setArtistData(artistId: String, artistSearch: ArtistSearch) {
        val json = gson.toJson(artistSearch)
        sharedPreferences.edit { putString("artistData://$artistId", json) }
    }

    fun getArtistData(artistId: String): ArtistSearch? {
        val key = "artistData://$artistId"
        val json = sharedPreferences.getString(key, null)
        return json?.let { gson.fromJson(it, ArtistSearch::class.java) }
    }
}
