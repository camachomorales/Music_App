package com.example.music.activities

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.example.music.adapters.ActivityArtistProfileTopAlbumsAdapter  // Asume que existe; ajusta si no
import com.example.music.adapters.ActivityArtistProfileTopSongsAdapter  // Asume que existe; ajusta si no
import com.example.music.adapters.ActivitySeeMoreListAdapter  // Asume que existe; ajusta si no
import com.example.music.databinding.ActivityArtistProfileBinding  // Asume que tienes ViewBinding
import com.example.music.api.model.BasicDataRecord
import com.example.music.network.ApiManager
import com.example.music.network.utility.RequestNetwork
import com.example.music.records.AlbumsSearch
import com.example.music.records.ArtistSearch
import com.example.music.records.SongResponse
import com.example.music.utils.SharedPreferenceManager
import com.squareup.picasso.Picasso

/**
 * The `ArtistProfileActivity` class displays the profile information of an artist,
 * including their name, image, top songs, top albums, and singles.
 * It fetches the artist's data from a remote API and handles network connectivity changes.
 *
 * <p>
 * This activity uses a collapsing toolbar layout to provide a visually appealing
 * header that expands and collapses as the user scrolls.
 * It also utilizes Shimmer effect as placeholder while data is loading.
 * </p>
 */
class ArtistProfileActivity : AppCompatActivity() {

    private val TAG = "ArtistProfileActivity"
    private lateinit var binding: ActivityArtistProfileBinding
    private var artistId = "9999"
    private var artistSearch: ArtistSearch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtistProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.collapsingToolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
        }
        // Corrección: Usar setExpandedTitleColor() en lugar de propiedad
        binding.collapsingToolbarLayout.setExpandedTitleColor(resources.getColor(android.R.color.transparent))

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        binding.collapsingToolbarAppbarlayout.addOnOffsetChangedListener { _, verticalOffset ->
            // Puedes agregar lógica aquí si es necesario
        }

        binding.topSongsRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.topAlbumsRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.topSinglesRecyclerview.layoutManager = LinearLayoutManager(this)

        binding.topSongsSeeMore.setOnClickListener {
            startActivity(Intent(this, SeeMoreActivity::class.java).apply {
                putExtra("id", artistId)
                putExtra("type", ActivitySeeMoreListAdapter.Mode.TOP_SONGS.name)
                putExtra("artist_name", binding.artistName.text.toString())
            })
        }
        binding.topAlbumsSeeMore.setOnClickListener {
            startActivity(Intent(this, SeeMoreActivity::class.java).apply {
                putExtra("id", artistId)
                putExtra("type", ActivitySeeMoreListAdapter.Mode.TOP_ALBUMS.name)
                putExtra("artist_name", binding.artistName.text.toString())
            })
        }
        binding.topSinglesSeeMore.visibility = View.GONE
        // binding.topSinglesSeeMore.setOnClickListener { /* Deshabilitado */ }

        showShimmerData()
        showData()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    private fun showData() {
        intent.extras?.let { extras ->
            Log.i(TAG, "showData: $extras")
            val artist = extras.getString("data", "null") ?: return
            val apiManager = ApiManager(this)
            val sharedPreferenceManager = SharedPreferenceManager  // Acceso directo al object singleton

            val responseListener = object : RequestNetwork.RequestListener {
                override fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>) {
                    try {
                        artistSearch = Gson().fromJson(response, ArtistSearch::class.java)
                        Log.i(TAG, "onResponse: $response")
                        artistSearch?.data?.id?.let { id ->
                            sharedPreferenceManager.setArtistData(id, artistSearch!!)
                            Log.i(TAG, "Cached: ${sharedPreferenceManager.getArtistData(id)}")
                        }
                        display()
                    } catch (e: Exception) {
                        Log.e(TAG, "onResponse error", e)
                        finish()
                    }
                }

                override fun onErrorResponse(tag: String, message: String) {
                    Log.i(TAG, "onErrorResponse: $message")
                    if (artistId != "9999") {
                        sharedPreferenceManager.getArtistData(artistId)?.let { offlineData ->
                            artistSearch = offlineData
                            display()
                        }
                    }
                }
            }

            if ((artist.startsWith("http") || artist.startsWith("www")) && artist.contains("jiosaavn.com")) {
                apiManager.retrieveArtistsByLink(artist, null, null, null, null, null, responseListener)
                return
            }

            val artistItem = Gson().fromJson(artist, BasicDataRecord::class.java) ?: return
            artistId = artistItem.id

            Picasso.get().load(Uri.parse(artistItem.image)).into(binding.artistImg)
            binding.artistName.text = artistItem.title
            binding.collapsingToolbarLayout.title = artistItem.title

            apiManager.retrieveArtistById(artistId, null, null, null, null, null, responseListener)
        }
    }

    private fun display() {
        Log.i(TAG, "display: $artistSearch")
        artistSearch?.takeIf { it.success }?.let { search ->
            search.data.image.lastOrNull()?.url?.let { url ->
                Picasso.get().load(Uri.parse(url)).into(binding.artistImg)
            }
            binding.artistName.text = search.data.name
            binding.collapsingToolbarLayout.title = search.data.name
            binding.topSongsRecyclerview.adapter = ActivityArtistProfileTopSongsAdapter(search.data.topSongs ?: emptyList())
            binding.topAlbumsRecyclerview.adapter = ActivityArtistProfileTopAlbumsAdapter(search.data.topAlbums ?: emptyList())
            binding.topSinglesRecyclerview.adapter = ActivityArtistProfileTopAlbumsAdapter(search.data.singles ?: emptyList())
        }
    }

    private fun getShimmerData(): List<SongResponse.Song> {
        return List(11) {
            SongResponse.Song(
                "<shimmer>", "", "", "", "", 0.0, "", false, 0, "", false, "",
                SongResponse.Lyrics("", "", ""), "", "",
                SongResponse.Album("", "", ""),
                SongResponse.Artists(emptyList(), emptyList(), emptyList()),
                emptyList(), emptyList()
            )
        }
    }

    private fun showShimmerData() {
        val shimmerData = getShimmerData()
        // Corrección: Crear Results con valores por defecto (no null), ya que el modelo no permite nulls
        val shimmerDataAlbum = List(11) {
            AlbumsSearch.Data.Results(
                id = "<shimmer>",
                rawName = "<shimmer>",
                rawDescription = null,
                url = "",
                year = 0,
                type = "",
                playCount = 0,
                language = "",
                explicitContent = false,
                artist = AlbumsSearch.Data.Results.Artists(
                    primary = emptyList(),
                    featured = emptyList(),
                    all = emptyList()
                ),
                image = emptyList()  // Lista vacía en lugar de null
            )
        }

        binding.topSongsRecyclerview.adapter = ActivityArtistProfileTopSongsAdapter(shimmerData)
        binding.topAlbumsRecyclerview.adapter = ActivityArtistProfileTopAlbumsAdapter(shimmerDataAlbum)
        binding.topSinglesRecyclerview.adapter = ActivityArtistProfileTopAlbumsAdapter(shimmerDataAlbum)

        SharedPreferenceManager.getArtistData(artistId)?.let { offlineData ->
            artistSearch = offlineData
            display()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
