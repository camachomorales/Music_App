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
import com.example.music.records.SongResponse
import com.squareup.picasso.Picasso

class ActivitySeeMoreListAdapter(
    private val data: MutableList<SongResponse.Song> = mutableListOf()
) : RecyclerView.Adapter<ActivitySeeMoreListAdapter.ViewHolder>() {

    private var isLoadingAdded = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (viewType == 1) {
            R.layout.activity_artist_profile_view_top_songs_item
        } else {
            R.layout.progress_bar_layout
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
            return
        }

        val item = data[position]
        val images = item.image

        holder.itemView.findViewById<TextView>(R.id.position)?.text = (position + 1).toString()
        holder.itemView.findViewById<TextView>(R.id.coverTitle)?.text = item.name
        holder.itemView.findViewById<TextView>(R.id.coverPlayed)?.text =
            String.format("%s | %s", item.year, item.label)

        holder.itemView.findViewById<ImageView>(R.id.coverImage)?.let { coverImage ->
            Picasso.get()
                .load(Uri.parse(images[images.size - 1].url))
                .into(coverImage)
        }

        holder.itemView.setOnClickListener { view ->
            view.context.startActivity(
                Intent(view.context, MusicOverviewActivity::class.java).apply {
                    putExtra("id", item.id)
                }
            )
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int = 1

    fun add(item: SongResponse.Song) {
        data.add(item)
        notifyItemInserted(data.size - 1)
    }

    fun addAll(moveResults: List<SongResponse.Song>) {
        moveResults.forEach { add(it) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    enum class Mode {
        TOP_SONGS,
        TOP_ALBUMS,
        TOP_SINGLES
    }

    companion object {
        private const val LOADING = 0
        private const val ITEM = 1
    }
}