package com.example.music.adapters

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.activities.MusicOverviewActivity
import com.example.music.databinding.ActivityArtistProfileViewTopSongsItemBinding
import com.example.music.records.SongResponse
import com.facebook.shimmer.ShimmerFrameLayout
import com.squareup.picasso.Picasso

class ActivityArtistProfileTopSongsAdapter(
    private val data: List<SongResponse.Song>
) : RecyclerView.Adapter<ActivityArtistProfileTopSongsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (viewType == 1) {
            R.layout.activity_artist_profile_view_top_songs_item
        } else {
            R.layout.artist_profile_view_top_songs_shimmer
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
        if (getItemViewType(position) == 0) {
            holder.itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer)?.startShimmer()
            return
        }

        val itemView = ActivityArtistProfileViewTopSongsItemBinding.bind(holder.itemView)
        val item = data[position]
        val images = item.image

        itemView.position.text = (position + 1).toString()
        itemView.coverTitle.text = item.name
        itemView.coverPlayed.text = String.format("%s | %s", item.year, item.label)

        Picasso.get()
            .load(Uri.parse(images[images.size - 1].url))
            .into(itemView.coverImage)

        holder.itemView.setOnClickListener {
            it.context.startActivity(
                Intent(it.context, MusicOverviewActivity::class.java).apply {
                    putExtra("id", item.id)
                }
            )
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return if (data[position].id == "<shimmer>") 0 else 1
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}