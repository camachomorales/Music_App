package com.example.music.activities

import android.app.ProgressDialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import com.example.music.ApplicationClass
import com.example.music.R
import com.example.music.api.model.AlbumItem
import com.example.music.api.model.BasicDataRecord
import com.example.music.databinding.ActivityMusicOverviewBinding
import com.example.music.databinding.MusicOverviewMoreInfoBottomSheetBinding
import com.example.music.network.ApiManager
import com.example.music.network.utility.RequestNetwork
import com.example.music.records.SongResponse
import com.example.music.records.sharedpref.SavedLibraries
import com.example.music.services.MusicService
import com.example.music.utils.SharedPreferenceManager
import com.example.music.utils.TrackDownloader
import com.example.music.utils.customview.BottomSheetItemView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.squareup.picasso.Picasso

class MusicOverviewActivity : AppCompatActivity(), MusicService.ActionPlaying, ServiceConnection {

    private lateinit var binding: ActivityMusicOverviewBinding
    private val handler = Handler(Looper.getMainLooper())
    private val mHandler = Handler(Looper.getMainLooper())

    private var songUrl = ""
    private var idFromExtra = ""
    private var imageUrl = ""
    private var shareUrl = ""
    private var musicService: MusicService? = null
    private var artistsList: List<SongResponse.Artist> = ArrayList()
    private var mSongResponse: SongResponse? = null
    private val isDebugMode = false

