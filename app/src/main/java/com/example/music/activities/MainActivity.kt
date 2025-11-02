package com.example.music.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.ApplicationClass
import com.example.music.R
import com.example.music.adapters.ActivityMainAlbumItemAdapter
import com.example.music.adapters.ActivityMainArtistsItemAdapter
import com.example.music.adapters.ActivityMainPlaylistAdapter
import com.example.music.adapters.ActivityMainPopularSongs
import com.example.music.adapters.SavedLibrariesAdapter
import com.example.music.api.model.AlbumItem
import com.example.music.databinding.ActivityMainBinding
import com.example.music.network.ApiManager
import com.example.music.network.NetworkChangeReceiver
import com.example.music.network.utility.RequestNetwork
import com.example.music.records.AlbumsSearch
import com.example.music.records.ArtistsSearch
import com.example.music.records.PlaylistsSearch
import com.example.music.records.SongSearch
import com.example.music.utils.NetworkUtil
import com.example.music.utils.SharedPreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.yarolegovich.slidingrootnav.SlidingRootNav
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import org.json.JSONException
import org.json.JSONObject
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var applicationClass: ApplicationClass
    private lateinit var slidingRootNavBuilder: SlidingRootNav
    private val handler = Handler(Looper.getMainLooper())

    private val songs = mutableListOf<AlbumItem>()
    private val artists = mutableListOf<ArtistsSearch.Data.Results>()
    private val albums = mutableListOf<AlbumItem>()
    private val playlists = mutableListOf<AlbumItem>()

    private val networkChangeReceiver = NetworkChangeReceiver(object : NetworkChangeReceiver.NetworkStatusListener {
        override fun onNetworkConnected() {
            if (songs.isEmpty() || artists.isEmpty() || albums.isEmpty() || playlists.isEmpty()) {
                showData()
            }
        }

        override fun onNetworkDisconnected() {
            if (songs.isEmpty() || artists.isEmpty() || albums.isEmpty() || playlists.isEmpty()) {
                showOfflineData()
            }
            Snackbar.make(binding.root, "No Internet Connection", Snackbar.LENGTH_LONG).show()
        }
    })

    private lateinit var requestStoragePermission: ActivityResultLauncher<Array<String>>

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle notification permission result
    }

    companion object {
        private const val TAG = "MainActivity"

        fun calculateNoOfColumns(context: Context, columnWidthDp: Float): Int {
            val displayMetrics = context.resources.displayMetrics
            val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
            return ((screenWidthDp / columnWidthDp) + 0.5).toInt()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applicationClass = applicationContext as ApplicationClass
        ApplicationClass.setCurrentActivity(this)
        applicationClass.updateTheme()

        slidingRootNavBuilder = SlidingRootNavBuilder(this)
            .withMenuLayout(R.layout.main_drawer_layout)
            .withContentClickableWhenMenuOpened(false)
            .withDragDistance(250)
            .inject()

        updateVersionTextInDrawer()
        onDrawerItemsClicked()

        binding.profileIcon.setOnClickListener {
            slidingRootNavBuilder.openMenu(true)
        }

        val span = calculateNoOfColumns(this, 200f)
        binding.playlistRecyclerView.layoutManager = GridLayoutManager(this, span)

        binding.popularSongsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.popularArtistsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.popularAlbumsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.savedRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        OverScrollDecoratorHelper.setUpOverScroll(binding.popularSongsRecyclerView, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
        OverScrollDecoratorHelper.setUpOverScroll(binding.popularArtistsRecyclerView, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
        OverScrollDecoratorHelper.setUpOverScroll(binding.popularAlbumsRecyclerView, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
        OverScrollDecoratorHelper.setUpOverScroll(binding.savedRecyclerView, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)

        binding.refreshLayout.setOnRefreshListener {
            showShimmerData()
            showData()
            binding.refreshLayout.isRefreshing = false
        }

        setupPlayBarControls()
        setupBackPressHandler()

        showShimmerData()
        showOfflineData()
        showPlayBarData()
        showSavedLibrariesData()
        askNotificationPermission()

        requestStoragePermission = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            if (result.containsValue(false)) {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            }
        }

        getStoragePermission()
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (slidingRootNavBuilder.isMenuOpened) {
                    slidingRootNavBuilder.closeMenu()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupPlayBarControls() {
        binding.playBarPlayPauseIcon.setOnClickListener {
            applicationClass.togglePlayPause()
            val isPlaying = ApplicationClass.player.isPlaying  // CORRECCIÓN AQUÍ
            applicationClass.showNotification(
                if (isPlaying) R.drawable.baseline_pause_24 else R.drawable.play_arrow_24px
            )
            binding.playBarPlayPauseIcon.setImageResource(
                if (isPlaying) R.drawable.baseline_pause_24 else R.drawable.play_arrow_24px
            )
        }

        binding.playBarBackground.setOnClickListener {
            if (ApplicationClass.MUSIC_ID.isNotBlank()) {
                startActivity(Intent(this, MusicOverviewActivity::class.java).apply {
                    putExtra("id", ApplicationClass.MUSIC_ID)
                })
            }
        }

        binding.playBarPrevIcon.setOnClickListener {
            try {
                Log.i(TAG, "Play bar previous button clicked")

                binding.playBarPrevIcon.alpha = 0.5f
                binding.playBarPrevIcon.animate().alpha(1.0f).setDuration(200).start()

                applicationClass.prevTrack()
                updatePlaybarUi()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling previous button click", e)
            }
        }

        binding.playBarNextIcon.setOnClickListener {
            try {
                Log.i(TAG, "Play bar next button clicked")

                binding.playBarNextIcon.alpha = 0.5f
                binding.playBarNextIcon.animate().alpha(1.0f).setDuration(200).start()

                applicationClass.nextTrack()
                updatePlaybarUi()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling next button click", e)
            }
        }

    }

    private fun getStoragePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            requestStoragePermissionIfNeeded()
        }
    }

    private fun requestStoragePermissionIfNeeded() {
        if (!checkIfStorageAccessAvailable()) {
            requestStoragePermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun checkIfStorageAccessAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            true
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun showSavedLibrariesData() {
        val savedLibraries = SharedPreferenceManager.getSavedLibrariesData()
        binding.savedLibrariesSection.visibility =
            if (savedLibraries != null && savedLibraries.lists.isNotEmpty())
                View.VISIBLE
            else
                View.GONE

        savedLibraries?.lists?.let {
            binding.savedRecyclerView.adapter = SavedLibrariesAdapter(it)
        }
    }

    private fun onDrawerItemsClicked() {
        slidingRootNavBuilder.layout.findViewById<View>(R.id.settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            slidingRootNavBuilder.closeMenu()
        }

        slidingRootNavBuilder.layout.findViewById<View>(R.id.logo).setOnClickListener {
            slidingRootNavBuilder.closeMenu()
        }

        slidingRootNavBuilder.layout.findViewById<View>(R.id.library).setOnClickListener {
            startActivity(Intent(this, SavedLibrariesActivity::class.java))
            slidingRootNavBuilder.closeMenu()
        }

        slidingRootNavBuilder.layout.findViewById<View>(R.id.about).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
            slidingRootNavBuilder.closeMenu()
        }

        slidingRootNavBuilder.layout.findViewById<View>(R.id.download_manager).setOnClickListener {
            startActivity(Intent(this, DownloadManagerActivity::class.java))
            slidingRootNavBuilder.closeMenu()
        }
    }

    private fun updateVersionTextInDrawer() {
        try {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            val drawerLayout = slidingRootNavBuilder.layout
            drawerLayout?.findViewById<android.widget.TextView>(R.id.versionTxt)?.text = "version $versionName"
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Error getting app version: ${e.message}")
        }
    }

    private val runnable = Runnable { showPlayBarData() }

    private fun showPlayBarData() {
        binding.playBarBackground.visibility =
            if (ApplicationClass.MUSIC_ID.isBlank()) View.GONE else View.VISIBLE

        binding.playBarMusicTitle.text = ApplicationClass.MUSIC_TITLE
        binding.playBarMusicDesc.text = ApplicationClass.MUSIC_DESCRIPTION
        Picasso.get().load(Uri.parse(ApplicationClass.IMAGE_URL)).into(binding.playBarCoverImage)
        updatePlaybarUi()

        val gradientDrawable = GradientDrawable().apply {
            setColor(ApplicationClass.IMAGE_BG_COLOR)
            cornerRadius = 18f
        }

        binding.playBarBackground.background = gradientDrawable

        binding.playBarMusicTitle.setTextColor(ApplicationClass.TEXT_ON_IMAGE_COLOR1)
        binding.playBarMusicDesc.setTextColor(ApplicationClass.TEXT_ON_IMAGE_COLOR1)

        binding.playBarPlayPauseIcon.imageTintList = ColorStateList.valueOf(ApplicationClass.TEXT_ON_IMAGE_COLOR)
        binding.playBarPrevIcon.imageTintList = ColorStateList.valueOf(ApplicationClass.TEXT_ON_IMAGE_COLOR)
        binding.playBarNextIcon.imageTintList = ColorStateList.valueOf(ApplicationClass.TEXT_ON_IMAGE_COLOR)

        OverScrollDecoratorHelper.setUpStaticOverScroll(binding.root, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)

        handler.postDelayed(runnable, 1000)
    }

    private fun updatePlaybarUi() {
        binding.playBarPlayPauseIcon.setImageResource(
            if (ApplicationClass.player.isPlaying)  // CORRECCIÓN AQUÍ
                R.drawable.baseline_pause_24
            else
                R.drawable.play_arrow_24px
        )
    }

    override fun onResume() {
        super.onResume()
        NetworkChangeReceiver.registerReceiver(this, networkChangeReceiver)
        showSavedLibrariesData()
    }

    override fun onPause() {
        super.onPause()
        NetworkChangeReceiver.unregisterReceiver(this, networkChangeReceiver)
    }

    override fun onDestroy() {
        ApplicationClass.cancelNotification()
        handler.removeCallbacks(runnable)
        super.onDestroy()
    }

    private fun showData() {
        songs.clear()
        artists.clear()
        albums.clear()
        playlists.clear()

        val apiManager = ApiManager(this)

        // Songs
        apiManager.searchSongs(" ", 0, 15, object : RequestNetwork.RequestListener {
            override fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>) {
                val songSearch = Gson().fromJson(response, SongSearch::class.java)
                Log.i(TAG, "onResponse: $response")

                if (songSearch.success) {
                    songSearch.data.results.forEach { result ->
                        songs.add(
                            AlbumItem(
                                result.name,
                                "${result.language} ${result.year}",
                                result.image.last().url,
                                result.id
                            )
                        )
                    }
                    binding.popularSongsRecyclerView.adapter = ActivityMainPopularSongs(songs)
                    SharedPreferenceManager.setHomeSongsRecommended(songSearch)
                } else {
                    try {
                        showOfflineData()
                        Toast.makeText(
                            this@MainActivity,
                            JSONObject(response).getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: JSONException) {
                        Log.e(TAG, "onResponse error: ", e)
                    }
                }
            }

            override fun onErrorResponse(tag: String, message: String) {
                showOfflineData()
            }
        })

        // Artists
        apiManager.searchArtists(" ", 0, 15, object : RequestNetwork.RequestListener {
            override fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>) {
                val artistSearch = Gson().fromJson(response, ArtistsSearch::class.java)
                Log.i(TAG, "onResponse: $response")

                if (artistSearch.success) {
                    artistSearch.data.results.forEach { result ->
                        artists.add(result)
                    }
                    binding.popularArtistsRecyclerView.adapter = ActivityMainArtistsItemAdapter(artists)
                    SharedPreferenceManager.setHomeArtistsRecommended(artistSearch)
                } else {
                    try {
                        showOfflineData()
                        Toast.makeText(
                            this@MainActivity,
                            JSONObject(response).getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: JSONException) {
                        Log.e(TAG, "onResponse error: ", e)
                    }
                }
            }

            override fun onErrorResponse(tag: String, message: String) {
                showOfflineData()
            }
        })

        // Albums
        apiManager.searchAlbums(" ", 0, 15, object : RequestNetwork.RequestListener {
            override fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>) {
                val albumsSearch = Gson().fromJson(response, AlbumsSearch::class.java)
                Log.i(TAG, "onResponse: $response")

                if (albumsSearch.success) {
                    albumsSearch.data.results.forEach { result ->
                        albums.add(
                            AlbumItem(
                                result.name,
                                "${result.language} ${result.year}",
                                result.image.last().url,
                                result.id
                            )
                        )
                    }
                    binding.popularAlbumsRecyclerView.adapter = ActivityMainAlbumItemAdapter(albums)
                    SharedPreferenceManager.setHomeAlbumsRecommended(albumsSearch)
                } else {
                    try {
                        Toast.makeText(
                            this@MainActivity,
                            JSONObject(response).getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                        showOfflineData()
                    } catch (e: JSONException) {
                        Log.e(TAG, "onResponse error: ", e)
                    }
                }
            }

            override fun onErrorResponse(tag: String, message: String) {
                showOfflineData()
            }
        })

        // Playlists
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        apiManager.searchPlaylists(currentYear, null, null, object : RequestNetwork.RequestListener {
            override fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>) {
                val playlistsSearch = Gson().fromJson(response, PlaylistsSearch::class.java)
                Log.i(TAG, "onResponse: $response")

                if (playlistsSearch.success) {
                    playlistsSearch.data.results.forEach { result ->
                        playlists.add(
                            AlbumItem(
                                result.name,
                                "",
                                result.image.last().url,
                                result.id
                            )
                        )
                    }
                    binding.playlistRecyclerView.adapter = ActivityMainPlaylistAdapter(playlists)
                    SharedPreferenceManager.setHomePlaylistRecommended(playlistsSearch)
                } else {
                    try {
                        Toast.makeText(
                            this@MainActivity,
                            JSONObject(response).getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                        showOfflineData()
                    } catch (e: JSONException) {
                        Log.e(TAG, "onResponse error: ", e)
                    }
                }
            }

            override fun onErrorResponse(tag: String, message: String) {
                showOfflineData()
            }
        })
    }

    private fun showShimmerData() {
        val dataShimmer = mutableListOf<AlbumItem>()
        val artistsShimmer = mutableListOf<ArtistsSearch.Data.Results>()

        repeat(11) {
            dataShimmer.add(AlbumItem("<shimmer>", "<shimmer>", "<shimmer>", "<shimmer>"))
            artistsShimmer.add(
                ArtistsSearch.Data.Results(
                    "<shimmer>",
                    "<shimmer>",
                    "<shimmer>",
                    "<shimmer>",
                    "<shimmer>",
                    emptyList()
                )
            )
        }

        binding.popularSongsRecyclerView.adapter = ActivityMainAlbumItemAdapter(dataShimmer)
        binding.popularAlbumsRecyclerView.adapter = ActivityMainAlbumItemAdapter(dataShimmer)
        binding.popularArtistsRecyclerView.adapter = ActivityMainArtistsItemAdapter(artistsShimmer)
        binding.playlistRecyclerView.adapter = ActivityMainPlaylistAdapter(dataShimmer)
    }

    private fun tryConnect() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            try {
                Thread.sleep(2000)
            } catch (e: Exception) {
                Log.e(TAG, "onErrorResponse: ", e)
            }
        }
    }




    private fun showOfflineData() {
        SharedPreferenceManager.getHomeSongsRecommended()?.let { songSearch ->  // CORRECCIÓN
            songSearch.data.results.forEach { result ->
                songs.add(
                    AlbumItem(
                        result.name,
                        "${result.language} ${result.year}",
                        result.image.last().url,
                        result.id
                    )
                )
            }
            binding.popularSongsRecyclerView.adapter = ActivityMainPopularSongs(songs)
        }

        SharedPreferenceManager.getHomeArtistsRecommended()?.let { artistsSearch ->  // CORRECCIÓN
            artistsSearch.data.results.forEach { result ->
                artists.add(result)
            }
            binding.popularArtistsRecyclerView.adapter = ActivityMainArtistsItemAdapter(artists)
        }

        SharedPreferenceManager.getHomeAlbumsRecommended()?.let { albumsSearch ->  // CORRECCIÓN
            albumsSearch.data.results.forEach { result ->
                albums.add(
                    AlbumItem(
                        result.name,
                        "${result.language} ${result.year}",
                        result.image.last().url,
                        result.id
                    )
                )
            }
            binding.popularAlbumsRecyclerView.adapter = ActivityMainAlbumItemAdapter(albums)
        }

        SharedPreferenceManager.getHomePlaylistRecommended()?.let { playlistsSearch ->  // CORRECCIÓN
            playlistsSearch.data.results.forEach { result ->
                playlists.add(
                    AlbumItem(
                        result.name,
                        "",
                        result.image.last().url,
                        result.id
                    )
                )
            }
            binding.playlistRecyclerView.adapter = ActivityMainPlaylistAdapter(playlists)
        }
    }

    private fun playBarPopUpAnimation() {
        showPopup()
    }

    private fun showPopup() {
        binding.playBarBackground.visibility = View.VISIBLE

        val slideUp = TranslateAnimation(0f, 0f, 1000f, 0f).apply {
            duration = 500
            fillAfter = true
        }

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 500
        }

        slideUp.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {
                binding.playBarBackground.startAnimation(fadeIn)
            }

            override fun onAnimationEnd(animation: android.view.animation.Animation?) {}

            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })

        binding.playBarBackground.startAnimation(slideUp)
    }

    fun closePopup() {
        val fadeOut = AlphaAnimation(1f, 0f).apply {
            duration = 500
            fillAfter = true
        }

        fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}

            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                binding.playBarBackground.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })

        binding.playBarBackground.startAnimation(fadeOut)
    }

    @Suppress("UNUSED_PARAMETER")
    fun openSearch(view: View) {
        startActivity(Intent(this, SearchActivity::class.java))
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}