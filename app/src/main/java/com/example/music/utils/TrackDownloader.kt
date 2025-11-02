package com.example.music.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import com.example.music.records.SongResponse
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import org.jaudiotagger.tag.mp4.field.Mp4TagReverseDnsField
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object TrackDownloader {

    private const val TAG = "TrackDownloader"

    interface TrackDownloadListener {
        fun onStarted()
        fun onFinished()
        fun onError(errorMessage: String)
    }

    fun isAlreadyDownloaded(title: String): Boolean {
        val musicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "Melotune"
        )

        if (!musicDir.exists()) {
            return false
        }

        val songFile = File(musicDir, "$title.m4a")
        val songFile1 = File(musicDir, "$title.mp4")
        val songFile2 = File(musicDir, "$title.mp3")

        return songFile.exists() || songFile1.exists() || songFile2.exists()
    }

    data class DownloadedTrack(
        val file: File,
        val title: String,
        val artist: String,
        val album: String,
        val year: String,
        val bitrate: String,
        val trackLength: String,
        val coverImage: Bitmap?,
        val trackUID: String?
    )

    fun getDownloadedTracks(context: Context): List<DownloadedTrack> {
        val data = mutableListOf<DownloadedTrack>()
        val musicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "Melotune"
        )

        if (!musicDir.exists()) {
            return data
        }

        val files = musicDir.listFiles() ?: return data

        for (file in files) {
            try {
                val audioFile = AudioFileIO.read(file)
                val tag = audioFile.tag
                val audioHeader = audioFile.audioHeader

                val uidFieldIdPrefix = "----:${context.packageName}:"
                val uidFieldId = "${uidFieldIdPrefix}TrackUID"

                val title = tag?.getFirst(FieldKey.TITLE) ?: file.name
                val artist = tag?.getFirst(FieldKey.ARTIST) ?: ""
                val album = tag?.getFirst(FieldKey.ALBUM) ?: ""
                val year = tag?.getFirst(FieldKey.YEAR) ?: ""
                val bitrate = audioHeader?.bitRateAsNumber?.toString() ?: "344"
                val trackLength = audioHeader?.trackLength?.toString() ?: "0"

                var trackUID: String? = null
                if (tag != null) {
                    try {
                        val field = tag.getFirst(uidFieldId)
                        if (field != null && field.isNotEmpty()) {
                            trackUID = field
                        } else {
                            // Buscar en todos los campos
                            tag.fields?.forEach { fieldItem ->
                                val id = fieldItem.id
                                if (id != null && id.equals(uidFieldId, ignoreCase = true)) {
                                    trackUID = fieldItem.toString()
                                    return@forEach
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting trackUID: ${e.message}")
                    }
                }

                val coverImage = tag?.firstArtwork?.binaryData
                val bitmap = coverImage?.let {
                    BitmapFactory.decodeByteArray(it, 0, it.size)
                }

                val downloadedTrack = DownloadedTrack(
                    file, title, artist, album, year, bitrate, trackLength, bitmap, trackUID
                )
                data.add(downloadedTrack)
                println(downloadedTrack)
            } catch (e: Exception) {
                Log.e(TAG, "Error reading file: ${e.message}")
            }
        }
        return data
    }

    fun downloadAndEmbedMetadata(
        context: Context,
        song: SongResponse.Song,
        listener: TrackDownloadListener
    ) {
        val downloadUrls = song.downloadUrl
        val images = song.image
        val audioUrl = downloadUrls[downloadUrls.size - 1].url
        val imageUrl = images[images.size - 1].url
        val title = song.name
        val artist = song.artists.primary[0].name
        val album = song.album.name

        Thread {
            Handler(Looper.getMainLooper()).post { listener.onStarted() }
            Log.d(TAG, "⬇️ Downloading and embedding metadata...")

            try {
                val tempFile = File(context.cacheDir, "$title.mp4")

                // Download audio file
                URL(audioUrl).openStream().use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val audioFile = AudioFileIO.read(tempFile)
                val tag = audioFile.tagOrCreateAndSetDefault

                tag.setField(FieldKey.TITLE, title)
                tag.setField(FieldKey.ARTIST, artist)
                tag.setField(FieldKey.ALBUM, album)
                tag.setField(FieldKey.YEAR, song.year)

                val uidField = Mp4TagReverseDnsField(
                    "----",
                    context.packageName,
                    "TrackUID",
                    song.id
                )
                tag.setField(uidField)

                // Download artwork
                val artworkFile = File(context.cacheDir, "artwork.jpg")
                URL(imageUrl).openStream().use { input ->
                    FileOutputStream(artworkFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val artwork = ArtworkFactory.createArtworkFromFile(artworkFile)
                tag.deleteArtworkField()
                tag.setField(artwork)

                audioFile.tag = tag
                audioFile.commit()

                val values = getContentValues(title, artist, album, song.id)

                val resolver = context.contentResolver
                val audioCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val newUri = resolver.insert(audioCollection, values)
                if (newUri == null) {
                    Log.e(TAG, "Failed to insert into MediaStore")
                    Handler(Looper.getMainLooper()).post {
                        listener.onError("Failed to insert into MediaStore")
                    }
                    return@Thread
                }

                URL("file://${tempFile.absolutePath}").openStream().use { input ->
                    resolver.openOutputStream(newUri)?.use { output ->
                        input.copyTo(output)
                        output.flush()
                    }
                }

                if (!tempFile.delete()) {
                    Log.e(TAG, "Failed to delete temp file")
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri))
                }

                Log.d(TAG, "✅ Downloaded and tagged: $title")

            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
                Handler(Looper.getMainLooper()).post {
                    listener.onError(e.message ?: "Unknown error")
                }
            } finally {
                Handler(Looper.getMainLooper()).post { listener.onFinished() }
            }
        }.start()
    }

    private fun getContentValues(title: String, artist: String, album: String, id: String): ContentValues {
        return ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, title)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
            put(MediaStore.Audio.Media.IS_MUSIC, true)
            put(MediaStore.Audio.Media.TITLE, title)
            put(MediaStore.Audio.Media.ARTIST, artist)
            put(MediaStore.Audio.Media.ALBUM, album)
            put(MediaStore.Audio.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/Melotune")
            put(MediaStore.Audio.Media._ID, id)
        }
    }
}