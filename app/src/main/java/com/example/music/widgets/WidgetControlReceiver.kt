package com.example.music.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.music.ApplicationClass

class WidgetControlReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return

        val applicationClass = ApplicationClass()

        when (action) {
            "ACTION_TOGGLE_PLAY" -> {
                Log.d("MelotuneWidget", "Play/Pause pressed")
                applicationClass.togglePlayPause()
            }
            "ACTION_NEXT" -> {
                Log.d("MelotuneWidget", "Next pressed")
                applicationClass.nextTrack()
            }
            "ACTION_PREV" -> {
                Log.d("MelotuneWidget", "Previous pressed")
                applicationClass.prevTrack()
            }
        }
    }
}
