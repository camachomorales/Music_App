package com.example.music.adapters

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.music.ApplicationClass
import com.example.music.R
import com.example.music.activities.MusicOverviewActivity
import com.example.music.databinding.ActivityListSongItemBinding
import com.example.music.records.sharedpref.SavedLibraries
import com.squareup.picasso.Picasso

class UserCreatedSongsListAdapter(
    private val data: List<SavedLibraries.Library.Song>
) : RecyclerView.Adapter<UserCreatedSongsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = View.inflate(parent.context, R.layout.activity_list_song_item, null).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.binding.title.text = item.title
        holder.binding.artist.text = item.description

        if (item.image.isNotBlank()) {
            Picasso.get().load(Uri.parse(item.image)).into(holder.binding.coverImage)
        }

        holder.itemView.setOnClickListener { view ->
            ApplicationClass.trackQueue?.let { queue ->
                if (queue.contains(item.id)) {
                    ApplicationClass.track_position = holder.bindingAdapterPosition
                }
            }

            holder.itemView.context.startActivity(
                Intent(view.context, MusicOverviewActivity::class.java).apply {
                    putExtra("id", item.id)
                }
            )
        }
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ActivityListSongItemBinding = ActivityListSongItemBinding.bind(itemView)
    }
}