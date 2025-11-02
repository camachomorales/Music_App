package com.example.music.records

import com.google.gson.annotations.SerializedName
import com.example.music.utils.TextParserUtil

data class ArtistsSearch(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Data
) {
    data class Data(
        @SerializedName("total") val total: Int,
        @SerializedName("start") val start: Int,
        @SerializedName("results") val results: List<Results>
    ) {
        data class Results(
            @SerializedName("id") val id: String,
            @SerializedName("name") private val rawName: String,
            @SerializedName("role") val role: String,
            @SerializedName("type") val type: String,
            @SerializedName("url") val url: String,
            @SerializedName("image") val image: List<GlobalSearch.Image>
        ) {
            val name: String
                get() = TextParserUtil.parseHtmlText(rawName)
        }
    }
}
