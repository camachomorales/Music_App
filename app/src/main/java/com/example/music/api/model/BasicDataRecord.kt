package com.example.music.api.model

import com.example.music.utils.TextParserUtil

data class BasicDataRecord(
    private val rawId: String,
    private val rawTitle: String,
    private val rawSubtitle: String,
    val image: String
) {
    val id: String
        get() = rawId

    val title: String
        get() = TextParserUtil.parseHtmlText(rawTitle)

    val subtitle: String
        get() = TextParserUtil.parseHtmlText(rawSubtitle)
}
