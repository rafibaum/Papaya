package com.rafibaum.papaya.albums

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.rafibaum.papaya.LiftScrollListener
import com.rafibaum.papaya.MainActivity
import com.rafibaum.papaya.R
import com.rafibaum.papaya.service.ALBUM_ID
import kotlinx.android.synthetic.main.fragment_albums.*
import kotlin.math.min

class AlbumFragment : Fragment() {

    private lateinit var adapter: AlbumAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_albums, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = (requireActivity() as MainActivity)
        activity.setSupportActionBar(appbar)

        adapter = AlbumAdapter(this)
        albumList.adapter = adapter
        val spans = resources.getInteger(R.integer.albumsSpan)
        albumList.layoutManager = GridLayoutManager(context, spans)
        albumList.addItemDecoration(
            SpacingItemDecoration(
                spans,
                resources.getDimensionPixelSize(R.dimen.albumsSpacing)
            )
        )
        albumList.setHasFixedSize(true)
        albumList.addOnScrollListener(LiftScrollListener(appbar))

        // Needed to make return transitions work. Suspends transition until recycler view is fully
        // loaded and transitions can be mapped properly on return.
        postponeEnterTransition()
    }

    override fun onStart() {
        super.onStart()

        val mediaBrowser = (requireActivity() as MainActivity).getMediaBrowser()
        mediaBrowser.subscribe(ALBUM_ID, albumBrowserCallback)
    }

    override fun onStop() {
        super.onStop()

        val mediaBrowser = (requireActivity() as MainActivity).getMediaBrowser()
        mediaBrowser.unsubscribe(ALBUM_ID, albumBrowserCallback)
    }

    private val albumBrowserCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            children.sortWith(Comparator { i1, i2 ->
                val a1 = i1.description.subtitle!!
                val a2 = i2.description.subtitle!!

                val len = min(a1.length, a2.length)

                for (i in 0..len) {
                    val cmp = a1[i].compareTo(a2[i])
                    if (cmp != 0) {
                        return@Comparator cmp
                    }
                }

                return@Comparator a1.length.compareTo(a2.length)
            })
            adapter.albums = children
            adapter.notifyDataSetChanged()
            (view?.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        override fun onError(parentId: String) {
            Log.e("AlbumAdapter", "Can't load albums")
        }
    }
}