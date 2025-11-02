package com.example.music.records.sharedpref

import com.google.gson.annotations.SerializedName

data class SavedLibraries(
    @SerializedName("lists") val lists: List<Library>
) {
    data class Library(
        @SerializedName("id") val id: String,
        @SerializedName("isCreatedByUser") val isCreatedByUser: Boolean,
        @SerializedName("isAlbum") val isAlbum: Boolean,
        @SerializedName("name") val name: String,
        @SerializedName("image") val image: String,
        @SerializedName("description") val description: String,
        @SerializedName("songs") val songs: List<Song>
    ) {
        data class Song(
            @SerializedName("id") val id: String,
            @SerializedName("title") val title: String,
            @SerializedName("description") val description: String,
            @SerializedName("image") val image: String
        )
    }
}
