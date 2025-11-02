package com.example.music.activities

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.R
import com.example.music.adapters.ActivitySearchListItemAdapter
import com.example.music.api.model.SearchListItem  // Import corregido
import com.example.music.databinding.ActivitySearchBinding
import com.example.music.network.ApiManager
import com.example.music.network.utility.RequestNetwork
import com.example.music.records.GlobalSearch
import com.example.music.utils.SharedPreferenceManager
import com.google.gson.Gson
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private var globalSearch: GlobalSearch? = null

    companion object {
        private const val TAG = "SearchActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa SharedPreferenceManager si no lo has hecho en Application
        SharedPreferenceManager.init(this)

        OverScrollDecoratorHelper.setUpOverScroll(binding.hscrollview)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.edittext.requestFocus()

        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            Log.i(TAG, "checkedIds: $checkedIds")
            globalSearch?.let {
                if (it.success) {
                    refreshData()
                }
            }
        }

        binding.edittext.setOnEditorActionListener { textView, _, _ ->
            showData(textView.text.toString())
            Log.i(TAG, "onCreate: ${textView.text}")
            binding.edittext.clearFocus()
            hideKeyboard(binding.edittext)
            true
        }

        binding.edittext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.clearIcon.visibility = if (s.isNullOrEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.clearIcon.setOnClickListener {
            binding.edittext.setText("")
        }
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showData(query: String) {
        showShimmerData()

        val apiManager = ApiManager(this)
        // Usa SharedPreferenceManager directamente (ya inicializado)
        apiManager.globalSearch(query, object : RequestNetwork.RequestListener {
            override fun onResponse(
                tag: String,
                response: String,
                responseHeaders: HashMap<String, Any>
            ) {
                try {
                    val result = Gson().fromJson(response, GlobalSearch::class.java) as GlobalSearch  // Casteo explícito
                    globalSearch = result
                    if (result.success) {
                        SharedPreferenceManager.setSearchResultCache(query, result)
                        refreshData()
                    } else {
                        Toast.makeText(
                            this@SearchActivity,
                            "Oops, There was an error while searching",
                            Toast.LENGTH_SHORT
                        ).show()
                        onFailed()
                    }
                    Log.i(TAG, "onResponse: $response")
                } catch (e: Exception) {
                    Log.e(TAG, "Parse error: ${e.message}")
                    onFailed()
                }
            }

            override fun onErrorResponse(tag: String, message: String) {
                Log.e(TAG, "onErrorResponse: $message")
                Toast.makeText(
                    this@SearchActivity,
                    "Oops, There was an error while searching",
                    Toast.LENGTH_SHORT
                ).show()
                onFailed()
            }

            private fun onFailed() {
                val resultOffline = SharedPreferenceManager.getSearchResult(query)
                if (resultOffline != null) {
                    globalSearch = resultOffline
                    refreshData()
                }
            }
        })
    }

    private fun refreshData() {
        val data = ArrayList<SearchListItem>()  // Ahora se infiere correctamente
        val checkedChipId = binding.chipGroup.checkedChipId

        when (checkedChipId) {
            R.id.chip_all -> {
                globalSearch?.data?.topQuery?.results?.forEach { item ->
                    if (item.type in listOf("song", "album", "playlist", "artist")) {
                        data.add(
                            SearchListItem(
                                item.id,
                                item.title,
                                item.description,
                                item.image.lastOrNull()?.url ?: "",
                                SearchListItem.Type.valueOf(item.type.uppercase())
                            )
                        )
                    }
                }
                addSongsData(data)
                addAlbumsData(data)
                addPlaylistsData(data)
                addArtistsData(data)
            }
            R.id.chip_song -> addSongsData(data)
            R.id.chip_albums -> addAlbumsData(data)
            R.id.chip_playlists -> addPlaylistsData(data)
            R.id.chip_artists -> addArtistsData(data)
            else -> throw IllegalStateException("Unexpected value: ${binding.chipGroup.checkedChipId}")
        }

        if (data.isNotEmpty()) {
            binding.recyclerView.adapter = ActivitySearchListItemAdapter(data)
        }
    }

    private fun addSongsData(data: ArrayList<SearchListItem>) {
        globalSearch?.data?.songs?.results?.forEach { item ->
            data.add(
                SearchListItem(
                    item.id,
                    item.title,
                    item.description,
                    item.image.lastOrNull()?.url ?: "",
                    SearchListItem.Type.SONG
                )
            )
        }
    }

    private fun addAlbumsData(data: ArrayList<SearchListItem>) {
        globalSearch?.data?.albums?.results?.forEach { item ->
            data.add(
                SearchListItem(
                    item.id,
                    item.title,
                    item.description,
                    item.image.lastOrNull()?.url ?: "",
                    SearchListItem.Type.ALBUM
                )
            )
        }
    }

    private fun addPlaylistsData(data: ArrayList<SearchListItem>) {
        globalSearch?.data?.playlists?.results?.forEach { item ->
            data.add(
                SearchListItem(
                    item.id,
                    item.title,
                    item.description,
                    item.image.lastOrNull()?.url ?: "",
                    SearchListItem.Type.PLAYLIST
                )
            )
        }
    }

    private fun addArtistsData(data: ArrayList<SearchListItem>) {
        globalSearch?.data?.artists?.results?.forEach { item ->
            data.add(
                SearchListItem(
                    item.id,
                    item.title,
                    item.description,
                    item.image.lastOrNull()?.url ?: "",
                    SearchListItem.Type.ARTIST
                )
            )
        }
    }

    private fun showShimmerData() {
        val data = ArrayList<SearchListItem>()
        repeat(11) {
            data.add(
                SearchListItem(
                    "<shimmer>",
                    "",
                    "",
                    "",
                    SearchListItem.Type.SONG
                )
            )
        }
        binding.recyclerView.adapter = ActivitySearchListItemAdapter(data)
    }

    @Suppress("UNUSED_PARAMETER")
    fun backPress(view: View) {
        finish()
    }
}