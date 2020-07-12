package com.rafibaum.papaya.albums

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import com.rafibaum.papaya.R
import com.rafibaum.papaya.SpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_albums.*
import kotlinx.coroutines.launch

private const val DIR_CHOOSER_ID: Int = 30

class AlbumFragment : Fragment() {

    private val albums: MutableLiveData<List<Album>> = MutableLiveData()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dirChooser = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        dirChooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(Intent.createChooser(dirChooser, "Choose directory"), DIR_CHOOSER_ID)

        return inflater.inflate(R.layout.fragment_albums, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(appbar)

        val rvAlbums = albumList
        val adapter = AlbumAdapter(albums.value)
        val spans = resources.getInteger(R.integer.albumsSpan)
        rvAlbums.adapter = adapter
        rvAlbums.layoutManager = GridLayoutManager(context, spans)
        rvAlbums.addItemDecoration(SpacingItemDecoration(spans, resources.getDimensionPixelSize(R.dimen.albumsSpacing)))
        rvAlbums.setHasFixedSize(true)

        albums.observe(viewLifecycleOwner) {
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

                data?.data?.let {
                    viewLifecycleOwner.lifecycleScope.launch {
                        getAlbumsFromUri(it)
                    }
                }
            }
        }
    }

    private fun getAlbumsFromUri(rootUri: Uri) {
        val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE)

        val contentResolver = requireContext().contentResolver
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri))

        val albums = ArrayList<Album>()

        val artistsCursor = contentResolver.query(childrenUri, projection, null, null, null)
            ?: throw IllegalStateException("Null cursor")

        while (artistsCursor.moveToNext()) {
            val artistDocId = artistsCursor.getString(0)
            val artistName = artistsCursor.getString(1)
            val artistMime = artistsCursor.getString(2)

            if (DocumentsContract.Document.MIME_TYPE_DIR == artistMime) {
                // Artist directory
                val artistUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, artistDocId)
                val albumsCursor = contentResolver.query(artistUri, projection, null, null, null) ?: throw IllegalStateException("Null album cursor")

                while (albumsCursor.moveToNext()) {
                    val albumDocId = albumsCursor.getString(0)
                    val albumName = albumsCursor.getString(1)
                    val albumMime = albumsCursor.getString(2)

                    if (DocumentsContract.Document.MIME_TYPE_DIR == albumMime) {
                        // Album directory
                        val tracks = ArrayList<Track>()
                        var coverUri: Uri? = null
                        val albumUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, albumDocId)
                        val trackCursor = contentResolver.query(albumUri, projection, null, null, null) ?: throw IllegalStateException("Null track cursor")

                        while (trackCursor.moveToNext()) {
                            val fileDocId = trackCursor.getString(0)
                            val fileName = trackCursor.getString(1)
                            val fileMime = trackCursor.getString(2)

                            if (fileMime.startsWith("audio")) {
                                val splitTrack = fileName.split("-")
                                val position = splitTrack[0].trim().toInt()

                                val trackNameWithExtension = splitTrack[1].trim()
                                val trackName = trackNameWithExtension.substring(0, trackNameWithExtension.lastIndexOf("."))

                                val trackUri = DocumentsContract.buildDocumentUriUsingTree(rootUri, fileDocId)

                                tracks.add(Track(trackName, position, trackUri))
                            } else if (fileMime.startsWith("image")) {
                                coverUri = DocumentsContract.buildDocumentUriUsingTree(rootUri, fileDocId)
                            }
                        }

                        val sortedTracks = tracks.sortedBy { it.position }
                        albums.add(Album(albumName, artistName, coverUri, sortedTracks))

                        trackCursor.close()
                    }
                }

                albumsCursor.close()
            }
        }

        artistsCursor.close()

        this.albums.postValue(albums)
    }

}