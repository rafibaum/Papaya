package com.rafibaum.papaya

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialContainerTransform
import com.rafibaum.papaya.albums.AlbumStore
import kotlinx.android.synthetic.main.fragment_tracks.*

class TracksFragment : Fragment() {

    private val args: TracksFragmentArgs by navArgs()
    private val albumStore: AlbumStore by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tracks, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transform = MaterialContainerTransform()
        sharedElementEnterTransition = transform
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        albumStore.albums.observe(viewLifecycleOwner) {
            textView.text = it[args.albumIndex].name
            artistText.text = it[args.albumIndex].artist
            imageView.setImageURI(it[args.albumIndex].cover)
        }
    }
}