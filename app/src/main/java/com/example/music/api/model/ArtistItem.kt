package com.example.music.api.model

import com.example.music.utils.TextParserUtil

data class ArtistItem(
    private val rawName: String,
    val image: String,
    val id: String
) {
    val name: String
        get() = TextParserUtil.parseHtmlText(rawName)
}
