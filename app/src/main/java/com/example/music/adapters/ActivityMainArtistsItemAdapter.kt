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
import com.example.music.api.model.BasicDataRecord
import com.example.music.records.ArtistsSearch
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import com.squareup.picasso.Picasso

class ActivityMainArtistsItemAdapter(
    private val data: List<ArtistsSearch.Data.Results>
) : RecyclerView.Adapter<ActivityMainArtistsItemAdapter.ActivityMainArtistsItemAdapterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityMainArtistsItemAdapterViewHolder {
        val layoutId = if (viewType == 0) {
            R.layout.activity_main_artists_item
        } else {
            R.layout.artists_item_shimmer
        }

        val view = View.inflate(parent.context, layoutId, null).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        return ActivityMainArtistsItemAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityMainArtistsItemAdapterViewHolder, position: Int) {
        if (getItemViewType(position) == 1) {
            holder.itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer)?.startShimmer()
            return
        }

        val item = data[position]
        val images = item.image

        holder.itemView.findViewById<TextView>(R.id.artist_name)?.apply {
            text = item.name
            isSelected = true
        }

        holder.itemView.findViewById<ImageView>(R.id.artist_img)?.let { imageView ->
            Picasso.get()
                .load(Uri.parse(images[images.size - 1].url))
                .into(imageView)
        }

        holder.itemView.setOnClickListener { v ->
            val basicDataRecord = BasicDataRecord(
                item.id,
                item.name,
                "",
                images[images.size - 1].url
            )

            v.context.startActivity(
                Intent(v.context, ArtistProfileActivity::class.java).apply {
                    putExtra("data", Gson().toJson(basicDataRecord))
                }
            )
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return if (data[position].id == "<shimmer>") 1 else 0
    }

    class ActivityMainArtistsItemAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}