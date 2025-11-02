package com.example.music.api.model

import com.example.music.utils.TextParserUtil

data class AlbumItem(
    private val rawAlbumTitle: String,
    private val rawAlbumSubTitle: String,
    val albumCover: String,
    val id: String
) {
    val albumTitle: String
        get() = TextParserUtil.parseHtmlText(rawAlbumTitle)

    val albumSubTitle: String
        get() = TextParserUtil.parseHtmlText(rawAlbumSubTitle)
}

