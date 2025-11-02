package com.example.music.adapters

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.activities.MusicOverviewActivity
import com.example.music.databinding.DownloadManagerMoreViewBinding
import com.example.music.utils.TrackDownloader
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog

class ActivityDownloadManagerListAdapter(
    private val data: List<TrackDownloader.DownloadedTrack>
) : RecyclerView.Adapter<ActivityDownloadManagerListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (viewType == 0) {
            R.layout.download_manager_list_item
        } else {
            R.layout.activity_list_shimmer
        }

        val view = View.inflate(parent.context, layoutId, null).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == 1) {
            holder.itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer)?.startShimmer()
            return
        }

        holder.itemView.findViewById<View>(R.id.title)?.isSelected = true
        holder.itemView.findViewById<View>(R.id.artist)?.isSelected = true

        val item = data[position]

        holder.itemView.findViewById<TextView>(R.id.title)?.text = item.title
        holder.itemView.findViewById<TextView>(R.id.artist)?.text = item.artist

        item.coverImage?.let { bitmap ->
            holder.itemView.findViewById<ImageView>(R.id.coverImage)?.setImageBitmap(bitmap)
        }

        holder.itemView.setOnClickListener { view ->
            showDialog(item, view)
        }
    }

    private fun showDialog(track: TrackDownloader.DownloadedTrack, view: View) {
        val bottomSheetDialog = BottomSheetDialog(view.context, R.style.MyBottomSheetDialogTheme)
        val binding = DownloadManagerMoreViewBinding.inflate((view.context as Activity).layoutInflater)

        binding.songTitle.text = track.title
        binding.songSubTitle.text = track.artist
        track.coverImage?.let { binding.coverImage.setImageBitmap(it) }
        binding.albumTitle.text = track.album
        binding.songYear.text = track.year
        binding.bitrate.text = "${track.bitrate} kbps"
        binding.duration.text = "${track.trackLength} Seconds"

        if (track.trackUID.isNullOrEmpty()) {
            binding.button.visibility = View.GONE
        }

        binding.button.setOnClickListener {
            it.context.startActivity(
                Intent(it.context, MusicOverviewActivity::class.java).apply {
                    putExtra("type", "clear")
                    putExtra("id", track.trackUID)
                }
            )
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.show()
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return if (data[position].title == "<shimmer>") 1 else 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}