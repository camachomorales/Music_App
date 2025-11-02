package com.example.music.records

import com.google.gson.annotations.SerializedName

data class ArtistAllSongs(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Data
) {
    data class Data(
        @SerializedName("total") val total: Int,
        @SerializedName("songs") val songs: List<SongResponse.Song>
    )
}
