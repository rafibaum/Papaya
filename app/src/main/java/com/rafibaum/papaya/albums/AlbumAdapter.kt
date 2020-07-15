package com.rafibaum.papaya.albums

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.rafibaum.papaya.R

class AlbumAdapter(private var albums: List<Album>?) :
    RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    inner class ViewHolder(val albumCoverView: View) : RecyclerView.ViewHolder(albumCoverView) {
        val albumName: TextView = albumCoverView.findViewById(R.id.albumName)
        val albumArtist: TextView = albumCoverView.findViewById(R.id.albumArtist)
        val coverImage: ImageView = albumCoverView.findViewById(R.id.albumCover)
    }

    fun update(albums: List<Album>) {
        this.albums = albums
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val albumCoverView = inflater.inflate(R.layout.album_cover, parent, false)
        return ViewHolder(albumCoverView)
    }

    override fun getItemCount(): Int = albums?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = albums!![position]
        holder.albumName.text = album.name
        holder.albumArtist.text = album.artist
        holder.coverImage.setImageURI(album.cover)

        val transitionName = "album_cover_$position"
        holder.albumCoverView.transitionName = transitionName
        holder.albumCoverView.setOnClickListener {
            val toTracks = AlbumFragmentDirections.seeTracks(position, transitionName)
            val extras = FragmentNavigatorExtras(
                holder.albumCoverView to transitionName
            )
            it.findNavController().navigate(toTracks, extras)
        }
    }


}