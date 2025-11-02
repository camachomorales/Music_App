package com.example.music.api.model

import com.example.music.utils.TextParserUtil

data class SearchListItem(
    val id: String,
    private val rawTitle: String,
    private val rawSubtitle: String,
    val coverImage: String,
    val type: Type
) {

    val title: String
        get() = TextParserUtil.parseHtmlText(rawTitle)

    val subtitle: String
        get() = TextParserUtil.parseHtmlText(rawSubtitle)

    enum class Type {
        SONG,
        ALBUM,
        PLAYLIST,
        ARTIST
    }
}