    companion object {
        private const val TAG = "MusicOverviewActivity"

        fun convertPlayCount(playCount: Int): String {
            return when {
                playCount < 1000 -> playCount.toString()
                playCount < 1000000 -> "${playCount / 1000}K"
                else -> "${playCount / 1000000}M"
            }
        }

        fun convertDuration(duration: Long): String {
            val hours = (duration / (1000 * 60 * 60)).toInt()
            val minutes = (duration % (1000 * 60 * 60) / (1000 * 60)).toInt()
            val seconds = ((duration % (1000 * 60 * 60)) % (1000 * 60) / 1000).toInt()

            val secondString = if (seconds < 10) "0$seconds" else seconds.toString()

            return if (hours > 0) {
                "$hours:$minutes:$secondString"
            } else {
                "$minutes:$secondString"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.title.isSelected = true
        binding.description.isSelected = true

        val applicationClass = application as ApplicationClass
        if (applicationClass.getTrackQueue().size <= 1) {
            binding.shuffleIcon.visibility = View.INVISIBLE
        }

        setupPlayPauseButton()
        setupSeekBar()
        setupNavigationButtons()
        setupControlButtons()
        setupShareButton()
        setupMoreInfoButton()
        setupTrackQualityButton()

        binding.trackQuality.text = ApplicationClass.TRACK_QUALITY

        showData()
        updateTrackInfo()
    }

    private fun setupPlayPauseButton() {
        binding.playPauseImage.setOnClickListener {
            try {
                if (ApplicationClass.player == null) {
                    Log.e(TAG, "Player is null, cannot toggle playback")
                    Toast.makeText(this, "Media player not ready. Try again.", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                val player = ApplicationClass.player!!
                if (player.isPlaying) {
                    Log.i(TAG, "Pausing playback")
                    handler.removeCallbacks(runnable)
                    player.pause()
                    binding.playPauseImage.setImageResource(R.drawable.play_arrow_24px)
                } else {
                    Log.i(TAG, "Starting playback")
                    player.play()
                    binding.playPauseImage.setImageResource(R.drawable.baseline_pause_24)
                    updateSeekbar()
                }

                val isPlaying = player.isPlaying
                Log.i(TAG, "Player state after click: isPlaying=$isPlaying")
                val applicationClass = application as ApplicationClass
                applicationClass.showNotification(if (isPlaying) R.drawable.baseline_pause_24 else R.drawable.play_arrow_24px)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling playback", e)
                Toast.makeText(this, "Error controlling playback. Try again.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setupSeekBar() {
        binding.seekbar.max = 100

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val player = ApplicationClass.player ?: return
                val playPosition = (player.duration / 100) * binding.seekbar.progress
                player.seekTo(playPosition)
                binding.elapsedDuration.text = convertDuration(player.currentPosition)
            }
        })
    }

    private fun setupNavigationButtons() {
        val applicationClass = application as ApplicationClass

        binding.nextIcon.setOnClickListener {
            try {
                Log.i(TAG, "Next button clicked")
                if (ApplicationClass.player == null) {
                    Log.e(TAG, "Player is null, cannot skip to next track")
                    Toast.makeText(this, "Media player not ready", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                binding.nextIcon.alpha = 0.5f
                binding.nextIcon.animate().alpha(1.0f).setDuration(200).start()

                applicationClass.nextTrack()

                updateSeekbar()
                updateTrackInfo()

                binding.playPauseImage.setImageResource(
                    if (ApplicationClass.player?.isPlaying == true)
                        R.drawable.baseline_pause_24
                    else
                        R.drawable.play_arrow_24px
                )

                if (isDebugMode) {
                    Toast.makeText(this, "Playing next track", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error skipping to next track", e)
                Toast.makeText(this, "Error skipping to next track", Toast.LENGTH_SHORT).show()
            }
        }

        binding.prevIcon.setOnClickListener {
            try {
                Log.i(TAG, "Previous button clicked")
                val player = ApplicationClass.player
                if (player == null) {
                    Log.e(TAG, "Player is null, cannot skip to previous track")
                    Toast.makeText(this, "Media player not ready", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                binding.prevIcon.alpha = 0.5f
                binding.prevIcon.animate().alpha(1.0f).setDuration(200).start()

                if (player.currentPosition > 3000) {
                    player.seekTo(0)
                    if (isDebugMode) {
                        Toast.makeText(this, "Restarting current track", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    applicationClass.prevTrack()
                    if (isDebugMode) {
                        Toast.makeText(this, "Playing previous track", Toast.LENGTH_SHORT).show()
                    }
                }

                updateSeekbar()
                updateTrackInfo()

                binding.playPauseImage.setImageResource(
                    if (player.isPlaying)
                        R.drawable.baseline_pause_24
                    else
                        R.drawable.play_arrow_24px
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error going to previous track", e)
                Toast.makeText(this, "Error going to previous track", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupControlButtons() {
        binding.repeatIcon.setOnClickListener {
            try {
                val player = ApplicationClass.player ?: return@setOnClickListener
                val currentMode = player.repeatMode
                val (newMode, modeMessage) = when (currentMode) {
                    Player.REPEAT_MODE_OFF -> Pair(Player.REPEAT_MODE_ONE, "Repeat One")
                    Player.REPEAT_MODE_ONE -> Pair(Player.REPEAT_MODE_ALL, "Repeat All")
                    else -> Pair(Player.REPEAT_MODE_OFF, "Repeat Off")
                }

                player.repeatMode = newMode
                updateRepeatButtonUI()

                if (isDebugMode) {
                    Toast.makeText(this, modeMessage, Toast.LENGTH_SHORT).show()
                }

                Log.i(TAG, "Repeat mode changed to: $newMode")
            } catch (e: Exception) {
                Log.e(TAG, "Error changing repeat mode", e)
                Toast.makeText(this, "Error changing repeat mode", Toast.LENGTH_SHORT).show()
            }
        }

        binding.shuffleIcon.setOnClickListener {
            val player = ApplicationClass.player ?: return@setOnClickListener
            player.shuffleModeEnabled = !player.shuffleModeEnabled

            val tintColor = if (player.shuffleModeEnabled) {
                ContextCompat.getColor(this, R.color.spotify_green)
            } else {
                ContextCompat.getColor(this, R.color.textSec)
            }
            binding.shuffleIcon.imageTintList = ColorStateList.valueOf(tintColor)

            if (isDebugMode) {
                Toast.makeText(this, "Shuffle Mode Changed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupShareButton() {
        binding.shareIcon.setOnClickListener {
            if (shareUrl.isBlank()) return@setOnClickListener
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareUrl)
                type = "text/plain"
            }
            startActivity(sendIntent)
        }
    }

    private fun setupMoreInfoButton() {
        binding.moreIcon.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme)
            val _binding = MusicOverviewMoreInfoBottomSheetBinding.inflate(layoutInflater)

            _binding.albumTitle.text = binding.title.text.toString()
            _binding.albumSubTitle.text = binding.description.text.toString()
            Picasso.get().load(Uri.parse(imageUrl)).into(_binding.coverImage)

            val linearLayout = _binding.main

            setupGoToAlbum(_binding)
            setupAddToLibrary(_binding)
            setupDownload(_binding)
            setupArtistsList(linearLayout)

            bottomSheetDialog.setContentView(_binding.root)
            bottomSheetDialog.show()
        }
    }

    private fun setupGoToAlbum(_binding: MusicOverviewMoreInfoBottomSheetBinding) {
        _binding.goToAlbum.setOnClickListener {
            val songResponse = mSongResponse ?: return@setOnClickListener
            val album = songResponse.data[0].album ?: return@setOnClickListener

            startActivity(Intent(this, ListActivity::class.java).apply {
                putExtra("type", "album")
                putExtra("id", album.id)
                putExtra("data", Gson().toJson(AlbumItem(album.name, "", "", album.id)))
            })
        }
    }

    private fun setupAddToLibrary(_binding: MusicOverviewMoreInfoBottomSheetBinding) {
        _binding.addToLibrary.setOnClickListener {
            var savedLibraries = SharedPreferenceManager.getSavedLibrariesData()

            if (savedLibraries == null) {
                savedLibraries = SavedLibraries(ArrayList())
            }

            if (savedLibraries.lists.isEmpty()) {
                Snackbar.make(_binding.root, "No Libraries Found", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userCreatedLibraries = savedLibraries.lists
                .filter { it.isCreatedByUser }
                .map { it.name }
                .toList()

            val materialAlertDialogBuilder = getMaterialAlertDialogBuilder(
                userCreatedLibraries,
                savedLibraries
            )
            materialAlertDialogBuilder.show()
        }
    }

    @Suppress("DEPRECATION")
    private fun setupDownload(_binding: MusicOverviewMoreInfoBottomSheetBinding) {
        val song = mSongResponse?.data?.get(0) ?: return

        if (TrackDownloader.isAlreadyDownloaded(song.name)) {
            _binding.download.getTitleTextView().text = "Download Manager"
        }

        _binding.download.setOnClickListener {
            if (TrackDownloader.isAlreadyDownloaded(song.name)) {
                startActivity(Intent(this, DownloadManagerActivity::class.java))
                return@setOnClickListener
            }

            val progressDialog = ProgressDialog(this).apply {
                setMessage("Downloading...")
                setCancelable(false)
                setCanceledOnTouchOutside(false)
            }

            TrackDownloader.downloadAndEmbedMetadata(
                this,
                song,
                object : TrackDownloader.TrackDownloadListener {
                    override fun onStarted() {
                        progressDialog.show()
                    }

                    override fun onFinished() {
                        progressDialog.dismiss()
                        if (TrackDownloader.isAlreadyDownloaded(song.name)) {
                            Toast.makeText(
                                this@MusicOverviewActivity,
                                "Successfully Downloaded.",
                                Toast.LENGTH_SHORT
                            ).show()
                            _binding.download.getTitleTextView().text = "Download Manager"
                        }
                    }

                    override fun onError(errorMessage: String) {
                        Toast.makeText(this@MusicOverviewActivity, errorMessage, Toast.LENGTH_SHORT)
                            .show()
                        MaterialAlertDialogBuilder(this@MusicOverviewActivity).apply {
                            setTitle("Error")
                            setMessage(errorMessage)
                            setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            show()
                        }
                    }
                }
            )
        }
    }

    private fun setupArtistsList(linearLayout: android.widget.LinearLayout) {
        artistsList.forEach { artist ->
            try {
                val imgUrl = artist.image.lastOrNull()?.url ?: ""
                val bottomSheetItemView =
                    BottomSheetItemView(this, artist.name, imgUrl, artist.id).apply {
                        isFocusable = true
                        isClickable = true
                        setOnClickListener {
                            Log.i(TAG, "BottomSheetItemView: onClick!")
                            startActivity(
                                Intent(
                                    this@MusicOverviewActivity,
                                    ArtistProfileActivity::class.java
                                ).apply {
                                    putExtra(
                                        "data",
                                        Gson().toJson(
                                            BasicDataRecord(
                                                artist.id,
                                                artist.name,
                                                "",
                                                imgUrl
                                            )
                                        )
                                    )
                                })
                        }
                    }
                linearLayout.addView(bottomSheetItemView)
            } catch (e: Exception) {
                Log.e(TAG, "BottomSheetDialog error: ", e)
            }
        }
    }

    private fun setupTrackQualityButton() {
        binding.trackQuality.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.track_quality_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                Toast.makeText(this, menuItem.title, Toast.LENGTH_SHORT).show()
                ApplicationClass.setTrackQuality(menuItem.title.toString())
                mSongResponse?.let { onSongFetched(it, true) }
                prepareMediaPlayer()
                binding.trackQuality.text = ApplicationClass.TRACK_QUALITY
                true
            }
            popupMenu.show()
        }
    }

    private fun getMaterialAlertDialogBuilder(
        userCreatedLibraries: List<String>,
        savedLibraries: SavedLibraries
    ): MaterialAlertDialogBuilder {
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        val listAdapter: ListAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            userCreatedLibraries
        )

        materialAlertDialogBuilder.setAdapter(listAdapter) { _, i ->
            Log.i(TAG, "pickedLibrary: $i")

            val song = mSongResponse?.data?.get(0) ?: return@setAdapter

            val newSong = SavedLibraries.Library.Song(
                id = song.id,
                title = song.name,
                description = binding.description.text.toString(),
                image = imageUrl
            )

            val updatedSongs = savedLibraries.lists[i].songs.toMutableList()
            updatedSongs.add(newSong)

            val updatedLibrary = savedLibraries.lists[i].copy(songs = updatedSongs)

            val updatedLists = savedLibraries.lists.toMutableList()
            updatedLists[i] = updatedLibrary

            SharedPreferenceManager.setSavedLibrariesData(SavedLibraries(updatedLists))

            Toast.makeText(
                this,
                "Added to ${savedLibraries.lists[i].name}",
                Toast.LENGTH_SHORT
            ).show()
        }

        materialAlertDialogBuilder.setTitle("Select Library")
        return materialAlertDialogBuilder
    }

    override fun onResume() {
        super.onResume()

        ApplicationClass.setCurrentActivity(this)

        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)

        ApplicationClass.player?.let {
            updateTrackInfo()
            if (it.isPlaying) {
                updateSeekbar()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
        mHandler.removeCallbacks(mUpdateTimeTask)

        try {
            unbindService(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error unbinding service", e)
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(runnable)
        mHandler.removeCallbacks(mUpdateTimeTask)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        mHandler.removeCallbacks(mUpdateTimeTask)
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val binder = service as MusicService.MyBinder
        musicService = binder.getService()
        musicService?.setCallback(this)
        Log.i(TAG, "onServiceConnected")
    }

    override fun onServiceDisconnected(name: ComponentName) {
        Log.e(TAG, "onServiceDisconnected")
        musicService = null
    }

    private fun showData() {
        val extras = intent.extras ?: return
        val apiManager = ApiManager(this)
        val id = extras.getString("id", "")
        idFromExtra = id

        if (ApplicationClass.MUSIC_ID == id) {
            updateSeekbar()
            binding.playPauseImage.setImageResource(
                if (ApplicationClass.player?.isPlaying == true)
                    R.drawable.baseline_pause_24
                else
                    R.drawable.play_arrow_24px
            )
        }

        val requestListener = object : RequestNetwork.RequestListener {
            override fun onResponse(
                tag: String,
                response: String,
                responseHeaders: HashMap<String, Any>
            ) {
                val songResponse = Gson().fromJson(response, SongResponse::class.java)
                if (songResponse.success) {
                    onSongFetched(songResponse)
                    SharedPreferenceManager.setSongResponseById(id, songResponse)
                } else {
                    val cachedResponse = SharedPreferenceManager.getSongResponseById(id)
                    if (cachedResponse != null) {
                        onSongFetched(cachedResponse)
                    } else {
                        finish()
                    }
                }
            }

            override fun onErrorResponse(tag: String, message: String) {
                val cachedResponse = SharedPreferenceManager.getSongResponseById(id)
                if (cachedResponse != null) {
                    onSongFetched(cachedResponse)
                } else {
                    Toast.makeText(this@MusicOverviewActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (extras.getString("type", "") == "clear") {
            val applicationClass = application as ApplicationClass
            applicationClass.setTrackQueue(ArrayList(listOf(id)))
        }

        when {
            (id.startsWith("http") || id.startsWith("www")) && id.contains("jiosaavn.com") -> {
                apiManager.retrieveSongByLink(id, requestListener)
            }

            else -> {
                val cachedResponse = SharedPreferenceManager.getSongResponseById(id)
                if (cachedResponse != null) {
                    onSongFetched(cachedResponse)
                } else {
                    apiManager.retrieveSongById(id, null, requestListener)
                }
            }
        }
    }

    private fun onSongFetched(songResponse: SongResponse, forced: Boolean = false) {
        mSongResponse = songResponse
        ApplicationClass.CURRENT_TRACK = songResponse

        val song = songResponse.data[0]
        binding.title.text = song.name
        binding.description.text = String.format(
            "%s plays | %s | %s",
            convertPlayCount(song.playCount ?: 0),
            song.year,
            song.copyright
        )

        val image = song.image
        imageUrl = image.last().url
        shareUrl = song.url
        Picasso.get().load(Uri.parse(image.last().url)).into(binding.coverImage)

        val downloadUrls = song.downloadUrl
        artistsList = song.artists.primary

        songUrl = ApplicationClass.getDownloadUrl(downloadUrls)

        if (ApplicationClass.MUSIC_ID != idFromExtra || forced) {
            val applicationClass = application as ApplicationClass
            applicationClass.setMusicDetails(
                imageUrl,
                binding.title.text.toString(),
                binding.description.text.toString(),
                idFromExtra
            )
            applicationClass.setSongUrl(songUrl)
            prepareMediaPlayer()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun backPress(view: View) {
        finish()
    }

    private fun prepareMediaPlayer() {
        try {
            val applicationClass = application as ApplicationClass
            applicationClass.prepareMediaPlayer()

            val player = ApplicationClass.player
            if (player != null && player.duration > 0) {
                binding.totalDuration.text = convertDuration(player.duration)
            } else {
                binding.totalDuration.text = "00:00"
                Handler(Looper.getMainLooper()).postDelayed({
                    ApplicationClass.player?.let {
                        if (it.duration > 0) {
                            binding.totalDuration.text = convertDuration(it.duration)
                        }
                    }
                }, 500)
            }

            if (player?.isPlaying == true) {
                binding.playPauseImage.setImageResource(R.drawable.baseline_pause_24)
                updateSeekbar()
            } else {
                binding.playPauseImage.setImageResource(R.drawable.play_arrow_24px)
            }

            applicationClass.showNotification(
                if (player?.isPlaying == true)
                    R.drawable.baseline_pause_24
                else
                    R.drawable.play_arrow_24px
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing media player", e)
            Toast.makeText(this, "Error playing track. Retrying...", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({ prepareMediaPlayer() }, 1000)
        }
    }

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            updateSeekbar()
        }
    }

    private fun updateSeekbar() {
        try {
            val player = ApplicationClass.player
            if (player == null) {
                Log.e(TAG, "Player is null in updateSeekbar")
                return
            }

            if (player.isPlaying) {
                try {
                    val duration = player.duration
                    val currentPosition = player.currentPosition

                    if (duration > 0) {
                        val progress = ((currentPosition.toFloat() / duration) * 100).toInt()
                        binding.seekbar.progress = progress
                        binding.elapsedDuration.text = convertDuration(currentPosition)
                    }

                    handler.postDelayed(runnable, 1000)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating seekbar", e)
                }
            } else {
                try {
                    val duration = player.duration
                    val currentPosition = player.currentPosition

                    if (duration > 0) {
                        val progress = ((currentPosition.toFloat() / duration) * 100).toInt()
                        binding.seekbar.progress = progress
                        binding.elapsedDuration.text = convertDuration(currentPosition)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating seekbar while paused", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in updateSeekbar", e)
        }
    }

    private val mUpdateTimeTask: Runnable = object : Runnable {
        override fun run() {
            updateTrackInfo()
        }
    }

    private fun updateTrackInfo() {
        val player = ApplicationClass.player ?: return

        if (binding.title.text.toString() != ApplicationClass.MUSIC_TITLE) {
            binding.title.text = ApplicationClass.MUSIC_TITLE
        }
        if (binding.description.text.toString() != ApplicationClass.MUSIC_DESCRIPTION) {
            binding.description.text = ApplicationClass.MUSIC_DESCRIPTION
        }

        Picasso.get().load(Uri.parse(ApplicationClass.IMAGE_URL)).into(binding.coverImage)

        val progress = ((player.currentPosition.toFloat() / player.duration) * 100).toInt()
        binding.seekbar.progress = progress

        val secondaryProgress =
            ((player.bufferedPosition.toFloat() / player.duration) * 100).toInt()
        binding.seekbar.secondaryProgress = secondaryProgress

        binding.elapsedDuration.text = convertDuration(player.currentPosition)

        if (binding.totalDuration.text.toString() != convertDuration(player.duration)) {
            binding.totalDuration.text = convertDuration(player.duration)
        }

        binding.playPauseImage.setImageResource(
            if (player.isPlaying)
                R.drawable.baseline_pause_24
            else
                R.drawable.play_arrow_24px
        )

        updateRepeatButtonUI()

        val shuffleTintColor = if (player.shuffleModeEnabled) {
            ContextCompat.getColor(this, R.color.spotify_green)
        } else {
            ContextCompat.getColor(this, R.color.textSec)
        }
        binding.shuffleIcon.imageTintList = ColorStateList.valueOf(shuffleTintColor)

        mHandler.postDelayed(mUpdateTimeTask, 1000)
    }

    private fun updateRepeatButtonUI() {
        val player = ApplicationClass.player ?: return
        val repeatMode = player.repeatMode

        val (tintColor, iconResource) = when (repeatMode) {
            Player.REPEAT_MODE_ONE -> {
                try {
                    Pair(
                        ContextCompat.getColor(this, R.color.spotify_green),
                        R.drawable.repeat_one_24px
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting repeat_one icon: ${e.message}")
                    Pair(
                        ContextCompat.getColor(this, R.color.spotify_green),
                        R.drawable.repeat_24px
                    )
                }
            }

            Player.REPEAT_MODE_ALL -> {
                Pair(
                    ContextCompat.getColor(this, R.color.spotify_green),
                    R.drawable.repeat_24px
                )
            }

            else -> {
                Pair(
                    ContextCompat.getColor(this, R.color.textSec),
                    R.drawable.repeat_24px
                )
            }
        }

        binding.repeatIcon.setImageResource(iconResource)
        binding.repeatIcon.imageTintList = ColorStateList.valueOf(tintColor)
    }

    override fun nextClicked() {
        Log.i(TAG, "nextClicked called from service")
        try {
            val player = ApplicationClass.player
            if (player == null) {
                Log.e(TAG, "Player is null in nextClicked")
                return
            }

            binding.nextIcon.alpha = 0.5f
            binding.nextIcon.animate().alpha(1.0f).setDuration(200).start()

            val applicationClass = application as ApplicationClass
            applicationClass.nextTrack()

            updateTrackInfo()
            updateSeekbar()

            binding.playPauseImage.setImageResource(
                if (player.isPlaying)
                    R.drawable.baseline_pause_24
                else
                    R.drawable.play_arrow_24px
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in nextClicked", e)
        }
    }

    override fun prevClicked() {
        Log.i(TAG, "prevClicked called from service")
        try {
            val player = ApplicationClass.player
            if (player == null) {
                Log.e(TAG, "Player is null in prevClicked")
                return
            }

            binding.prevIcon.alpha = 0.5f
            binding.prevIcon.animate().alpha(1.0f).setDuration(200).start()

            val applicationClass = application as ApplicationClass

            if (player.currentPosition > 3000) {
                player.seekTo(0)
            } else {
                applicationClass.prevTrack()
            }

            updateTrackInfo()
            updateSeekbar()

            binding.playPauseImage.setImageResource(
                if (player.isPlaying)
                    R.drawable.baseline_pause_24
                else
                    R.drawable.play_arrow_24px
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in prevClicked", e)
        }
    }

    override fun playClicked() {
        binding.playPauseImage.setImageResource(
            if (ApplicationClass.player?.isPlaying != true)
                R.drawable.play_arrow_24px
            else
                R.drawable.baseline_pause_24
        )
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        // Implementación vacía o puedes agregar lógica aquí si es necesario
    }

    fun showNotification(playPauseButton: Int) {
        val applicationClass = applicationContext as ApplicationClass
        val songId = intent.extras?.getString("id") ?: ""
        applicationClass.setMusicDetails(
            imageUrl,
            binding.title.text.toString(),
            binding.description.text.toString(),
            songId
        )
        Log.i(TAG, "MusicOverviewActivity showNotification for song ID: $songId")
        applicationClass.showNotification(playPauseButton)
    }
}