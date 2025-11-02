package com.example.music.api
/*
package com.example.music.api.model

data class Album(
    val idAlbum: String,
    val strAlbum: String,
    val strArtist: String,
    val strAlbumThumb: String? = null
)

package com.example.music.api.model

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun AlbumItem1(
    album: Album,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        album.strAlbumThumb?.let { thumbUrl ->
            Image(
                painter = rememberAsyncImagePainter(thumbUrl),
                contentDescription = album.strAlbum,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = album.strAlbum, style = MaterialTheme.typography.titleMedium)
            Text(text = album.strArtist, style = MaterialTheme.typography.bodyMedium)
        }
    }
}


package com.example.music.api.model

data class Artist(
    val idArtist: String,
    val strArtist: String,
    val strArtistThumb: String? = null
)

package com.example.music.api.model

data class SearchResponse(
    val results: Results
)

data class Results(
    val song: List<Song> = emptyList(),
    val album: List<Album> = emptyList(),
    val artist: List<Artist> = emptyList()
)


package com.example.music.api.model

data class Song(
    val idTrack: String,
    val strTrack: String,
    val strArtist: String,
    val strTrackThumb: String? = null
)


package com.example.music.api.Network

import com.example.music.api.model.Album
import com.example.music.api.model.SearchResponse
import com.example.music.api.model.Song
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("search")
    suspend fun searchMusic(
        @Query("term") term: String,
        @Query("limit") limit: Int = 25
    ): SearchResponse

    @GET("artist/albums")
    suspend fun getArtistAlbums(
        @Query("artistName") artistName: String
    ): List<Album>

    @GET("album/tracks")
    suspend fun getAlbumTracks(
        @Query("albumId") albumId: String
    ): List<Song>
}


package com.example.music.api.Network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://saavnapi.kmplayer.in/") // URL pública real para JioSaavn API no oficial
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
*/