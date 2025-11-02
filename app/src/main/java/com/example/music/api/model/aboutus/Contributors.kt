package com.example.music.api.model.aboutus

import com.google.gson.annotations.SerializedName

data class Contributors(
    @SerializedName("contributors")
    val contributors: List<Contributor>
) {
    data class Contributor(
        @SerializedName("login")
        val login: String,
        @SerializedName("avatar_url")
        val avatarUrl: String,
        @SerializedName("html_url")
        val htmlUrl: String
    )
}
