package com.example.music.records

import com.google.gson.annotations.SerializedName

data class SongSearch(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Data
) {
    data class Data(
        @SerializedName("total") val total: Int,
        @SerializedName("start") val start: Int,
        @SerializedName("results") val results: List<SongResponse.Song>
    )
}
