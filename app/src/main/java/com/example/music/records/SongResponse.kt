package com.example.music.records

import com.google.gson.annotations.SerializedName
import com.example.music.utils.TextParserUtil

data class SongResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Song>
) {
    data class Song(
        @SerializedName("id") val id: String,
        @SerializedName("name") private val rawName: String,
        @SerializedName("type") val type: String,
        @SerializedName("year") val year: String,
        @SerializedName("releaseDate") val releaseDate: String,
        @SerializedName("duration") val duration: Double,
        @SerializedName("label") val label: String,
        @SerializedName("explicitContent") val explicitContent: Boolean,
        @SerializedName("playCount") val playCount: Int?,
        @SerializedName("language") val language: String,
        @SerializedName("hasLyrics") val hasLyrics: Boolean,
        @SerializedName("lyricsId") val lyricsId: String,
        @SerializedName("lyrics") val lyrics: Lyrics,
        @SerializedName("url") val url: String,
        @SerializedName("copyright") val copyright: String,
        @SerializedName("album") val album: Album,
        @SerializedName("artists") val artists: Artists,
        @SerializedName("image") val image: List<GlobalSearch.Image>,
        @SerializedName("downloadUrl") val downloadUrl: List<DownloadUrl>
    ) {
        val name: String
            get() = TextParserUtil.parseHtmlText(rawName)
    }

    data class Lyrics(
        @SerializedName("lyrics") val lyrics: String,
        @SerializedName("copyright") val copyright: String,
        @SerializedName("snippet") val snippet: String
    ) {
        fun getLyricsText() = TextParserUtil.parseHtmlText(lyrics)
    }

    data class Album(
        @SerializedName("id") val id: String,
        @SerializedName("name") val rawName: String,
        @SerializedName("url") val url: String
    ) {
        val name: String
            get() = TextParserUtil.parseHtmlText(rawName)
    }

    data class Artists(
        @SerializedName("primary") val primary: List<Artist>,
        @SerializedName("featured") val featured: List<Artist>,
        @SerializedName("all") val all: List<Artist>
    )

    data class Artist(
        @SerializedName("id") val id: String,
        @SerializedName("name") val rawName: String,
        @SerializedName("role") private val rawRole: String,  // CAMBIO: private val rawRole
        @SerializedName("type") val type: String,
        @SerializedName("image") val image: List<GlobalSearch.Image>,
        @SerializedName("url") val url: String
    ) {
        val name: String
            get() = TextParserUtil.parseHtmlText(rawName)

        val role: String  // CAMBIO: convertir getRole() en propiedad
            get() = TextParserUtil.parseHtmlText(rawRole)
    }

    data class Image(
        @SerializedName("quality") val quality: String,
        @SerializedName("url") val url: String
    )

    data class DownloadUrl(
        @SerializedName("quality") val quality: String,
        @SerializedName("url") val url: String
    )
}
