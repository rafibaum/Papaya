package com.rafibaum.papaya.tracks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialContainerTransform
import com.rafibaum.papaya.R
import kotlinx.android.synthetic.main.fragment_tracks.*

class TracksFragment : Fragment() {

    private val args: TracksFragmentArgs by navArgs()

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
        sharedElementEnterTransition = enterTransform

        val exitTransform = MaterialContainerTransform()
        exitTransform.scaleMaskProgressThresholds =
            MaterialContainerTransform.ProgressThresholds(0.0f, 0.6f)
        sharedElementReturnTransition = exitTransform
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.transitionName = args.transitionName

        tracksList.setHasFixedSize(true)

        postponeEnterTransition()
        tracksList.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }

        //TODO
//        albumStore.albums.observe(viewLifecycleOwner) {
//            val album = it[args.albumIndex]
//            val adapter = TracksAdapter(this, args.albumIndex, album)
//            tracksList.adapter = adapter
//            tracksList.layoutManager = LinearLayoutManager(context)
//        }
    }
}