package com.example.music.records

import com.google.gson.annotations.SerializedName
import com.example.music.utils.TextParserUtil

data class GlobalSearch(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Data
) {
    data class Data(
        @SerializedName("topQuery") val topQuery: TopQuery,
        @SerializedName("songs") val songs: Songs,
        @SerializedName("albums") val albums: Albums,
        @SerializedName("artists") val artists: Artists,
        @SerializedName("playlists") val playlists: Playlists
    ) {
        data class TopQuery(
            @SerializedName("results") val results: List<Results>,
            @SerializedName("position") val position: Int
        ) {
            data class Results(
                @SerializedName("id") val id: String,
                @SerializedName("title") private val rawTitle: String,
                @SerializedName("image") val image: List<Image>,
                @SerializedName("url") val url: String,
                @SerializedName("type") val type: String,
                @SerializedName("description") private val rawDescription: String
            ) {
                val title: String
                    get() = TextParserUtil.parseHtmlText(rawTitle)

                val description: String
                    get() = TextParserUtil.parseHtmlText(rawDescription)
            }
        }

        data class Songs(
            @SerializedName("results") val results: List<Results>,
            @SerializedName("position") val position: Int
        ) {
            data class Results(
                @SerializedName("id") val id: String,
                @SerializedName("title") private val rawTitle: String,
                @SerializedName("image") val image: List<Image>,
                @SerializedName("album") private val rawAlbum: String,
                @SerializedName("url") val url: String,
                @SerializedName("type") val type: String,
                @SerializedName("description") private val rawDescription: String,
                @SerializedName("primaryArtists") private val rawPrimaryArtists: String,
                @SerializedName("singers") private val rawSingers: String,
                @SerializedName("language") private val rawLanguage: String
            ) {
                val title: String
                    get() = TextParserUtil.parseHtmlText(rawTitle)

                val description: String
                    get() = TextParserUtil.parseHtmlText(rawDescription)

                val album: String
                    get() = TextParserUtil.parseHtmlText(rawAlbum)

                val primaryArtists: String
                    get() = TextParserUtil.parseHtmlText(rawPrimaryArtists)

                val singers: String
                    get() = TextParserUtil.parseHtmlText(rawSingers)

                val language: String
                    get() = TextParserUtil.parseHtmlText(rawLanguage)
            }
        }

        data class Albums(
            @SerializedName("results") val results: List<Results>,
            @SerializedName("position") val position: Int
        ) {
            data class Results(
                @SerializedName("id") val id: String,
                @SerializedName("title") private val rawTitle: String,
                @SerializedName("image") val image: List<Image>,
                @SerializedName("artist") private val rawArtist: String,
                @SerializedName("url") val url: String,
                @SerializedName("type") val type: String,
                @SerializedName("description") private val rawDescription: String,
                @SerializedName("year") private val rawYear: String,
                @SerializedName("songIds") val songIds: String,
                @SerializedName("language") private val rawLanguage: String
            ) {
                val title: String
                    get() = TextParserUtil.parseHtmlText(rawTitle)

                val description: String
                    get() = TextParserUtil.parseHtmlText(rawDescription)

                val artist: String
                    get() = TextParserUtil.parseHtmlText(rawArtist)

                val year: String
                    get() = TextParserUtil.parseHtmlText(rawYear)

                val language: String
                    get() = TextParserUtil.parseHtmlText(rawLanguage)
            }
        }

        data class Artists(
            @SerializedName("results") val results: List<Results>,
            @SerializedName("position") val position: Int
        ) {
            data class Results(
                @SerializedName("id") val id: String,
                @SerializedName("title") private val rawTitle: String,
                @SerializedName("image") val image: List<Image>,
                @SerializedName("type") val type: String,
                @SerializedName("description") private val rawDescription: String,
                @SerializedName("position") val position: Int
            ) {
                val title: String
                    get() = TextParserUtil.parseHtmlText(rawTitle)

                val description: String
                    get() = TextParserUtil.parseHtmlText(rawDescription)
            }
        }

        data class Playlists(
            @SerializedName("results") val results: List<Results>,
            @SerializedName("position") val position: Int
        ) {
            data class Results(
                @SerializedName("id") val id: String,
                @SerializedName("title") private val rawTitle: String,
                @SerializedName("image") val image: List<Image>,
                @SerializedName("url") val url: String,
                @SerializedName("type") val type: String,
                @SerializedName("language") private val rawLanguage: String,
                @SerializedName("description") private val rawDescription: String
            ) {
                val title: String
                    get() = TextParserUtil.parseHtmlText(rawTitle)

                val description: String
                    get() = TextParserUtil.parseHtmlText(rawDescription)

                val language: String
                    get() = TextParserUtil.parseHtmlText(rawLanguage)
            }
        }
    }

    data class Image (
        @SerializedName("quality") val quality: String,
        @SerializedName("url") val url: String
    )
}
