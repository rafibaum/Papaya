package com.rafibaum.papaya.albums

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.rafibaum.papaya.R

class AlbumAdapter(private val albums: List<Album>) : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    inner class ViewHolder(albumCoverView: View) : RecyclerView.ViewHolder(albumCoverView) {
        val coverImage: ImageView = albumCoverView.findViewById(R.id.albumCover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val albumCoverView = inflater.inflate(R.layout.album_cover, parent, false)
        return ViewHolder(albumCoverView)
    }

    override fun getItemCount(): Int = albums.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = albums[position]
        holder.coverImage.setImageResource(album.cover)
    }

}