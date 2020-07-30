package com.rafibaum.papaya.player

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.transition.MaterialContainerTransform
import com.rafibaum.papaya.R
import kotlinx.android.synthetic.main.fragment_player.*

private const val SEEKBAR_MAX = 1_000_000.0f

/**
 * UI for details about the currently playing track with music controls.
 */
class PlayerFragment : Fragment() {
    private val playerArgs: PlayerFragmentArgs by navArgs()
    private var playerState = PlaybackStateCompat.STATE_NONE
    private var mediaDuration: Float? = null

    private lateinit var placeholderColor: ColorDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val enterTransition = MaterialContainerTransform()
        enterTransition.scrimColor = Color.TRANSPARENT
        enterTransition.fadeMode = MaterialContainerTransform.FADE_MODE_THROUGH
        sharedElementEnterTransition = enterTransition

        placeholderColor = ColorDrawable(
            ContextCompat.getColor(
                requireContext(),
                R.color.placeholder
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.transitionName = playerArgs.transitionName

        postponeEnterTransition()

        val colour: Int = ContextCompat.getColor(requireContext(), R.color.seekbar)

        playerBar.progressDrawable.colorFilter =
            PorterDuffColorFilter(colour, PorterDuff.Mode.SRC_ATOP)

        val mediaController = MediaControllerCompat.getMediaController(requireActivity())

        // Play/pause button behaviour
        playerPlayBtn.setOnClickListener {
            when (playerState) {
                PlaybackStateCompat.STATE_PLAYING -> mediaController.transportControls.pause()
                else -> mediaController.transportControls.play()
            }
        }

        // Seeking behaviour
        playerBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mediaDuration?.let { duration ->
                    val seekDest = ((seekBar.progress / SEEKBAR_MAX) * duration).toLong()
                    mediaController.transportControls.seekTo(seekDest)
                }
            }
        })

        updateMetadata(mediaController.metadata)
        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    override fun onResume() {
        super.onResume()

        val mediaController = MediaControllerCompat.getMediaController(requireActivity())
        controllerCallback.onMetadataChanged(mediaController.metadata)
        controllerCallback.onPlaybackStateChanged(mediaController.playbackState)
        mediaController.registerCallback(controllerCallback)
    }

    override fun onPause() {
        super.onPause()

        val mediaController = MediaControllerCompat.getMediaController(requireActivity())
        mediaController.unregisterCallback(controllerCallback)
    }

    private fun updateMetadata(metadata: MediaMetadataCompat) {
        playerTrack.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
        playerArtist.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
        Glide.with(this@PlayerFragment)
            .load(Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)))
            .placeholder(placeholderColor).into(playerAlbumArt)
        mediaDuration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toFloat()
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            if (playerState != state.state) {
                playerState = state.state
                val icon = when (state.state) {
                    PlaybackStateCompat.STATE_PLAYING -> {
                        playerPlayBtn.isEnabled = true
                        R.drawable.pause
                    }
                    PlaybackStateCompat.STATE_PAUSED -> {
                        playerPlayBtn.isEnabled = true
                        R.drawable.play_arrow
                    }
                    else -> {
                        playerPlayBtn.isEnabled = false
                        R.drawable.play_arrow
                    }
                }

                val drawable = ContextCompat.getDrawable(requireContext(), icon)
                playerPlayBtn.setImageDrawable(drawable)
            }

            mediaDuration?.let { duration ->
                playerBar.progress = ((state.position / duration) * SEEKBAR_MAX).toInt()
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            updateMetadata(metadata)
        }
    }
}