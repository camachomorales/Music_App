package com.example.music.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.adapters.ActivityDownloadManagerListAdapter
import com.example.music.databinding.ActivityDownloadManagerBinding
import com.example.music.utils.TrackDownloader


class DownloadManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = ActivityDownloadManagerListAdapter(TrackDownloader.getDownloadedTracks(this))
    }

    fun backPress(view: View) {
        finish()
    }
}

