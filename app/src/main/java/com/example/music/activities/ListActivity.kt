package com.example.music.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.example.music.ApplicationClass
import com.example.music.R
import com.example.music.adapters.ActivityListSongsItemAdapter
import com.example.music.adapters.UserCreatedSongsListAdapter
import com.example.music.databinding.ActivityListBinding
import com.example.music.databinding.ActivityListMoreInfoBottomSheetBinding
import com.example.music.databinding.UserCreatedListActivityMoreBottomSheetBinding
import com.example.music.api.model.AlbumItem
import com.example.music.api.model.BasicDataRecord
import com.example.music.network.ApiManager
import com.example.music.network.utility.RequestNetwork
import com.example.music.records.AlbumSearch
import com.example.music.records.PlaylistSearch
import com.example.music.records.SongResponse
import com.example.music.records.sharedpref.SavedLibraries
import com.example.music.utils.SharedPreferenceManager
import com.example.music.utils.customview.BottomSheetItemView
import com.squareup.picasso.Picasso
import com.google.gson.Gson
import android.annotation.SuppressLint

class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding
    private val trackQueue = mutableListOf<String>()
    private var albumItem: AlbumItem? = null
    private var isAlbum = false
    private var isUserCreated = false
    private val artistData = mutableListOf<ArtistData>()

    companion object {
        private const val TAG = "ListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.addMoreSongs.visibility = View.GONE

        Log.i(TAG, "onCreate: reached ListActivity")

        showShimmerData()

        binding.playAllBtn.setOnClickListener {
            if (trackQueue.isNotEmpty()) {
                (applicationContext as ApplicationClass).setTrackQueue(trackQueue)
                (applicationContext as ApplicationClass).nextTrack()
                Log.i(TAG, "trackQueueSet: ${ApplicationClass.trackQueue} With POS ${ApplicationClass.track_position}")
                startActivity(Intent(this, MusicOverviewActivity::class.java).apply {
                    putExtra("id", trackQueue[0])
                })
            }
        }

        binding.addToLibrary.setOnClickListener {
            albumItem?.let { item ->
                val savedLibraries = SharedPreferenceManager.getSavedLibrariesData()

                if (isAlbumInLibrary(item, savedLibraries)) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Are you sure?")
                        .setMessage("Do you want to remove this album from your library?")
                        .setPositiveButton("Yes") { _, _ ->
                            val index = getAlbumIndexInLibrary(item, savedLibraries)
                            if (index != -1) {
                                SharedPreferenceManager.removeLibraryFromSavedLibraries(index)
                                Snackbar.make(binding.root, "Removed from Library", Snackbar.LENGTH_SHORT).show()
                                updateAlbumInLibraryStatus()
                                finish()
                            }
                        }
                        .setNegativeButton("No", null)
                        .show()
                } else {
                    val library = SavedLibraries.Library(
                        id = item.id,
                        isCreatedByUser = false,
                        isAlbum = isAlbum,
                        name = binding.albumTitle.text.toString(),
                        image = item.albumCover, // Fixed: was item.image
                        description = binding.albumSubTitle.text.toString(),
                        songs = mutableListOf()
                    )
                    SharedPreferenceManager.addLibraryToSavedLibraries(library)
                    Snackbar.make(binding.root, "Added to Library", Snackbar.LENGTH_SHORT).show()
                }

                updateAlbumInLibraryStatus()
            }
        }

        binding.addMoreSongs.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.moreIcon.setOnClickListener {
            onMoreIconClicked()
        }

        showData()
    }

    override fun onResume() {
        super.onResume()
        if (intent.extras?.getBoolean("createdByUser", false) == true) {
            onUserCreatedFetch()
        }
    }

    private fun onMoreIconClicked() {
        albumItem ?: return

        if (isUserCreated) {
            onMoreIconClickedUserCreated()
            return
        }

        val bottomSheetDialog = BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme)
        val sheetBinding = ActivityListMoreInfoBottomSheetBinding.inflate(layoutInflater)

        sheetBinding.albumTitle.text = binding.albumTitle.text.toString()
        sheetBinding.albumSubTitle.text = binding.albumSubTitle.text.toString()
        albumItem?.let {
            Picasso.get().load(it.albumCover).into(sheetBinding.coverImage) // Fixed: was it.image
        }

        val savedLibraries = SharedPreferenceManager.getSavedLibrariesData()

        // Set text for addToLibrary button/view
        try {
            val addToLibraryText = if (savedLibraries == null || savedLibraries.lists.isEmpty()) {
                "Add to library"
            } else {
                albumItem?.let { item ->
                    if (isAlbumInLibrary(item, savedLibraries)) {
                        "Remove from library"
                    } else {
                        "Add to library"
                    }
                } ?: "Add to library"
            }

            // Access the title TextView directly from BottomSheetItemView
            sheetBinding.addToLibrary.getTitleTextView().text = addToLibraryText
        } catch (e: Exception) {
            Log.e(TAG, "Error setting addToLibrary text: ", e)
        }

        sheetBinding.addToLibrary.setOnClickListener {
            bottomSheetDialog.dismiss()
            binding.addToLibrary.performClick()
        }

        artistData.forEach { artist ->
            try {
                val imgUrl = if (artist.image.isEmpty()) "" else artist.image
                val bottomSheetItemView = BottomSheetItemView(
                    this,
                    artist.name,
                    imgUrl,
                    artist.id
                )
                bottomSheetItemView.isFocusable = true
                bottomSheetItemView.isClickable = true
                bottomSheetItemView.setOnClickListener {
                    Log.i(TAG, "BottomSheetItemView: onClick!")
                    startActivity(Intent(this, ArtistProfileActivity::class.java).apply {
                        putExtra("data", Gson().toJson(
                            BasicDataRecord(artist.id, artist.name, "", imgUrl)
                        ))
                    })
                }
                sheetBinding.main.addView(bottomSheetItemView)
            } catch (e: Exception) {
                Log.e(TAG, "BottomSheetDialog: ", e)
            }
        }

        bottomSheetDialog.setContentView(sheetBinding.root)
        bottomSheetDialog.create()
        bottomSheetDialog.show()
    }

    private fun onMoreIconClickedUserCreated() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme)
        val sheetBinding = UserCreatedListActivityMoreBottomSheetBinding.inflate(layoutInflater)

        sheetBinding.albumTitle.text = binding.albumTitle.text.toString()
        sheetBinding.albumSubTitle.text = binding.albumSubTitle.text.toString()
        albumItem?.let {
            Picasso.get().load(it.albumCover).into(sheetBinding.coverImage) // Fixed: was it.image
        }

        sheetBinding.removeLibrary.setOnClickListener {
            bottomSheetDialog.dismiss()
            binding.addToLibrary.performClick()
        }

        bottomSheetDialog.setContentView(sheetBinding.root)
        bottomSheetDialog.create()
        bottomSheetDialog.show()
    }

    private fun updateAlbumInLibraryStatus() {
        val savedLibraries = SharedPreferenceManager.getSavedLibrariesData()

        if (savedLibraries == null) {
            binding.addToLibrary.setImageResource(R.drawable.round_add_24)
        } else {
            albumItem?.let { item ->
                binding.addToLibrary.setImageResource(
                    if (isAlbumInLibrary(item, savedLibraries))
                        R.drawable.round_done_24
                    else
                        R.drawable.round_add_24
                )
            }
        }
    }

    @SuppressLint("NewApi")
    private fun isAlbumInLibrary(albumItem: AlbumItem, savedLibraries: SavedLibraries?): Boolean {
        if (savedLibraries == null || savedLibraries.lists.isEmpty()) {
            return false
        }
        Log.i(TAG, "isAlbumInLibrary: $savedLibraries")
        return savedLibraries.lists.any { it.id == albumItem.id }
    }

    @SuppressLint("NewApi")
    private fun getAlbumIndexInLibrary(albumItem: AlbumItem, savedLibraries: SavedLibraries?): Int {
        if (savedLibraries == null || savedLibraries.lists.isEmpty()) {
            return -1
        }
        Log.i(TAG, "getAlbumIndexInLibrary: $savedLibraries")
        return savedLibraries.lists.indexOfFirst { it.id == albumItem.id }
    }

    private fun showShimmerData() {
        val data = List(11) {
            SongResponse.Song(
                id = "<shimmer>",
                rawName = "",
                type = "",
                year = "",
                releaseDate = "",
                duration = 0.0,
                label = "",
                explicitContent = false,
                playCount = 0,
                language = "",
                hasLyrics = false,
                lyricsId = "",
                url = "",
                copyright = "",
                album = SongResponse.Album("", "", ""),
                artists = SongResponse.Artists(emptyList(), emptyList(), emptyList()),
                image = emptyList(),
                downloadUrl = emptyList(),
                lyrics = SongResponse.Lyrics(
                    lyrics = "",
                    copyright = "",
                    snippet = ""
                )
            )
        }
        binding.recyclerView.adapter = ActivityListSongsItemAdapter(data)
    }

    private fun showData() {
        intent.extras ?: return

        albumItem = Gson().fromJson(
            intent.extras?.getString("data"),
            AlbumItem::class.java
        )

        updateAlbumInLibraryStatus()

        albumItem?.let { item ->
            binding.albumTitle.text = item.albumTitle // Fixed: was item.name
            binding.albumSubTitle.text = item.albumSubTitle // Fixed: was item.subtitle
            if (item.albumCover.isNotBlank()) { // Fixed: was item.image
                Picasso.get().load(item.albumCover).into(binding.albumCover)
            }
        }

        val apiManager = ApiManager(this)
        val intentId = intent.extras?.getString("id", "") ?: ""

        if (intent.extras?.getBoolean("createdByUser", false) == true) {
            onUserCreatedFetch()
            return
        }

        val checkIfUrlData = (intentId.startsWith("http") || intentId.startsWith("www"))
                && intentId.contains("jiosaavn.com")

        if (intent.extras?.getString("type", "") == "album") {
            isAlbum = true
            albumItem?.let { item ->
                SharedPreferenceManager.getAlbumResponseById(item.id)?.let { cachedAlbum ->
                    onAlbumFetched(cachedAlbum)
                    return
                }
            }

            val requestListener = object : RequestNetwork.RequestListener {
                override fun onResponse(
                    tag: String,
                    response: String,
                    responseHeaders: HashMap<String, Any>
                ) {
                    val albumSearch = Gson().fromJson(response, AlbumSearch::class.java)
                    Log.i(TAG, "onResponse: $albumSearch")
                    if (albumSearch.success) {
                        SharedPreferenceManager.setAlbumResponseById(albumSearch.data.id, albumSearch)
                        onAlbumFetched(albumSearch)
                    }
                }

                override fun onErrorResponse(tag: String, message: String) {
                    Log.e(TAG, "onErrorResponse: $message")
                    Toast.makeText(this@ListActivity, "Failed to fetch Album", Toast.LENGTH_SHORT).show()
                }
            }

            if (checkIfUrlData) {
                apiManager.retrieveAlbumByLink(intentId, requestListener)
            } else {
                albumItem?.let {
                    apiManager.retrieveAlbumById(it.id, requestListener)
                }
            }
            return
        }

        val responseListener = object : RequestNetwork.RequestListener {
            override fun onResponse(
                tag: String,
                response: String,
                responseHeaders: HashMap<String, Any>
            ) {
                Log.i(TAG, "onResponse: $response")
                val playlistSearch = Gson().fromJson(response, PlaylistSearch::class.java)
                if (playlistSearch.success) {
                    SharedPreferenceManager.setPlaylistResponseById(playlistSearch.data.id, playlistSearch)
                    onPlaylistFetched(playlistSearch)
                }
            }

            override fun onErrorResponse(tag: String, message: String) {
                Log.e(TAG, "onErrorResponse: $message")
            }
        }

        if (checkIfUrlData) {
            apiManager.retrievePlaylistByLink(intentId, null, null, responseListener)
        } else {
            albumItem?.let { item ->
                SharedPreferenceManager.getPlaylistResponseById(item.id)?.let { cachedPlaylist ->
                    onPlaylistFetched(cachedPlaylist)
                }
                apiManager.retrievePlaylistById(item.id, null, null, responseListener)
            }
        }
    }

    private fun onUserCreatedFetch() {
        isUserCreated = true

        binding.shareIcon.visibility = View.INVISIBLE
        binding.addToLibrary.visibility = View.INVISIBLE
        binding.addMoreSongs.visibility = View.VISIBLE

        val savedLibraries = SharedPreferenceManager.getSavedLibrariesData()

        if (savedLibraries == null || savedLibraries.lists.isEmpty()) {
            finish()
            return
        }

        val library = savedLibraries.lists.find { it.id == albumItem?.id }

        if (library == null) {
            finish()
            return
        }

        binding.albumTitle.text = library.name
        binding.albumSubTitle.text = library.description
        Picasso.get().load(library.image).into(binding.albumCover)
        binding.recyclerView.adapter = UserCreatedSongsListAdapter(library.songs)

        library.songs.forEach { song ->
            trackQueue.add(song.id)
        }
    }

    private fun onAlbumFetched(albumSearch: AlbumSearch) {
        binding.albumTitle.text = albumSearch.data.name
        binding.albumSubTitle.text = albumSearch.data.description ?: ""

        val imageUrl = albumSearch.data.image.lastOrNull()?.url ?: ""
        if (imageUrl.isNotEmpty()) {
            Picasso.get().load(imageUrl).into(binding.albumCover)
        }

        binding.recyclerView.adapter = ActivityListSongsItemAdapter(albumSearch.data.songs)

        albumSearch.data.songs.forEach { song ->
            trackQueue.add(song.id)
        }

        binding.shareIcon.setOnClickListener {
            if (albumSearch.data.url.isNotBlank()) {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, albumSearch.data.url)
                    type = "text/plain"
                }
                startActivity(sendIntent)
            }
        }

        albumSearch.data.artist.primary.forEach { artist ->
            val imageUrl = artist.image.lastOrNull()?.url ?: ""
            artistData.add(ArtistData(artist.name, artist.id, imageUrl))
        }
    }

    private fun onPlaylistFetched(playlistSearch: PlaylistSearch) {
        binding.albumTitle.text = playlistSearch.data.name
        binding.albumSubTitle.text = playlistSearch.data.description ?: ""

        val imageUrl = playlistSearch.data.image.lastOrNull()?.url ?: ""
        if (imageUrl.isNotEmpty()) {
            Picasso.get().load(imageUrl).into(binding.albumCover)
        }

        binding.recyclerView.adapter = ActivityListSongsItemAdapter(playlistSearch.data.songs)

        playlistSearch.data.songs.forEach { song ->
            trackQueue.add(song.id)
        }

        binding.shareIcon.setOnClickListener {
            if (playlistSearch.data.url.isNotBlank()) {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, playlistSearch.data.url)
                    type = "text/plain"
                }
                startActivity(sendIntent)
            }
        }

        playlistSearch.data.artists.forEach { artist ->
            val imageUrl = artist.image.lastOrNull()?.url
                ?: "https://i.pinimg.com/564x/1d/04/a8/1d04a87b8e6cf2c3829c7af2eccf6813.jpg"
            artistData.add(ArtistData(artist.name, artist.id, imageUrl))
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun backPress(view: View) {
        finish()
    }

    data class ArtistData(
        val name: String,
        val id: String,
        val image: String
    )
}