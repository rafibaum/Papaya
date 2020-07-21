package com.rafibaum.papaya.service

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.rafibaum.papaya.albums.Album

const val EMPTY_ID = "EMPTY"
const val ROOT_ID = "ROOT"
const val ALBUM_ID = "ALBUMS"

const val MUSIC_STORAGE_PREFERENCES = "MUSIC_STORAGE"
const val MUSIC_URI_PREF = "MUSIC_URI"

class PapayaService : MediaBrowserServiceCompat() {
    private var library: Library? = null
    private var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    override fun onCreate() {
        super.onCreate()

        val preferences =
            applicationContext.getSharedPreferences(MUSIC_STORAGE_PREFERENCES, Context.MODE_PRIVATE)
        val dirUri = preferences.getString(MUSIC_URI_PREF, null) ?: return

        library = Library(applicationContext, Uri.parse(dirUri))

        mediaSession = MediaSessionCompat(baseContext, "PapayaMediaSession").apply {
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
            setPlaybackState(stateBuilder.build())
            setCallback(SessionCallback())
            setSessionToken(sessionToken)
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return if (library != null) {
            BrowserRoot(ROOT_ID, null)
        } else {
            BrowserRoot(EMPTY_ID, null)
        }
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        library?.let { library ->
            val idParts = parentId.split("/")

            val items = when (idParts[0]) {
                EMPTY_ID -> null
                ROOT_ID -> root()
                ALBUM_ID -> albums(idParts, library)
                else -> null
            }

            result.sendResult(items)
            return
        }

        result.sendResult(null)
    }

    private fun root(): MutableList<MediaBrowserCompat.MediaItem> {
        val items = ArrayList<MediaBrowserCompat.MediaItem>()

        val albums = MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat.Builder().setMediaId(ALBUM_ID).setTitle("Albums").build(),
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        )
        items.add(albums)
        return items
    }

    private fun albums(
        idParts: List<String>,
        library: Library
    ): MutableList<MediaBrowserCompat.MediaItem>? {
        return if (idParts.size < 2) {
            albumListing(library)
        } else {
            trackListing(idParts[1], library)
        }
    }

    private fun albumListing(library: Library): MutableList<MediaBrowserCompat.MediaItem> {
        val items = ArrayList<MediaBrowserCompat.MediaItem>()
        for (album in library.albums.values) {
            val albumItem = getAlbumMediaItem(album)
            items.add(albumItem)
        }
        return items
    }

    private fun trackListing(
        key: String,
        library: Library
    ): MutableList<MediaBrowserCompat.MediaItem>? {
        val album = library.albums[key] ?: return null
        val items = ArrayList<MediaBrowserCompat.MediaItem>()

        for ((pos, track) in album.tracks.withIndex()) {
            val trackItem = MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder().setMediaId("$ALBUM_ID/${album.uuid}/$pos")
                    .setTitle(track.name).build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
            )
            items.add(trackItem)
        }

        return items
    }

    override fun onLoadItem(itemId: String, result: Result<MediaBrowserCompat.MediaItem>) {
        library?.let { library ->
            val idParts = itemId.split("/")

            val item = when (idParts[0]) {
                ALBUM_ID -> album(idParts[1], library)
                else -> null
            }

            result.sendResult(item)
            return
        }

        result.sendResult(null)
        return
    }

    private fun album(
        key: String,
        library: Library
    ): MediaBrowserCompat.MediaItem? {
        val album = library.albums[key] ?: return null
        return getAlbumMediaItem(album)
    }

    private fun getAlbumMediaItem(album: Album): MediaBrowserCompat.MediaItem {
        return MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat.Builder().setMediaId("$ALBUM_ID/${album.uuid}")
                .setTitle(album.name)
                .setSubtitle(album.artist).setIconUri(album.cover).build(),
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        )
    }

    inner class SessionCallback : MediaSessionCompat.Callback()
}