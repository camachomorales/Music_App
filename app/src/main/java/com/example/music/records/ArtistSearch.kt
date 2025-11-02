package com.example.music.records

import com.google.gson.annotations.SerializedName
import com.example.music.utils.TextParserUtil

data class ArtistSearch(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Data
) {
    data class Data(
        @SerializedName("id") val id: String,
        @SerializedName("name") private val rawName: String,
        @SerializedName("url") val url: String,
        @SerializedName("type") val type: String,
        @SerializedName("followerCount") val followerCount: Int,
        @SerializedName("fanCount") val fanCount: Int,
        @SerializedName("isVerified") val isVerified: Boolean,
        @SerializedName("dominantLanguage") val dominantLanguage: String,
        @SerializedName("dominantType") val dominantType: String,
        @SerializedName("bio") val bio: List<Bio>,
        @SerializedName("dob") val dob: String,
        @SerializedName("fb") val fb: String,
        @SerializedName("twitter") val twitter: String,
        @SerializedName("wiki") val wiki: String,
        @SerializedName("availableLanguages") val availableLanguages: List<String>,
        @SerializedName("isRadioPresent") val isRadioPresent: Boolean,
        @SerializedName("image") val image: List<GlobalSearch.Image>,
        @SerializedName("topSongs") val topSongs: List<SongResponse.Song>,
        @SerializedName("topAlbums") val topAlbums: List<AlbumsSearch.Data.Results>,
        @SerializedName("singles") val singles: List<AlbumsSearch.Data.Results>,
        @SerializedName("similarArtists") val similarArtists: List<SimilarArtist>
    ) {
        val name: String
            get() = TextParserUtil.parseHtmlText(rawName)

        data class Bio(
            @SerializedName("text") private val rawText: String,
            @SerializedName("title") private val rawTitle: String,
            @SerializedName("sequence") val sequence: Int
        ) {
            val text: String
                get() = TextParserUtil.parseHtmlText(rawText)

            val title: String
                get() = TextParserUtil.parseHtmlText(rawTitle)
        }

        data class SimilarArtist(
            @SerializedName("id") val id: String,
            @SerializedName("name") private val rawName: String
        ) {
            val name: String
                get() = TextParserUtil.parseHtmlText(rawName)
        }
    }
}

