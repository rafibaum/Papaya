package com.rafibaum.papaya.tracks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.rafibaum.papaya.R
import com.rafibaum.papaya.albums.Album

private const val COVER = 0
private const val NAME = 1
private const val ARTIST = 2
private const val TRACK = 3

class TracksAdapter(private val albumIndex: Int, private val album: Album) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class AlbumCoverHolder(coverView: View) : RecyclerView.ViewHolder(coverView) {
        val coverImage: ImageView = coverView.findViewById(R.id.tracksAlbumCover)
    }

    inner class AlbumNameHolder(val nameView: TextView) : RecyclerView.ViewHolder(nameView)

    inner class AlbumArtistHolder(val artistView: TextView) : RecyclerView.ViewHolder(artistView)

    inner class TrackViewHolder(val trackView: View) : RecyclerView.ViewHolder(trackView) {
        val trackPosition: TextView = trackView.findViewById(R.id.trackPosition)
        val trackName: TextView = trackView.findViewById(R.id.trackName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            COVER -> {
                val view = inflater.inflate(R.layout.tracks_album_cover, parent, false)
                AlbumCoverHolder(view)
            }
            NAME -> {
                val view = inflater.inflate(R.layout.tracks_album_name, parent, false)
                AlbumNameHolder(view as TextView)
            }
            ARTIST -> {
                val view = inflater.inflate(R.layout.tracks_album_artist, parent, false)
                AlbumArtistHolder(view as TextView)
            }
            TRACK -> {
                val view = inflater.inflate(R.layout.track, parent, false)
                TrackViewHolder(view)
            }
            else -> throw IllegalStateException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = 3 + album.tracks.size

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> COVER
            1 -> NAME
            2 -> ARTIST
            else -> TRACK
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            COVER -> {
                val coverHolder: AlbumCoverHolder = holder as AlbumCoverHolder
                coverHolder.coverImage.setImageURI(album.cover)
            }
            NAME -> {
                val nameHolder: AlbumNameHolder = holder as AlbumNameHolder
                nameHolder.nameView.text = album.name
            }
            ARTIST -> {
                val artistHolder: AlbumArtistHolder = holder as AlbumArtistHolder
                artistHolder.artistView.text = album.artist
            }
            TRACK -> {
                val trackHolder: TrackViewHolder = holder as TrackViewHolder
                val trackPosition = position - 3
                val track = album.tracks[trackPosition]
                trackHolder.trackPosition.text = track.position.toString()
                trackHolder.trackName.text = track.name

                val transitionName = "track_container_$trackPosition"
                trackHolder.trackView.transitionName = transitionName
                trackHolder.trackView.setOnClickListener {
                    val playTrack = TracksFragmentDirections.playTrack(
                        albumIndex,
                        trackPosition,
                        transitionName
                    )
                    val playExtras = FragmentNavigatorExtras(
                        trackHolder.trackView to transitionName
                    )
                    it.findNavController().navigate(playTrack, playExtras)
                }
            }
        }
    }
}