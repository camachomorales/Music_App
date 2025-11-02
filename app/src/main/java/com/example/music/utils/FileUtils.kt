package com.example.music.utils

import android.util.Log
import java.io.File
import java.io.IOException

object FileUtils {

    fun readFileToByteArray(file: File): ByteArray? {
        return try {
            file.readBytes()
        } catch (e: IOException) {
            Log.e("FileUtils", "Error reading file to byte array", e)
            null
        }
    }
}
