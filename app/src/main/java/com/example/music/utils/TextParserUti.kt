package com.example.music.utils

import androidx.annotation.NonNull
import androidx.core.text.HtmlCompat

object TextParserUtil {

    /**
     * @param htmlText HTML text to be parsed
     * @return Parsed text
     */
    @NonNull
    fun parseHtmlText(htmlText: String): String {
        return HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    }
}
