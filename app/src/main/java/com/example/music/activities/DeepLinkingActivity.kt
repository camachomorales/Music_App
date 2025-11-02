package com.example.music.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.music.R

class DeepLinkingActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DeepLinkingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deep_linking)
        handleIntent(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
        finish()
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val data: Uri? = intent.data
        if (data == null) {
            Log.w(TAG, "No data in intent")
            return
        }

        val host = data.host
        val path = data.path
        Log.d(TAG, "Deep link host: $host path: $path")

        if (path == null) {
            openMainScreen()
            return
        }

        when {
            path.startsWith("/song") -> openPlayerForSongUrl(data.toString())
            path.startsWith("/album") -> openAlbumFromUrl(data.toString())
            path.startsWith("/featured") -> openPlaylistFromUrl(data.toString())
            path.startsWith("/artist") -> openArtistFromUrl(data.toString())
            else -> openMainScreen()
        }
    }

    private fun openPlayerForSongUrl(songUrl: String) {
        val intent = Intent(this, MusicOverviewActivity::class.java).apply {
            putExtra("type", "clear")
            putExtra("id", songUrl)
        }
        startActivity(intent)
    }

    private fun openAlbumFromUrl(albumUrl: String) {
        val intent = Intent(this, ListActivity::class.java).apply {
            putExtra("type", "album")
            putExtra("id", albumUrl)
        }
        startActivity(intent)
    }

    private fun openPlaylistFromUrl(playlistUrl: String) {
        val intent = Intent(this, ListActivity::class.java).apply {
            putExtra("type", "playlist")
            putExtra("id", playlistUrl)
        }
        startActivity(intent)
    }

    private fun openArtistFromUrl(artistUrl: String) {
        val intent = Intent(this, ArtistProfileActivity::class.java).apply {
            putExtra("data", artistUrl)
        }
        startActivity(intent)
    }

    private fun openMainScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
