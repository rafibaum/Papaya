package com.rafibaum.papaya.albums

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import com.rafibaum.papaya.R
import com.rafibaum.papaya.SpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_albums.*

private const val DIR_CHOOSER_ID = 30
private const val ALBUM_STORAGE_PREFERENCES = "ALBUM_STORAGE"
private const val ALBUM_URI_PREF = "ALBUM_URI"

class AlbumFragment : Fragment() {

    private val albumStore: AlbumStore by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val preferences =
            requireContext().getSharedPreferences(ALBUM_STORAGE_PREFERENCES, Context.MODE_PRIVATE)
        val dirUri = preferences.getString(ALBUM_URI_PREF, null)

        if (dirUri != null) {
            albumStore.setRootUri(Uri.parse(dirUri))
        } else {
            val dirChooser = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            dirChooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            dirChooser.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            startActivityForResult(
                Intent.createChooser(dirChooser, "Choose directory"),
                DIR_CHOOSER_ID
            )
        }

        return inflater.inflate(R.layout.fragment_albums, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(appbar)

        val rvAlbums = albumList
        val adapter = AlbumAdapter(albumStore.albums.value)
        val spans = resources.getInteger(R.integer.albumsSpan)
        rvAlbums.adapter = adapter
        rvAlbums.layoutManager = GridLayoutManager(context, spans)
        rvAlbums.addItemDecoration(
            SpacingItemDecoration(
                spans,
                resources.getDimensionPixelSize(R.dimen.albumsSpacing)
            )
        )
        rvAlbums.setHasFixedSize(true)

        // Needed to make return transitions work. Suspends transition until recycler view is fully
        // loaded and transitions can be mapped properly on return.
        postponeEnterTransition()
        rvAlbums.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }

        albumStore.albums.observe(viewLifecycleOwner) {
            adapter.update(it)
            albumLoadingProgress.visibility = View.INVISIBLE
            rvAlbums.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            DIR_CHOOSER_ID -> {
                if (resultCode != Activity.RESULT_OK) {
                    throw IllegalStateException("Result not okay")
                }

                //TODO: Handle properly
                data?.data?.let {
                    requireContext().contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    val preferences = requireContext().getSharedPreferences(
                        ALBUM_STORAGE_PREFERENCES,
                        Context.MODE_PRIVATE
                    )
                    with(preferences.edit()) {
                        putString(ALBUM_URI_PREF, it.toString())
                        apply()
                    }
                    albumStore.setRootUri(it)
                }
            }
        }
    }

}