package com.example.music.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.music.ApplicationClass
import com.example.music.R
import com.example.music.activities.MusicOverviewActivity

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action == null) {
            Log.e(TAG, "Received null context or intent action")
            return
        }

        val action = intent.action ?: return
        Log.i(TAG, "Received action: $action")

        // Create intent for MusicService
        val serviceIntent = Intent(context, MusicService::class.java)

        // Get ApplicationClass instance
        val applicationClass = context.applicationContext as? ApplicationClass
        if (applicationClass == null) {
            Log.e(TAG, "ApplicationClass is null")
            return
        }

        when (action) {
            ApplicationClass.ACTION_NEXT -> {
                Log.i(TAG, "Processing NEXT action")
                try {
                    // Call nextTrack through ApplicationClass for immediate effect
                    applicationClass.nextTrack()

                    // Then notify the service for UI updates
                    serviceIntent.putExtra("action", action)
                    context.startService(serviceIntent)

                } catch (e: Exception) {
                    Log.e(TAG, "Error processing next action: ${e.message}", e)
                    Toast.makeText(context, "Error playing next track", Toast.LENGTH_SHORT).show()
                }
            }

            ApplicationClass.ACTION_PREV -> {
                Log.i(TAG, "Processing PREVIOUS action")
                try {
                    // If we're already at the beginning of the track, go to previous track
                    // Otherwise just restart the current track (standard music player behavior)
                    val player = ApplicationClass.player
                    if (player.currentPosition > 3000) {
                        player.seekTo(0)
                        player.play()
                    } else {
                        // Call prevTrack for track change
                        applicationClass.prevTrack()
                    }

                    // Then notify service for UI updates
                    serviceIntent.putExtra("action", action)
                    context.startService(serviceIntent)

                } catch (e: Exception) {
                    Log.e(TAG, "Error processing previous action: ${e.message}", e)
                    Toast.makeText(context, "Error playing previous track", Toast.LENGTH_SHORT).show()
                }
            }

            ApplicationClass.ACTION_PLAY -> {
                Log.i(TAG, "Processing PLAY/PAUSE action")
                try {
                    // Toggle playback
                    applicationClass.togglePlayPause()

                    // Notify service
                    serviceIntent.putExtra("action", action)
                    serviceIntent.putExtra("fromNotification", true)
                    context.startService(serviceIntent)

                    // Update notification with current state
                    val isPlaying = ApplicationClass.player.isPlaying
                    applicationClass.showNotification(
                        if (isPlaying) R.drawable.baseline_pause_24 else R.drawable.play_arrow_24px
                    )

                    // Show visual feedback
                    Toast.makeText(
                        context,
                        if (isPlaying) "Playing" else "Paused",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error toggling playback", e)
                }
            }

            "action_click" -> {
                Log.i(TAG, "Processing CLICK action")
                try {
                    // Launch activity for the current track
                    val activityIntent = Intent(context, MusicOverviewActivity::class.java).apply {
                        putExtra("id", ApplicationClass.MUSIC_ID)
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(activityIntent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error launching activity", e)
                }
            }

            else -> {
                Log.i(TAG, "Unknown action received: $action")
            }
        }
    }
}