package com.example.music.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.activities.ListActivity
import com.example.music.api.model.AlbumItem
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import com.squareup.picasso.Picasso

class ActivityMainPlaylistAdapter(
    private val data: List<AlbumItem>
) : RecyclerView.Adapter<ActivityMainPlaylistAdapter.PlaylistAdapterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistAdapterViewHolder {
        val layoutId = if (viewType == 0) {
            R.layout.activity_main_playlist_item
        } else {
            R.layout.main_playlist_item_shimmer
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutId, null, false).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        return PlaylistAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistAdapterViewHolder, position: Int) {
        if (getItemViewType(position) == 1) {
            holder.itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer)?.startShimmer()
            return
        }

        val item = data[position]

        holder.itemView.findViewById<TextView>(R.id.title)?.text = item.albumTitle

        holder.itemView.findViewById<ImageView>(R.id.imageView)?.let { imageView ->
            Picasso.get().load(Uri.parse(item.albumCover)).into(imageView)
        }

        holder.itemView.setOnClickListener { v ->
            v.context.startActivity(
                Intent(v.context, ListActivity::class.java).apply {
                    putExtra("data", Gson().toJson(item))
                }
            )
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return if (data[position].id == "<shimmer>") 1 else 0
    }

    class PlaylistAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}