package com.example.music.services

interface ActionPlaying {
    fun nextClicked()
    fun prevClicked()
    fun playClicked()
    fun onProgressChanged(progress: Int)
}
