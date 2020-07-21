package com.rafibaum.papaya.tracks

import android.graphics.drawable.ColorDrawable
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rafibaum.papaya.R

private const val COVER = 0
private const val NAME = 1
private const val ARTIST = 2
private const val TRACK = 3

class TracksAdapter(
    private val fragment: Fragment,
    private val album: MediaBrowserCompat.MediaItem
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var tracks: MutableList<MediaBrowserCompat.MediaItem>? = null
    private val placeholderColor = ColorDrawable(
        ContextCompat.getColor(
            fragment.requireContext(),
            R.color.placeholder
        )
    )

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

    override fun getItemCount(): Int {
        tracks?.let { tracks ->
            return 3 + tracks.size
        }

        return 0
    }

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
                Glide.with(fragment).load(album.description.iconUri).placeholder(placeholderColor)
                    .into(coverHolder.coverImage)
            }
            NAME -> {
                val nameHolder: AlbumNameHolder = holder as AlbumNameHolder
                nameHolder.nameView.text = album.description.title
            }
            ARTIST -> {
                val artistHolder: AlbumArtistHolder = holder as AlbumArtistHolder
                artistHolder.artistView.text = album.description.subtitle
            }
            TRACK -> {
                val trackHolder: TrackViewHolder = holder as TrackViewHolder
                val trackPosition = position - 3
                val track = tracks!![trackPosition]
                trackHolder.trackPosition.text =
                    (trackPosition + 1).toString() //TODO: use actual position
                trackHolder.trackName.text = track.description.title

                val transitionName = "track_container_$trackPosition"
                trackHolder.trackView.transitionName = transitionName
                trackHolder.trackView.setOnClickListener {
                    //TODO
//                    val playTrack = TracksFragmentDirections.playTrack(
//                        albumIndex,
//                        trackPosition,
//                        transitionName
//                    )
//                    val playExtras = FragmentNavigatorExtras(
//                        trackHolder.trackView to transitionName
//                    )
//                    it.findNavController().navigate(playTrack, playExtras)
                }
            }
        }
    }
}