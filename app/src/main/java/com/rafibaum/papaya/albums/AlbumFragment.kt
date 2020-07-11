package com.rafibaum.papaya.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.rafibaum.papaya.R
import kotlinx.android.synthetic.main.fragment_albums.*

class AlbumFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_albums, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(appbar)

        val rvAlbums = albumList
        val albums = ArrayList<Album>()
        for (i in 0..4) {
            albums.add(Album(R.drawable.kamasi_heaven_and_earth))
            albums.add(Album(R.drawable.in_rainbows))
            albums.add(Album(R.drawable.the_only_difference))
        }
        val adapter = AlbumAdapter(albums)
        rvAlbums.adapter = adapter
        rvAlbums.layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.albumsSpan))
        rvAlbums.setHasFixedSize(true)
    }
}