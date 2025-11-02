package com.example.music.api.network

import com.example.music.api.model.*
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api/search/all")
    suspend fun searchAll(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): SearchAllResponse

    @GET("api/search/songs")
    suspend fun searchSongs(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): SongSearchResponse

    @GET("api/search/albums")
    suspend fun searchAlbums(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): AlbumSearchResponse

    @GET("api/search/artists")
    suspend fun searchArtists(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ArtistSearchResponse

    @GET("api/search/playlists")
    suspend fun searchPlaylists(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): PlaylistSearchResponse

    @GET("api/songs")
    suspend fun getSongDetails(
        @Query("id") songId: String
    ): SongDetailsResponse

    @GET("api/albums")
    suspend fun getAlbumDetails(
        @Query("id") albumId: String
    ): AlbumDetailsResponse

    @GET("api/artists")
    suspend fun getArtistDetails(
        @Query("id") artistId: String
    ): ArtistDetailsResponse

    @GET("api/playlists")
    suspend fun getPlaylistDetails(
        @Query("id") playlistId: String
    ): PlaylistDetailsResponse

    @GET("api/search/suggestions")
    suspend fun getSearchSuggestions(
        @Query("query") query: String
    ): SuggestionsResponse

    @GET("api/artist/albums")
    suspend fun getArtistAlbums(
        @Query("artistName") artistName: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): AlbumsResult

    @GET("api/albums/tracks")  // Cambia esta ruta al endpoint real
    suspend fun getAlbumTracks(
        @Query("albumId") albumId: String
    ): SongsResult

}
