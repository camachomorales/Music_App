package com.example.music.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.SeekBar
import com.example.music.ApplicationClass
import com.example.music.activities.MusicOverviewActivity

class MusicService : Service() {

    private val binder = MyBinder()
    private var actionPlaying: ActionPlaying? = null

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class MyBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.extras == null) return START_STICKY

        val actionName = intent.getStringExtra("action") ?: ""

        when (actionName) {
            ApplicationClass.ACTION_NEXT -> {
                // Handle next action
                actionPlaying?.nextClicked()
            }

            ApplicationClass.ACTION_PREV -> {
                // Handle previous action
                actionPlaying?.prevClicked()
            }

            ApplicationClass.ACTION_PLAY -> {
                // Handle play/pause action
                actionPlaying?.playClicked()
            }

            "action_click" -> {
                val musicId = intent.getStringExtra("id")
                val activityIntent = Intent(this, MusicOverviewActivity::class.java).apply {
                    putExtra("id", musicId)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(activityIntent)
            }
        }

        return START_STICKY
    }

    fun setCallback(callback: ActionPlaying) {
        this.actionPlaying = callback
    }

    interface ActionPlaying {
        fun nextClicked()
        fun prevClicked()
        fun playClicked()
        fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
    }
}