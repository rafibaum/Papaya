package com.rafibaum.papaya.albums

import android.graphics.drawable.ColorDrawable
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rafibaum.papaya.R

class AlbumAdapter(private val fragment: Fragment) :
    RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {
    var albums: MutableList<MediaBrowserCompat.MediaItem>? = null
    private val placeholderColor = ColorDrawable(
        ContextCompat.getColor(
            fragment.requireContext(),
            R.color.placeholder
        )
    )

    inner class ViewHolder(val albumCoverView: View) : RecyclerView.ViewHolder(albumCoverView) {
        val albumName: TextView = albumCoverView.findViewById(R.id.albumName)
        val albumArtist: TextView = albumCoverView.findViewById(R.id.albumArtist)
        val coverImage: ImageView = albumCoverView.findViewById(R.id.albumCover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val albumCoverView = inflater.inflate(R.layout.album_cover, parent, false)
        return ViewHolder(albumCoverView)
    }

    override fun getItemCount(): Int = albums?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val description = albums!![position].description
        holder.albumName.text = description.title
        holder.albumArtist.text = description.subtitle
        Glide.with(fragment).load(description.iconUri).placeholder(placeholderColor)
            .into(holder.coverImage)
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