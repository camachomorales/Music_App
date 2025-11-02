package com.example.music.adapters

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.activities.ListActivity
import com.example.music.api.model.AlbumItem
import com.example.music.records.sharedpref.SavedLibraries
import com.google.gson.Gson
import com.squareup.picasso.Picasso

class SavedLibrariesAdapter(
    private val data: List<SavedLibraries.Library>
) : RecyclerView.Adapter<SavedLibrariesAdapter.ViewHolder>() {

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

        holder.title.text = item.name
        holder.artist.text = item.description
        Picasso.get().load(Uri.parse(item.image)).into(holder.coverImage)

        holder.itemView.setOnClickListener { v ->
            val albumItem = AlbumItem(
                item.name,
                item.description,
                item.image,
                item.id
            )

            if (item.isCreatedByUser) {
                v.context.startActivity(
                    Intent(v.context, ListActivity::class.java).apply {
                        putExtra("id", item.id)
                        putExtra("data", Gson().toJson(albumItem))
                        putExtra("type", "playlist")
                        putExtra("createdByUser", true)
                    }
                )
                return@setOnClickListener
            }

            v.context.startActivity(
                Intent(v.context, ListActivity::class.java).apply {
                    putExtra("data", Gson().toJson(albumItem))
                    putExtra("type", if (item.isAlbum) "album" else "playlist")
                    putExtra("id", item.id)
                }
            )
        }
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverImage: ImageView = itemView.findViewById(R.id.coverImage)
        val title: TextView = itemView.findViewById(R.id.title)
        val artist: TextView = itemView.findViewById(R.id.artist)
    }
}