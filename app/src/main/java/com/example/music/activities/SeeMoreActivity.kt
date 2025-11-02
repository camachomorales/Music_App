package com.example.music.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.adapters.ActivitySeeMoreAlbumListAdapter
import com.example.music.adapters.ActivitySeeMoreListAdapter
import com.example.music.databinding.ActivitySeeMoreBinding
import com.example.music.network.ApiManager
import com.example.music.network.utility.RequestNetwork
import com.example.music.records.ArtistAllAlbum
import com.example.music.records.ArtistAllSongs
import com.google.gson.Gson
import com.paginate.Paginate

class SeeMoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeeMoreBinding
    private var totalItems = 0
    private var currentPage = 0
    private var isLoading = false

    private var artistId = ""
    private var mode = ActivitySeeMoreListAdapter.Mode.TOP_SONGS
    private val activitySeeMoreListAdapter = ActivitySeeMoreListAdapter()
    private val activitySeeMoreAlbumListAdapter = ActivitySeeMoreAlbumListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeeMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = activitySeeMoreListAdapter

        val callbacks = object : Paginate.Callbacks {
            override fun onLoadMore() {
                requestDataNext()
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            override fun hasLoadedAllItems(): Boolean {
                return currentPage == totalItems / 10
            }
        }

        Paginate.with(binding.recyclerView, callbacks)
            .setLoadingTriggerThreshold(2)
            .addLoadingListItem(true)
            .build()

        showData()
    }

    private fun showData() {
        val extras = intent.extras
        if (extras == null) {
            finish()
            return
        }

        binding.toolbarText.text = extras.getString("artist_name")
        artistId = extras.getString("id") ?: ""
        val type = extras.getString("type", ActivitySeeMoreListAdapter.Mode.TOP_SONGS.name)
        mode = ActivitySeeMoreListAdapter.Mode.valueOf(type)

        binding.recyclerView.adapter = if (mode == ActivitySeeMoreListAdapter.Mode.TOP_SONGS) {
            activitySeeMoreListAdapter
        } else {
            activitySeeMoreAlbumListAdapter
        }

        requestDataFirst()
    }

    private fun requestDataFirst() {
        val apiManager = ApiManager(this)

        if (mode == ActivitySeeMoreListAdapter.Mode.TOP_SONGS) {
            apiManager.retrieveArtistSongs(
                id = artistId,
                page = 0,
                sortBy = null,
                sortOrder = null,
                listener = object : RequestNetwork.RequestListener {
                    override fun onResponse(
                        tag: String,
                        response: String,
                        responseHeaders: HashMap<String, Any>
                    ) {
                        val artistAllSongs = Gson().fromJson(response, ArtistAllSongs::class.java)
                        if (!artistAllSongs.success) {
                            finish()
                            return
                        }
                        isLoading = false
                        currentPage = 0
                        totalItems = artistAllSongs.data.total
                        activitySeeMoreListAdapter.addAll(artistAllSongs.data.songs)
                    }

                    override fun onErrorResponse(tag: String, message: String) {
                        isLoading = false
                        Toast.makeText(this@SeeMoreActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } else {
            apiManager.retrieveArtistAlbums(
                id = artistId,
                page = 0,
                listener = object : RequestNetwork.RequestListener {
                    override fun onResponse(
                        tag: String,
                        response: String,
                        responseHeaders: HashMap<String, Any>
                    ) {
                        val artistAllAlbum = Gson().fromJson(response, ArtistAllAlbum::class.java)
                        if (!artistAllAlbum.success) {
                            finish()
                            return
                        }
                        isLoading = false
                        currentPage = 0
                        totalItems = artistAllAlbum.data.total
                        activitySeeMoreAlbumListAdapter.addAll(artistAllAlbum.data.albums)
                    }

                    override fun onErrorResponse(tag: String, message: String) {
                        isLoading = false
                        Toast.makeText(this@SeeMoreActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    private fun requestDataNext() {
        currentPage++
        val apiManager = ApiManager(this)

        if (mode == ActivitySeeMoreListAdapter.Mode.TOP_SONGS) {
            apiManager.retrieveArtistSongs(
                id = artistId,
                page = currentPage,
                sortBy = null,
                sortOrder = null,
                listener = object : RequestNetwork.RequestListener {
                    override fun onResponse(
                        tag: String,
                        response: String,
                        responseHeaders: HashMap<String, Any>
                    ) {
                        val artistAllSongs = Gson().fromJson(response, ArtistAllSongs::class.java)
                        if (!artistAllSongs.success) {
                            finish()
                            return
                        }
                        isLoading = false
                        activitySeeMoreListAdapter.addAll(artistAllSongs.data.songs)
                    }

                    override fun onErrorResponse(tag: String, message: String) {
                        isLoading = false
                        Toast.makeText(this@SeeMoreActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } else {
            apiManager.retrieveArtistAlbums(
                id = artistId,
                page = currentPage,
                listener = object : RequestNetwork.RequestListener {
                    override fun onResponse(
                        tag: String,
                        response: String,
                        responseHeaders: HashMap<String, Any>
                    ) {
                        val artistAllAlbum = Gson().fromJson(response, ArtistAllAlbum::class.java)
                        if (!artistAllAlbum.success) {
                            finish()
                            return
                        }
                        isLoading = false
                        activitySeeMoreAlbumListAdapter.addAll(artistAllAlbum.data.albums)
                    }

                    override fun onErrorResponse(tag: String, message: String) {
                        isLoading = false
                        Toast.makeText(this@SeeMoreActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun backPress(view: View) {
        finish()
    }
}