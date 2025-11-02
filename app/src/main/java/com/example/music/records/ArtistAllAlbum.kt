package com.example.music.records

import com.google.gson.annotations.SerializedName

data class ArtistAllAlbum(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Data
) {
    data class Data(
        @SerializedName("total") val total: Int,
        @SerializedName("albums") val albums: List<AlbumsSearch.Data.Results>
    )
}
