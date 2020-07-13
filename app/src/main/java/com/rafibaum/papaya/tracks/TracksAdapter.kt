package com.rafibaum.papaya.tracks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rafibaum.papaya.R
import com.rafibaum.papaya.albums.Album

class TracksAdapter(private val album: Album) : RecyclerView.Adapter<TracksAdapter.ViewHolder>() {

    inner class ViewHolder(trackView: View) : RecyclerView.ViewHolder(trackView) {
        val trackPosition: TextView = trackView.findViewById(R.id.trackPosition)
        val trackName: TextView = trackView.findViewById(R.id.trackName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val trackView = inflater.inflate(R.layout.track, parent, false)
        return ViewHolder(trackView)
    }

    override fun getItemCount(): Int = album.tracks.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = album.tracks[position]
        holder.trackPosition.text = track.position.toString()
        holder.trackName.text = track.name
    }

}