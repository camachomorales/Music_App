package com.example.music.utils

import android.media.MediaPlayer

class MediaPlayerUtil private constructor() : MediaPlayer() {

    companion object {
        @Volatile
        private var instance: MediaPlayerUtil? = null

        fun getInstance(): MediaPlayerUtil =
            instance ?: synchronized(this) {
                instance ?: MediaPlayerUtil().also { instance = it }
            }
    }
}
