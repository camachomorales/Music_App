package com.example.music.adapters

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.activities.ArtistProfileActivity
import com.example.music.activities.ListActivity
import com.example.music.activities.MusicOverviewActivity
import com.example.music.api.model.AlbumItem
import com.example.music.api.model.BasicDataRecord
import com.example.music.api.model.SearchListItem
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import com.squareup.picasso.Picasso

class ActivitySearchListItemAdapter(
    private val data: List<SearchListItem>
) : RecyclerView.Adapter<ActivitySearchListItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (viewType == 0) {
            R.layout.activity_list_song_item
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
        holder.itemView.findViewById<TextView>(R.id.artist)?.text = item.subtitle

        Picasso.get()
            .load(Uri.parse(item.coverImage))
            .into(holder.itemView.findViewById<ImageView>(R.id.coverImage))

        holder.itemView.setOnClickListener { view ->
            val intent = Intent().apply {
                putExtra("id", item.id)

                when (item.type) {
                    SearchListItem.Type.SONG -> {
                        setClass(holder.itemView.context, MusicOverviewActivity::class.java)
                    }
                    SearchListItem.Type.ALBUM -> {
                        val albumItem = AlbumItem(
                            item.title,
                            item.subtitle,
                            item.coverImage,
                            item.id
                        )
                        putExtra("data", Gson().toJson(albumItem))
                        putExtra("type", "album")
                        setClass(holder.itemView.context, ListActivity::class.java)
                    }
                    SearchListItem.Type.PLAYLIST -> {
                        val albumItem = AlbumItem(
                            item.title,
                            item.subtitle,
                            item.coverImage,
                            item.id
                        )
                        putExtra("data", Gson().toJson(albumItem))
                        setClass(holder.itemView.context, ListActivity::class.java)
                    }
                    SearchListItem.Type.ARTIST -> {
                        setClass(holder.itemView.context, ArtistProfileActivity::class.java)
                        putExtra(
                            "data",
                            Gson().toJson(BasicDataRecord(item.id, item.title, "", item.coverImage))
                        )
                    }
                    else -> {}
                }
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return if (data[position].id == "<shimmer>") 1 else 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}