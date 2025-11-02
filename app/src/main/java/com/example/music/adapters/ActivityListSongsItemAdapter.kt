package com.example.music.adapters

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.music.ApplicationClass
import com.example.music.R
import com.example.music.activities.MusicOverviewActivity
import com.example.music.records.SongResponse
import com.facebook.shimmer.ShimmerFrameLayout
import com.squareup.picasso.Picasso

class ActivityListSongsItemAdapter(
    private val data: List<SongResponse.Song>
) : RecyclerView.Adapter<ActivityListSongsItemAdapter.ViewHolder>() {

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

        val song = data[position]

        holder.itemView.findViewById<View>(R.id.title)?.isSelected = true
        holder.itemView.findViewById<View>(R.id.artist)?.isSelected = true

        holder.itemView.findViewById<TextView>(R.id.title)?.text = song.name

        val artistsNames = StringBuilder()
        for (artist in song.artists.all) {
            if (!artistsNames.contains(artist.name)) {
                artistsNames.append(artist.name)
                artistsNames.append(", ")
            }
        }

        holder.itemView.findViewById<TextView>(R.id.artist)?.text = artistsNames.toString()

        val images = song.image
        Picasso.get()
            .load(Uri.parse(images[images.size - 1].url))
            .into(holder.itemView.findViewById<ImageView>(R.id.coverImage))

        holder.itemView.setOnClickListener { view ->
            ApplicationClass.trackQueue?.let { queue ->
                if (queue.contains(song.id)) {
                    ApplicationClass.track_position = holder.bindingAdapterPosition
                }
            }

            holder.itemView.context.startActivity(
                Intent(view.context, MusicOverviewActivity::class.java).apply {
                    putExtra("id", song.id)
                }
            )
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return if (data[position].id == "<shimmer>") 1 else 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
