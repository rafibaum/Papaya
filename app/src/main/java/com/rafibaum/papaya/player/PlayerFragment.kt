package com.rafibaum.papaya.player

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.rafibaum.papaya.R
import kotlinx.android.synthetic.main.fragment_player.*

/**
 * UI for details about the currently playing track with music controls.
 */
class PlayerFragment : Fragment() {

    // Provides information about player state
    private val mediaState: MediaState by viewModels()

    private val seekbarUpdater: Runnable = Runnable { updateSeekbar() }
    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch colour for player seek bar
        val colour: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(android.R.color.darker_gray, null)
        } else {
            resources.getColor(android.R.color.darker_gray)
        }

        playerBar.progressDrawable.colorFilter =
            PorterDuffColorFilter(colour, PorterDuff.Mode.SRC_ATOP)

        // Observe play/pause status
        mediaState.getState().observe(viewLifecycleOwner, Observer<PlayingStatus> { status ->
            val imageId = when (status) {
                PlayingStatus.IDLE -> {
                    playerPlayBtn.isEnabled = false
                    R.drawable.play_arrow_24px
                }
                PlayingStatus.PREPARING -> {
                    playerPlayBtn.isEnabled = false
                    R.drawable.play_arrow_24px
                }
                PlayingStatus.PLAYING -> {
                    playerPlayBtn.isEnabled = true
                    enableSeekbarUpdates()
                    R.drawable.pause_24px
                }
                PlayingStatus.PAUSED -> {
                    playerPlayBtn.isEnabled = true
                    disableSeekbarUpdates()
                    R.drawable.play_arrow_24px
                }
            }

            val drawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                resources.getDrawable(imageId, null)
            } else {
                resources.getDrawable(imageId)
            }

            playerPlayBtn.setImageDrawable(drawable)
        })

        // Play/pause button behaviour
        playerPlayBtn.setOnClickListener {
            when (mediaState.getState().value) {
                PlayingStatus.PLAYING -> mediaState.pause()
                PlayingStatus.PAUSED -> mediaState.play()
            }
        }

        // Seeking behaviour
        playerBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                disableSeekbarUpdates()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    mediaState.seekTo(seekBar.progress)
                }

                enableSeekbarUpdates()
            }
        })

        mediaState.setDataSource(
            requireContext(),
            Uri.parse("android.resource://com.rafibaum.papaya/raw/clair.mp3")
        )
        playerBar.max = mediaState.getDuration()
    }

    private fun disableSeekbarUpdates() {
        handler.removeCallbacks(seekbarUpdater)
    }

    private fun enableSeekbarUpdates() {
        handler.post(seekbarUpdater)
    }

    private fun updateSeekbar() {
        playerBar.progress = mediaState.getProgress()
        handler.postDelayed(seekbarUpdater, 100)
    }
}