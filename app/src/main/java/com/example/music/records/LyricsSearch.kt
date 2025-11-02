package com.example.music.records

import com.google.gson.annotations.SerializedName
import com.example.music.utils.TextParserUtil

data class LyricsSearch(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Data
) {
    data class Data(
        @SerializedName("lyrics") private val rawLyrics: String,
        @SerializedName("copyright") val copyright: String,
        @SerializedName("snippet") val snippet: String
    ) {
        val lyrics: String
            get() = TextParserUtil.parseHtmlText(rawLyrics)
    }
}
