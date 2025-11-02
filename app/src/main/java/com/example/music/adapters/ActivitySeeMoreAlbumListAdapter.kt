package com.example.music.adapters

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.activities.ListActivity
import com.example.music.api.model.AlbumItem
import com.example.music.records.AlbumsSearch
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import com.squareup.picasso.Picasso

class ActivitySeeMoreAlbumListAdapter(
    private val data: MutableList<AlbumsSearch.Data.Results> = mutableListOf()
) : RecyclerView.Adapter<ActivitySeeMoreAlbumListAdapter.ViewHolder>() {

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

        val item = data[position]
        val images = item.image

        holder.itemView.findViewById<TextView>(R.id.position)?.text = (position + 1).toString()
        holder.itemView.findViewById<TextView>(R.id.coverTitle)?.text = item.name
        holder.itemView.findViewById<TextView>(R.id.coverPlayed)?.text =
            String.format("%s | %s", item.year, item.language)

        holder.itemView.findViewById<ImageView>(R.id.coverImage)?.let { coverImage ->
            Picasso.get()
                .load(Uri.parse(images[images.size - 1].url))
                .into(coverImage)
        }

        holder.itemView.setOnClickListener {
            val albumItem = AlbumItem(
                item.id,
                item.name,
                images[images.size - 1].url,
                item.id
            )

            it.context.startActivity(
                Intent(it.context, ListActivity::class.java).apply {
                    putExtra("data", Gson().toJson(albumItem))
                    putExtra("type", "album")
                    putExtra("id", item.id)
                }
            )
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int = 1

    fun add(item: AlbumsSearch.Data.Results) {
        data.add(item)
        notifyItemInserted(data.size - 1)
    }

    fun addAll(moveResults: List<AlbumsSearch.Data.Results>) {
        moveResults.forEach { add(it) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}