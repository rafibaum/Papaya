package com.rafibaum.papaya.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.rafibaum.papaya.R
import com.rafibaum.papaya.SpacingItemDecoration
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
            albums.add(Album("Heaven and Earth", "Kamasi Washington", R.drawable.kamasi_heaven_and_earth))
            albums.add(Album("In Rainbows", "Radiohead", R.drawable.in_rainbows))
            albums.add(Album("The Only Difference", "Beatchild & The Slakadeliqs", R.drawable.the_only_difference))
            albums.add(Album("A Moon Shaped Pool", "Radiohead", R.drawable.a_moon_shaped_pool))
            albums.add(Album("4", "The Bamboos", R.drawable.bamboos_4))
            albums.add(Album("In Colour", "Jaime xx", R.drawable.in_colour))
        }
        val adapter = AlbumAdapter(albums)
        val spans = resources.getInteger(R.integer.albumsSpan)
        rvAlbums.adapter = adapter
        rvAlbums.layoutManager = GridLayoutManager(context, spans)
        rvAlbums.addItemDecoration(SpacingItemDecoration(spans, resources.getDimensionPixelSize(R.dimen.albumsSpacing)))
        rvAlbums.setHasFixedSize(true)

    }
}