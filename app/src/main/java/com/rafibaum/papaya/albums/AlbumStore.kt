package com.rafibaum.papaya.albums

import android.app.Application
import android.net.Uri
import android.provider.DocumentsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlbumStore(application: Application) : AndroidViewModel(application) {
    val albums: MutableLiveData<List<Album>> = MutableLiveData()

    fun setRootUri(rootUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            getAlbumsFromUri(rootUri)
        }
    }

    private fun getAlbumsFromUri(rootUri: Uri) {
        val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE)

        val contentResolver = getApplication<Application>().contentResolver
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