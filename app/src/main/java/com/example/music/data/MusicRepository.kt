
package com.example.music.data

import com.example.music.api.network.RetrofitInstance
import com.example.music.api.model.Album
import com.example.music.api.model.AlbumsResult
import com.example.music.api.model.Song
import com.example.music.api.model.SongsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepository {

    suspend fun getArtistAlbums(artistName: String): List<Album> {
        return withContext(Dispatchers.IO) {
            val albumsResult: AlbumsResult = RetrofitInstance.api.getArtistAlbums(artistName)
            albumsResult.results
        }
    }

    suspend fun getAlbumTracks(albumId: String): List<Song> {
        return withContext(Dispatchers.IO) {
            val songsResult: SongsResult = RetrofitInstance.api.getAlbumTracks(albumId)
            songsResult.results
        }
    }
}
