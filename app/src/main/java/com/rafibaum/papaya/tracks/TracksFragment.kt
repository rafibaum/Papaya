package com.rafibaum.papaya.tracks

import android.graphics.Color
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialContainerTransform
import com.rafibaum.papaya.LiftScrollListener
import com.rafibaum.papaya.MainActivity
import com.rafibaum.papaya.R
import kotlinx.android.synthetic.main.fragment_tracks.*

class TracksFragment : Fragment() {

    private val args: TracksFragmentArgs by navArgs()
    private lateinit var adapter: TracksAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tracks, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val enterTransform = MaterialContainerTransform()
        enterTransform.scaleMaskProgressThresholds =
            MaterialContainerTransform.ProgressThresholds(0.25f, 1.0f)
        enterTransform.scrimColor = Color.TRANSPARENT
        sharedElementEnterTransition = enterTransform

        val exitTransform = MaterialContainerTransform()
        exitTransform.scaleMaskProgressThresholds =
            MaterialContainerTransform.ProgressThresholds(0.0f, 0.6f)
        exitTransform.scrimColor = Color.TRANSPARENT
        sharedElementReturnTransition = exitTransform
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.transitionName = args.transitionName

        tracksList.setHasFixedSize(true)

        postponeEnterTransition()

        adapter = TracksAdapter(this, args.album)
        tracksList.adapter = adapter
        tracksList.layoutManager = LinearLayoutManager(context)
        tracksList.addOnScrollListener(LiftScrollListener(tracksAppbar))
    }

    override fun onStart() {
        super.onStart()

        val mediaBrowser = (requireActivity() as MainActivity).getMediaBrowser()
        mediaBrowser.subscribe(args.album.mediaId!!, tracksCallback)
    }

    override fun onStop() {
        super.onStop()

        val mediaBrowser = (requireActivity() as MainActivity).getMediaBrowser()
        mediaBrowser.unsubscribe(args.album.mediaId!!, tracksCallback)
    }

    private val tracksCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            adapter.tracks = children
            adapter.notifyDataSetChanged()
            (view?.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        override fun onError(parentId: String) {
            Log.e("TracksFragment", "Couldn't load tracks")
        }
    }
}