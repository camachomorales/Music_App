package com.example.music

import android.app.Activity
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.palette.graphics.Palette
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.example.music.activities.MusicOverviewActivity
import com.example.music.activities.SettingsActivity
import com.example.music.network.ApiManager
import com.example.music.network.utility.RequestNetwork
import com.example.music.records.SongResponse
import com.example.music.services.NotificationReceiver
import com.example.music.utils.SharedPreferenceManager
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.io.File

class ApplicationClass : Application() {

    private val TAG = "ApplicationClass"
    private lateinit var mediaSession: MediaSessionCompat

    companion object {
        lateinit var firebaseAnalytics: FirebaseAnalytics
        const val CHANNEL_ID_1 = "channel_1"
        const val ACTION_NEXT = "next"
        const val ACTION_PREV = "prev"
        const val ACTION_PLAY = "play"
        var CURRENT_TRACK: SongResponse? = null

        var TRACK_QUALITY = "320kbps"
        var isTrackDownloaded = false
        var trackQueue: MutableList<String> = mutableListOf()
        var MUSIC_TITLE = ""
        var MUSIC_DESCRIPTION = ""
        var IMAGE_URL = ""
        var MUSIC_ID = ""
        var SONG_URL = ""
        var track_position = -1
        var IMAGE_BG_COLOR = Color.argb(255, 25, 20, 20)
        var TEXT_ON_IMAGE_COLOR = IMAGE_BG_COLOR xor 0x00FFFFFF
        var TEXT_ON_IMAGE_COLOR1 = IMAGE_BG_COLOR xor 0x00FFFFFF
        private var currentActivity: Activity? = null

        // Player ahora es parte del companion object para acceso estático
        lateinit var player: ExoPlayer

        fun getCurrentActivity(): Activity? = currentActivity

        fun setCurrentActivity(activity: Activity?) {
            currentActivity = activity
        }

        fun setTrackQuality(string: String) {
            TRACK_QUALITY = string
            SharedPreferenceManager.setTrackQuality(string)
        }

        fun cancelNotification() {
            val notificationManager = getCurrentActivity()?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(0)
        }

        fun getDownloadUrl(downloadUrlList: List<SongResponse.DownloadUrl>): String {
            if (downloadUrlList.isEmpty()) return ""

            var bestUrl = ""
            for (downloadUrl in downloadUrlList) {
                val url = downloadUrl.url ?: continue
                if (url.startsWith("https:") && downloadUrl.quality == TRACK_QUALITY) {
                    return url
                }
                if (downloadUrl.quality == TRACK_QUALITY) {
                    bestUrl = url
                }
            }

            if (bestUrl.isNotEmpty()) {
                return if (bestUrl.startsWith("http:")) {
                    val httpsUrl = bestUrl.replace("http:", "https:")
                    Log.i("ApplicationClass", "Converted HTTP to HTTPS URL: $httpsUrl")
                    httpsUrl
                } else bestUrl
            }

            val lastUrl = downloadUrlList.lastOrNull()?.url ?: ""
            return if (lastUrl.startsWith("http:")) {
                val httpsUrl = lastUrl.replace("http:", "https:")
                Log.i("ApplicationClass", "Converted HTTP to HTTPS URL: $httpsUrl")
                httpsUrl
            } else lastUrl
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val cacheDir = File(cacheDir, "audio_cache")
        val databaseProvider = StandaloneDatabaseProvider(this)
        val cacheSize = 100 * 1024 * 1024L // 100 MB
        val cacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize)
        val simpleCache = SimpleCache(cacheDir, cacheEvictor, databaseProvider)

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(Util.getUserAgent(this, "AudioCachingApp"))
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)
            .setAllowCrossProtocolRedirects(true)

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .setHandleAudioBecomingNoisy(true)
            .setAudioAttributes(
                androidx.media3.common.AudioAttributes.Builder()
                    .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                    .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.i(TAG, "Player state changed to: ${getStateString(playbackState)}")
                if (playbackState == Player.STATE_IDLE) {
                    Log.e(TAG, "Player idle state detected, might need recovery")
                    if (SONG_URL.isNotEmpty()) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            try {
                                val mediaItem = MediaItem.fromUri(SONG_URL)
                                player.setMediaItem(mediaItem)
                                player.prepare()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error recovering from player error", e)
                            }
                        }, 2000)
                    }
                } else if (playbackState == Player.STATE_READY) {
                    Log.i(TAG, "Player ready, starting playback...")
                    player.play()
                } else if (playbackState == Player.STATE_ENDED) {
                    Log.i(TAG, "Track ended, auto-playing next track")
                    nextTrack()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.i(TAG, "Player isPlaying changed to: $isPlaying")
                showNotification()
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e(TAG, "Player error: ${error.message}")
                if (SONG_URL.isNotEmpty()) {
                    Log.i(TAG, "Attempting to recover from error")
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            val mediaItem = MediaItem.fromUri(SONG_URL)
                            player.setMediaItem(mediaItem)
                            player.prepare()
                            player.play()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error recovering from player error", e)
                        }
                    }, 2000)
                }
            }
        })

        mediaSession = MediaSessionCompat(this, "ApplicationClass")
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                player.play()
                showNotification()
            }

            override fun onPause() {
                player.pause()
                showNotification()
            }

            override fun onSkipToNext() {
                nextTrack()
            }

            override fun onSkipToPrevious() {
                prevTrack()
            }
        })
        mediaSession.isActive = true

        createNotificationChannel()
        SharedPreferenceManager.init(this)
        TRACK_QUALITY = SharedPreferenceManager.getTrackQuality()
    }

    fun updateTheme() {
        getCurrentActivity()?.let { activity ->
            val settingsSharedPrefManager = SettingsActivity.SettingsSharedPrefManager(activity)
            val theme = settingsSharedPrefManager.getTheme()
            val nightMode = when (theme) {
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel1 = NotificationChannel(
                CHANNEL_ID_1,
                "Media Controls",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel1.description = "Notifications for media playback"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel1)
        }
    }

    fun setMusicDetails(image: String?, title: String?, description: String?, id: String) {
        image?.let { IMAGE_URL = it }
        title?.let { MUSIC_TITLE = it }
        description?.let { MUSIC_DESCRIPTION = it }
        MUSIC_ID = id
        Log.i(TAG, "setMusicDetails: $MUSIC_TITLE - ID: $MUSIC_ID")
    }

    fun setSongUrl(songUrl: String) {
        SONG_URL = songUrl
    }

    fun setTrackQueue(queue: List<String>) {
        track_position = -1
        trackQueue = queue.toMutableList()
    }

    fun getTrackQueue(): List<String> = trackQueue

    fun showNotification(playPauseButton: Int) {
        try {
            Log.i(TAG, "showNotification: $MUSIC_TITLE - ID: $MUSIC_ID")

            if (MUSIC_ID.isEmpty()) {
                Log.e(TAG, "Cannot show notification: Music ID is empty")
                return
            }

            if (MUSIC_TITLE.isEmpty() || IMAGE_URL.isEmpty()) {
                Log.e(TAG, "Cannot show notification: Missing title or image")
                return
            }

            val state = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
                .setState(
                    if (playPauseButton == R.drawable.play_arrow_24px)
                        PlaybackStateCompat.STATE_PAUSED
                    else
                        PlaybackStateCompat.STATE_PLAYING,
                    0, 1.0f
                )
                .build()
            mediaSession.setPlaybackState(state)

            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, MUSIC_TITLE)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, MUSIC_DESCRIPTION)
                .build()
            mediaSession.setMetadata(metadata)

            val reqCode = MUSIC_ID.hashCode()
            val intent = Intent(this, MusicOverviewActivity::class.java).apply {
                putExtra("id", MUSIC_ID)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val contentIntent = PendingIntent.getActivity(
                this,
                reqCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val prevIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_PREV)
            val prevPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                prevIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val playIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_PLAY)
            val playPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                playIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val nextIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_NEXT)
            val nextPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                nextIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID_1)
                .setSmallIcon(R.drawable.headphone)
                .setContentTitle(MUSIC_TITLE)
                .setOngoing(playPauseButton != R.drawable.play_arrow_24px)
                .setContentText(MUSIC_DESCRIPTION)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2)
                )
                .addAction(
                    NotificationCompat.Action(
                        R.drawable.skip_previous_24px,
                        "prev",
                        prevPendingIntent
                    )
                )
                .addAction(
                    NotificationCompat.Action(
                        playPauseButton,
                        "play",
                        playPendingIntent
                    )
                )
                .addAction(
                    NotificationCompat.Action(
                        R.drawable.skip_next_24px,
                        "next",
                        nextPendingIntent
                    )
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true)

            Picasso.get().load(IMAGE_URL).into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    bitmap?.let {
                        try {
                            Palette.from(it).generate { palette ->
                                palette?.dominantSwatch?.let { swatch ->
                                    IMAGE_BG_COLOR = swatch.rgb
                                    TEXT_ON_IMAGE_COLOR = swatch.titleTextColor
                                    TEXT_ON_IMAGE_COLOR1 = swatch.bodyTextColor
                                }
                            }
                            notificationBuilder.setLargeIcon(it)
                            val notification = notificationBuilder.build()
                            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.notify(0, notification)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error displaying notification with bitmap", e)
                            showBasicNotification(notificationBuilder)
                        }
                    }
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    Log.e(TAG, "Error loading image for notification", e)
                    showBasicNotification(notificationBuilder)
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
        } catch (e: Exception) {
            Log.e(TAG, "showNotification error", e)
        }
    }

    private fun showBasicNotification(builder: NotificationCompat.Builder) {
        try {
            val notification = builder.build()
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing basic notification", e)
        }
    }

    @OptIn(UnstableApi::class)
    fun togglePlayPause() {
        try {
            val wasPlaying = player.isPlaying
            Log.i(TAG, "togglePlayPause: wasPlaying=$wasPlaying")

            if (wasPlaying) {
                player.pause()
                Log.i(TAG, "Player paused, isPlaying=${player.isPlaying}")
            } else {
                if (player.playbackState == Player.STATE_IDLE) {
                    Log.i(TAG, "Player in idle state, preparing")
                    prepareMediaPlayer()
                } else {
                    player.play()
                    Log.i(TAG, "Player started, isPlaying=${player.isPlaying}")
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                showNotification()
                Log.i(TAG, "Updated notification, isPlaying=${player.isPlaying}")
            }, 100)
        } catch (e: Exception) {
            Log.e(TAG, "Error in togglePlayPause", e)
        }
    }

    @OptIn(UnstableApi::class)
    fun prepareMediaPlayer() {
        try {
            if (player.isPlaying) {
                player.stop()
            }
            player.clearMediaItems()

            if (SONG_URL.isEmpty()) {
                Log.e(TAG, "prepareMediaPlayer: No URL available to play")
                return
            }

            var finalUrl = SONG_URL
            if (finalUrl.startsWith("http:")) {
                finalUrl = finalUrl.replace("http:", "https:")
                Log.i(TAG, "Converting URL from HTTP to HTTPS: $finalUrl")
            }

            val mediaItem = MediaItem.fromUri(finalUrl)
            isTrackDownloaded = false

            if (currentActivity == null) {
                Log.e(TAG, "prepareMediaPlayer: No current activity set")
                return
            }

            player.setMediaItem(mediaItem)
            player.playWhenReady = true
            player.prepare()

            showNotification()

            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    if (!player.isPlaying && player.playbackState == Player.STATE_READY) {
                        Log.i(TAG, "Starting delayed playback")
                        player.play()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in delayed play", e)
                }
            }, 500)
        } catch (e: Exception) {
            Log.e(TAG, "prepareMediaPlayer error", e)
        }
    }

    @OptIn(UnstableApi::class)
    fun nextTrack() {
        if (trackQueue.isEmpty()) {
            Log.i(TAG, "Cannot play next track: track queue is empty")
            return
        }

        val repeatMode = player.repeatMode
        val shuffleEnabled = player.shuffleModeEnabled

        if (track_position >= trackQueue.size - 1) {
            when (repeatMode) {
                Player.REPEAT_MODE_ONE -> {
                    player.seekTo(0)
                    player.play()
                    Log.i(TAG, "Repeat ONE mode - restarting current track")
                    return
                }
                Player.REPEAT_MODE_ALL -> {
                    track_position = 0
                    Log.i(TAG, "Repeat ALL mode - looping back to first track in queue")
                }
                Player.REPEAT_MODE_OFF -> {
                    Log.i(TAG, "Repeat OFF mode - end of queue")
                    return
                }
            }
        } else {
            if (shuffleEnabled) {
                var newPosition: Int
                do {
                    newPosition = (0 until trackQueue.size).random()
                } while (newPosition == track_position && trackQueue.size > 1)
                track_position = newPosition
                Log.i(TAG, "Shuffle enabled, random next track position: $track_position")
            } else {
                track_position++
                Log.i(TAG, "Playing next track at position: $track_position")
            }
        }

        try {
            MUSIC_ID = trackQueue[track_position]
            Log.i(TAG, "Playing next track: $MUSIC_ID at position $track_position")
            playTrack()
            showNotification()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing next track", e)
            if (trackQueue.isNotEmpty()) {
                track_position = 0
                MUSIC_ID = trackQueue[0]
                playTrack()
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun prevTrack() {
        if (trackQueue.isEmpty()) {
            Log.i(TAG, "Cannot play previous track: track queue is empty")
            return
        }

        if (track_position <= 0) {
            if (player.repeatMode == Player.REPEAT_MODE_ALL) {
                track_position = trackQueue.size - 1
                Log.i(TAG, "Looping to last track in queue (position $track_position)")
            } else {
                Log.i(TAG, "At first track, restarting current track")
                player.seekTo(0)
                player.play()
                return
            }
        } else {
            if (player.shuffleModeEnabled) {
                track_position = (0 until trackQueue.size).random()
                Log.i(TAG, "Shuffle enabled, random previous track position: $track_position")
            } else {
                track_position--
                Log.i(TAG, "Playing previous track at position: $track_position")
            }
        }

        MUSIC_ID = trackQueue[track_position]
        Log.i(TAG, "Playing previous track: $MUSIC_ID")
        playTrack()
        showNotification()
    }

    private fun getStateString(state: Int): String {
        return when (state) {
            Player.STATE_IDLE -> "IDLE"
            Player.STATE_BUFFERING -> "BUFFERING"
            Player.STATE_READY -> "READY"
            Player.STATE_ENDED -> "ENDED"
            else -> "UNKNOWN"
        }
    }

    @OptIn(UnstableApi::class)
    private fun playTrack() {
        val apiManager = ApiManager(getCurrentActivity()!!)
        apiManager.retrieveSongById(MUSIC_ID, null, object : RequestNetwork.RequestListener {
            override fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>) {
                try {
                    val songResponse = Gson().fromJson(response, SongResponse::class.java)
                    if (songResponse.success) {
                        val data = songResponse.data.firstOrNull()
                        data?.let {
                            MUSIC_TITLE = it.name
                            MUSIC_DESCRIPTION = String.format(
                                "%s plays | %s | %s",
                                MusicOverviewActivity.convertPlayCount(it.playCount ?: 0),
                                it.year,
                                it.copyright
                            )
                            IMAGE_URL = it.image.lastOrNull()?.url ?: ""
                            val downloadUrls = it.downloadUrl
                            SONG_URL = getDownloadUrl(downloadUrls)
                            setMusicDetails(IMAGE_URL, MUSIC_TITLE, MUSIC_DESCRIPTION, MUSIC_ID)
                            prepareMediaPlayer()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in playTrack onResponse", e)
                }
            }

            override fun onErrorResponse(tag: String, message: String) {
                Log.e(TAG, "Error fetching song: $message")
            }
        })
    }

    @OptIn(UnstableApi::class)
    fun showNotification() {
        showNotification(if (player.isPlaying) R.drawable.baseline_pause_24 else R.drawable.play_arrow_24px)
    }
}