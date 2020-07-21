package com.rafibaum.papaya.service

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat

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
            val items = ArrayList<MediaBrowserCompat.MediaItem>()

            when (parentId) {
                EMPTY_ID -> {
                    result.sendResult(null)
                    return
                }
                ROOT_ID -> root(items)
                ALBUM_ID -> albums(library, items)
            }

            result.sendResult(items)
            return
        }

        result.sendResult(null)
    }

    private fun root(items: MutableList<MediaBrowserCompat.MediaItem>) {
        val albums = MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat.Builder().setMediaId(ALBUM_ID).setTitle("Albums").build(),
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        )
        items.add(albums)
    }

    private fun albums(library: Library, items: MutableList<MediaBrowserCompat.MediaItem>) {
        for (entry in library.albums) {
            val uuid = entry.key
            val album = entry.value

            val albumItem =
                MediaBrowserCompat.MediaItem(
                    MediaDescriptionCompat.Builder().setMediaId("$ALBUM_ID/$uuid")
                        .setTitle(album.name)
                        .setSubtitle(album.artist).setIconUri(album.cover).build(),
                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                )
            items.add(albumItem)
        }
    }

    inner class SessionCallback : MediaSessionCompat.Callback()
}