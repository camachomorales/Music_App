package com.example.music.api.model

import com.google.gson.annotations.SerializedName

// ===== RESPUESTAS DE BÚSQUEDA =====

data class SearchAllResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: SearchAllData
)

data class SearchAllData(
    @SerializedName("topQuery") val topQuery: TopQuery?,
    @SerializedName("songs") val songs: SongsResult?,
    @SerializedName("albums") val albums: AlbumsResult?,
    @SerializedName("artists") val artists: ArtistsResult?,
    @SerializedName("playlists") val playlists: PlaylistsResult?
)

data class TopQuery(
    @SerializedName("results") val results: List<SearchResult>
)

data class SearchResult(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("image") val image: List<ImageQuality>,
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String?
)

// ===== CANCIONES =====

data class SongSearchResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: SongsResult
)

data class SongsResult(
    @SerializedName("total") val total: Int,
    @SerializedName("start") val start: Int,
    @SerializedName("results") val results: List<Song>
)

data class Song(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("year") val year: String? = null,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    @SerializedName("duration") val duration: Int? = null,
    @SerializedName("label") val label: String? = null,
    @SerializedName("explicitContent") val explicitContent: Boolean? = false,
    @SerializedName("playCount") val playCount: Int? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("hasLyrics") val hasLyrics: Boolean? = false,
    @SerializedName("lyricsId") val lyricsId: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("copyright") val copyright: String? = null,
    @SerializedName("album") val album: AlbumInfo? = null,
    @SerializedName("artists") val artists: ArtistsInfo? = null,
    @SerializedName("image") val image: List<ImageQuality>? = null,
    @SerializedName("downloadUrl") val downloadUrl: List<DownloadQuality>? = null
)

data class SongDetailsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Song>
)

// ===== ÁLBUMES =====

data class AlbumSearchResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: AlbumsResult
)

data class AlbumsResult(
    @SerializedName("total") val total: Int,
    @SerializedName("start") val start: Int,
    @SerializedName("results") val results: List<Album>
)

data class Album(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("year") val year: String? = null,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    @SerializedName("playCount") val playCount: Int? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("explicitContent") val explicitContent: Boolean? = false,
    @SerializedName("songCount") val songCount: Int? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("artists") val artists: ArtistsInfo? = null,
    @SerializedName("image") val image: List<ImageQuality>? = null,
    @SerializedName("songs") val songs: List<Song>? = null
)

data class AlbumDetailsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Album
)

data class AlbumInfo(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?
)

// ===== ARTISTAS =====

data class ArtistSearchResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ArtistsResult
)

data class ArtistsResult(
    @SerializedName("total") val total: Int,
    @SerializedName("start") val start: Int,
    @SerializedName("results") val results: List<Artist>
)

data class Artist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("image") val image: List<ImageQuality>? = null,
    @SerializedName("followerCount") val followerCount: Int? = null,
    @SerializedName("fanCount") val fanCount: String? = null,
    @SerializedName("isVerified") val isVerified: Boolean? = false,
    @SerializedName("dominantLanguage") val dominantLanguage: String? = null,
    @SerializedName("dominantType") val dominantType: String? = null,
    @SerializedName("topSongs") val topSongs: List<Song>? = null,
    @SerializedName("topAlbums") val topAlbums: List<Album>? = null
)

data class ArtistDetailsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Artist
)

data class ArtistsInfo(
    @SerializedName("primary") val primary: List<ArtistBasic>? = null,
    @SerializedName("featured") val featured: List<ArtistBasic>? = null,
    @SerializedName("all") val all: List<ArtistBasic>? = null
)

data class ArtistBasic(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("image") val image: List<ImageQuality>?,
    @SerializedName("url") val url: String?
)

// ===== PLAYLISTS =====

data class PlaylistSearchResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: PlaylistsResult
)

data class PlaylistsResult(
    @SerializedName("total") val total: Int,
    @SerializedName("start") val start: Int,
    @SerializedName("results") val results: List<Playlist>
)

data class Playlist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("year") val year: String? = null,
    @SerializedName("playCount") val playCount: Int? = null,
    @SerializedName("explicitContent") val explicitContent: Boolean? = false,
    @SerializedName("songCount") val songCount: Int? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("image") val image: List<ImageQuality>? = null,
    @SerializedName("songs") val songs: List<Song>? = null
)

data class PlaylistDetailsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Playlist
)

// ===== MODELOS COMUNES =====

data class ImageQuality(
    @SerializedName("quality") val quality: String,
    @SerializedName("url") val url: String
)

data class DownloadQuality(
    @SerializedName("quality") val quality: String,
    @SerializedName("url") val url: String
)

// Sugerencias
data class SuggestionsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<String>
)

// ===== FUNCIONES DE AYUDA =====

fun List<ImageQuality>.getBestImageQuality(): String {
    return this.firstOrNull { it.quality == "500x500" }?.url
        ?: this.lastOrNull()?.url
        ?: ""
}

fun List<DownloadQuality>.getBestDownloadQuality(): String {
    return this.firstOrNull { it.quality == "320kbps" }?.url
        ?: this.firstOrNull { it.quality == "160kbps" }?.url
        ?: this.firstOrNull { it.quality == "96kbps" }?.url
        ?: this.lastOrNull()?.url
        ?: ""
}

fun Song.getArtistName(): String {
    return artists?.primary?.firstOrNull()?.name ?: ""
}

fun Album.getArtistName(): String {
    return artists?.primary?.firstOrNull()?.name ?: ""
}

fun Int.formatDuration(): String {
    val minutes = this / 60
    val seconds = this % 60
    return String.format("%d:%02d", minutes, seconds)
}
