package com.example.music.records

import com.google.gson.annotations.SerializedName
import com.example.music.utils.TextParserUtil

data class AlbumSearch(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Data
) {
    data class Data(
        @SerializedName("id") val id: String,
        @SerializedName("name") private val rawName: String,
        @SerializedName("url") val url: String,
        @SerializedName("description") private val rawDescription: String?,
        @SerializedName("year") val year: Int,
        @SerializedName("playCount") val playCount: Int,
        @SerializedName("language") val language: String,
        @SerializedName("explicitContent") val explicitContent: Boolean,
        @SerializedName("artists") val artist: SongResponse.Artists,
        @SerializedName("image") val image: List<Image>,
        @SerializedName("songs") val songs: List<SongResponse.Song>
    ) {
        val name: String
            get() = TextParserUtil.parseHtmlText(rawName)

        val description: String?
            get() = rawDescription?.let { TextParserUtil.parseHtmlText(it) }
    }
}

// Define local Image class to avoid unresolved 'GlobalSearch.Image'
data class Image(
    val url: String,
    val height: Int? = null,
    val width: Int? = null
)
