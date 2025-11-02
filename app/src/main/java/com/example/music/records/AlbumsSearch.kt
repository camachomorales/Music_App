package com.example.music.records

import com.google.gson.annotations.SerializedName
import com.example.music.utils.TextParserUtil

data class AlbumsSearch(
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
            @SerializedName("description") private val rawDescription: String?,
            @SerializedName("url") val url: String,
            @SerializedName("year") val year: Int,
            @SerializedName("type") val type: String,
            @SerializedName("playCount") val playCount: Int,
            @SerializedName("language") val language: String,
            @SerializedName("explicitContent") val explicitContent: Boolean,
            @SerializedName("artists") val artist: Artists,
            @SerializedName("image") val image: List<GlobalSearch.Image>
        ) {

            val name: String
                get() = TextParserUtil.parseHtmlText(rawName)

            val description: String?
                get() = rawDescription?.let { TextParserUtil.parseHtmlText(it) }

            data class Artists(
                @SerializedName("primary") val primary: List<Artist>,
                @SerializedName("featured") val featured: List<Artist>,
                @SerializedName("all") val all: List<Artist>
            ) {
                data class Artist(
                    @SerializedName("id") val id: String,
                    @SerializedName("name") private val rawName: String,
                    @SerializedName("url") val url: String,
                    @SerializedName("role") val role: String,
                    @SerializedName("image") val image: List<GlobalSearch.Image>,
                    @SerializedName("type") val type: String
                ) {
                    val name: String
                        get() = TextParserUtil.parseHtmlText(rawName)
                }
            }
        }
    }
}


