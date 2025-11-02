package com.example.music.adapters

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.activities.MusicOverviewActivity
import com.example.music.api.model.AlbumItem
import com.facebook.shimmer.ShimmerFrameLayout
import com.squareup.picasso.Picasso

class ActivityMainPopularSongs(
    private val data: List<AlbumItem>
) : RecyclerView.Adapter<ActivityMainPopularSongs.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (viewType == 0) {
            R.layout.activity_main_songs_item
        } else {
            R.layout.songs_item_shimmer
        }

        val view = View.inflate(parent.context, layoutId, null).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
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

        val item = data[position]

        holder.itemView.findViewById<TextView>(R.id.albumTitle)?.apply {
            text = item.albumTitle
            isSelected = true
        }

        holder.itemView.findViewById<TextView>(R.id.albumSubTitle)?.apply {
            text = item.albumSubTitle
            isSelected = true
        }

        holder.itemView.findViewById<ImageView>(R.id.coverImage)?.let { coverImage ->
            Picasso.get().load(Uri.parse(item.albumCover)).into(coverImage)
        }

        holder.itemView.setOnClickListener { v ->
            v.context.startActivity(
                Intent(v.context, MusicOverviewActivity::class.java).apply {
                    putExtra("type", "clear")
                    putExtra("id", item.id)
                }
            )
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return if (data[position].albumTitle == "<shimmer>") 1 else 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}