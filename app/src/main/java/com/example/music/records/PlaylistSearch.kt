package com.example.music.records

import com.google.gson.annotations.SerializedName
import com.example.music.utils.TextParserUtil

data class PlaylistSearch(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Data
) {
    data class Data(
        @SerializedName("id") val id: String,
        @SerializedName("name") private val rawName: String,
        @SerializedName("url") val url: String,
        @SerializedName("description") private val rawDescription: String?,
        @SerializedName("type") val type: String,
        @SerializedName("year") val year: Int,
        @SerializedName("playCount") val playCount: Int,
        @SerializedName("songCount") val songCount: Int,
        @SerializedName("language") val language: String,
        @SerializedName("explicitContent") val explicitContent: Boolean,
        @SerializedName("artists") val artists: List<Artist>,
        @SerializedName("image") val image: List<GlobalSearch.Image>,
        @SerializedName("songs") val songs: List<SongResponse.Song>
    ) {
        val name: String
            get() = TextParserUtil.parseHtmlText(rawName)

        val description: String?
            get() = rawDescription?.let { TextParserUtil.parseHtmlText(it) }

        data class Artist(
            @SerializedName("id") val id: String,
            @SerializedName("name") private val rawName: String,
            @SerializedName("url") val url: String,
            @SerializedName("role") val role: String,
            @SerializedName("type") val type: String,
            @SerializedName("image") val image: List<GlobalSearch.Image>
        ) {
            val name: String
                get() = TextParserUtil.parseHtmlText(rawName)
        }
    }
}

