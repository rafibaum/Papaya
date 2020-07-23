package com.rafibaum.papaya.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.rafibaum.papaya.R
import com.rafibaum.papaya.albums.Album

const val EMPTY_ID = "EMPTY"
const val ROOT_ID = "ROOT"
const val ALBUM_ID = "ALBUMS"
const val EXTRA_TRACK_POSITION = "TRACK_POS"

const val MUSIC_STORAGE_PREFERENCES = "MUSIC_STORAGE"
const val MUSIC_URI_PREF = "MUSIC_URI"

private const val PAPAYA_NOTIF_ID = 883

class PapayaService : MediaBrowserServiceCompat() {
    private var library: Library? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var mediaPlayer: MediaPlayer
    private val updateHandler = Handler(Looper.getMainLooper())

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

        mediaPlayer = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer.release()
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
            val extras = Bundle()
            extras.putInt(EXTRA_TRACK_POSITION, track.position)

            val trackItem = MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder().setMediaId("$ALBUM_ID/${album.uuid}/$pos")
                    .setTitle(track.name).setExtras(extras).build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
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

    private val stateUpdater = Runnable {
        updateState()
    }

    private fun updateState() {
        stateBuilder.setState(
            PlaybackStateCompat.STATE_PLAYING,
            mediaPlayer.currentPosition.toLong(),
            1.0f
        )
        mediaSession.setPlaybackState(stateBuilder.build())
        updateHandler.postDelayed(stateUpdater, 100)
    }

    private fun enableUpdating() {
        updateHandler.post(stateUpdater)
    }

    private fun disableUpdating() {
        updateHandler.removeCallbacks(stateUpdater)
    }

    inner class SessionCallback : MediaSessionCompat.Callback() {
        private lateinit var focusRequest: AudioFocusRequest

        override fun onPlay() {
            val focusResult = getAudioFocus()

            if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                return
            }

            startService(Intent(applicationContext, PapayaService::class.java))

            mediaSession.isActive = true
            stateBuilder.setState(
                PlaybackStateCompat.STATE_PLAYING,
                mediaPlayer.currentPosition.toLong(),
                1.0f
            )
            mediaSession.setPlaybackState(stateBuilder.build())

            mediaPlayer.start()

            registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
            startForeground(PAPAYA_NOTIF_ID, buildMediaNotification())
            enableUpdating()
        }

        override fun onSeekTo(pos: Long) {
            mediaPlayer.seekTo(pos.toInt())
        }

        override fun onPause() {
            disableUpdating()
            stateBuilder.setState(
                PlaybackStateCompat.STATE_PAUSED,
                mediaPlayer.currentPosition.toLong(),
                1.0f
            )
            mediaSession.setPlaybackState(stateBuilder.build())

            mediaPlayer.pause()

            unregisterReceiver(noisyReceiver)

            stopForeground(false)
        }

        override fun onStop() {
            abandonAudioFocus()
            stopSelf()
            disableUpdating()

            mediaSession.isActive = false
            stateBuilder.setState(
                PlaybackStateCompat.STATE_STOPPED,
                mediaPlayer.currentPosition.toLong(),
                1.0f
            )
            mediaSession.setPlaybackState(stateBuilder.build())

            mediaPlayer.stop()

            stopForeground(false)
        }

        override fun onPrepareFromMediaId(mediaId: String, extras: Bundle) {
            library?.let { library ->
                disableUpdating()
                mediaPlayer.reset()
                val idParts = mediaId.split("/")
                val album = library.albums[idParts[1]] ?: return
                val track = album.tracks[idParts[2].toInt()]
                mediaPlayer.setDataSource(applicationContext, track.location)

                stateBuilder.setState(PlaybackStateCompat.STATE_BUFFERING, 0, 1.0f)
                mediaSession.setPlaybackState(stateBuilder.build())

                val metadataBuilder = MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.name)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album.name)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, album.artist)

                album.cover?.let {
                    metadataBuilder.putString(
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                        it.toString()
                    )
                }

                mediaSession.setMetadata(metadataBuilder.build())

                mediaPlayer.setOnPreparedListener {
                    metadataBuilder.putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        mediaPlayer.duration.toLong()
                    )
                    mediaSession.setMetadata(metadataBuilder.build())

                    stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f)
                    mediaSession.setPlaybackState(stateBuilder.build())
                }

                mediaPlayer.prepareAsync()
            }
        }

        private fun getAudioFocus(): Int {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                    setAudioAttributes(AudioAttributes.Builder().run {
                        setUsage(AudioAttributes.USAGE_MEDIA)
                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        build()
                    })
                    setAcceptsDelayedFocusGain(true)
                    setOnAudioFocusChangeListener(focusCallback)
                    build()
                }

                audioManager.requestAudioFocus(focusRequest)
            } else {
                audioManager.requestAudioFocus(
                    focusCallback,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }
        }

        private fun abandonAudioFocus() {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(focusRequest)
            } else {
                audioManager.abandonAudioFocus(focusCallback)
            }
        }

        private fun buildMediaNotification(): Notification {
            val description = mediaSession.controller.metadata.description

            val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "papaya_service",
                    "Papaya Playback",
                    NotificationManager.IMPORTANCE_LOW
                )
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
                "papaya_service"
            } else {
                ""
            }

            val builder =
                NotificationCompat.Builder(applicationContext, channelId).apply {
                    setContentTitle(description.title)
                    setContentText(description.subtitle)
                    setSubText(description.description)
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(contentResolver, description.iconUri)
                    setLargeIcon(bitmap)

                    setContentIntent(mediaSession.controller.sessionActivity)

                    setDeleteIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            applicationContext,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )

                    setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    setSmallIcon(R.mipmap.ic_launcher)
                    color = ContextCompat.getColor(applicationContext, R.color.papayaDark)

                    addAction(
                        NotificationCompat.Action(
                            R.drawable.pause,
                            getString(R.string.pause),
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                applicationContext,
                                PlaybackStateCompat.ACTION_PLAY_PAUSE
                            )
                        )
                    )

                    setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession.sessionToken)
                            .setShowActionsInCompactView(0)
                            .setShowCancelButton(true)
                            .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                    applicationContext,
                                    PlaybackStateCompat.ACTION_STOP
                                )
                            )
                    )
                }
            return builder.build()
        }
    }

    private val focusCallback = AudioManager.OnAudioFocusChangeListener { focus ->
        when (focus) {
            AudioManager.AUDIOFOCUS_GAIN -> mediaSession.controller.transportControls.play()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> mediaSession.controller.transportControls.pause()
            //TODO: Consider delayed stop for playback for permanent loss of focus (see docs)
            AudioManager.AUDIOFOCUS_LOSS -> mediaSession.controller.transportControls.pause()
        }
    }

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                mediaSession.controller.transportControls.pause()
            }
        }
    }
}