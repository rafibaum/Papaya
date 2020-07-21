package com.rafibaum.papaya.albums

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.rafibaum.papaya.MainActivity
import com.rafibaum.papaya.R
import com.rafibaum.papaya.service.ALBUM_ID
import kotlinx.android.synthetic.main.fragment_albums.*

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
        (activity as AppCompatActivity).setSupportActionBar(appbar)

        adapter = AlbumAdapter(this, ArrayList())
        val spans = resources.getInteger(R.integer.albumsSpan)
        albumList.adapter = adapter
        albumList.layoutManager = GridLayoutManager(context, spans)
        albumList.addItemDecoration(
            SpacingItemDecoration(
                spans,
                resources.getDimensionPixelSize(R.dimen.albumsSpacing)
            )
        )
        albumList.setHasFixedSize(true)

        // Needed to make return transitions work. Suspends transition until recycler view is fully
        // loaded and transitions can be mapped properly on return.
        postponeEnterTransition()
        albumList.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }
    }

    override fun onStart() {
        super.onStart()

        val mediaBrowser = (requireActivity() as MainActivity).getMediaBrowser()
        mediaBrowser.subscribe(ALBUM_ID, albumsCallback)
    }

    private val albumsCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            val list = ArrayList<AlbumView>()
            for (item in children) {
                val description = item.description
                list.add(
                    AlbumView(
                        description.title!!.toString(),
                        description.subtitle!!.toString(),
                        description.iconUri!!
                    )
                )
            }

            adapter.update(list)
            albumLoadingProgress.visibility = View.INVISIBLE
            albumList.visibility = View.VISIBLE
        }

        override fun onError(parentId: String) {

        }
    }
}