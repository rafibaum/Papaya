package com.rafibaum.papaya.player

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialContainerTransform
import com.rafibaum.papaya.R
import kotlinx.android.synthetic.main.fragment_player.*

/**
 * UI for details about the currently playing track with music controls.
 */
class PlayerFragment : Fragment() {
    // Provides information about player state
//    private val mediaState: MediaState by viewModels()
//    private val albumStore: AlbumStore by activityViewModels()
    private val playerArgs: PlayerFragmentArgs by navArgs()

    private val seekbarUpdater: Runnable = Runnable { updateSeekbar() }
    private val handler: Handler = Handler(Looper.getMainLooper())

    private lateinit var placeholderColor: ColorDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform()
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
        view.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }

        val colour: Int = ContextCompat.getColor(requireContext(), R.color.seekbar)

        playerBar.progressDrawable.colorFilter =
            PorterDuffColorFilter(colour, PorterDuff.Mode.SRC_ATOP)

        //TODO
        // Observe play/pause status
//        mediaState.getState().observe(viewLifecycleOwner, Observer<PlayingStatus> { status ->
//            val imageId = when (status) {
//                PlayingStatus.IDLE -> {
//                    playerPlayBtn.isEnabled = false
//                    R.drawable.play_arrow
//                }
//                PlayingStatus.PREPARING -> {
//                    playerPlayBtn.isEnabled = false
//                    R.drawable.play_arrow
//                }
//                PlayingStatus.READY -> {
//                    playerPlayBtn.isEnabled = true
//                    playerBar.max = mediaState.getDuration()
//                    R.drawable.play_arrow
//                }
//                PlayingStatus.PLAYING -> {
//                    playerPlayBtn.isEnabled = true
//                    playerBar.max = mediaState.getDuration()
//                    enableSeekbarUpdates()
//                    R.drawable.pause
//                }
//                PlayingStatus.PAUSED -> {
//                    playerPlayBtn.isEnabled = true
//                    playerBar.max = mediaState.getDuration()
//                    disableSeekbarUpdates()
//                    R.drawable.play_arrow
//                }
//            }
//
//            val drawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                resources.getDrawable(imageId, null)
//            } else {
//                resources.getDrawable(imageId)
//            }
//
//            playerPlayBtn.setImageDrawable(drawable)
//        })

        //TODO
        // Play/pause button behaviour
//        playerPlayBtn.setOnClickListener {
//            when (mediaState.getState().value) {
//                PlayingStatus.PLAYING -> mediaState.pause()
//                PlayingStatus.PAUSED -> mediaState.play()
//                PlayingStatus.READY -> mediaState.play()
//            }
//        }

        // Seeking behaviour
        playerBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                disableSeekbarUpdates()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //TODO
//                if (seekBar != null) {
//                    mediaState.seekTo(seekBar.progress)
//                }

                enableSeekbarUpdates()
            }
        })

        //TODO
//        albumStore.albums.observe(viewLifecycleOwner) {
//            val album = it[playerArgs.album]
//            val track = album.tracks[playerArgs.track]
//
//            playerTrack.text = track.name
//            playerArtist.text = album.artist
//            Glide.with(this).load(album.cover).placeholder(placeholderColor).into(playerAlbumArt)
//            mediaState.setDataSource(
//                requireContext(),
//                track.location
//            )
//        }
    }

    override fun onStop() {
        super.onStop()
        disableSeekbarUpdates()
    }

    private fun disableSeekbarUpdates() {
        handler.removeCallbacks(seekbarUpdater)
    }

    private fun enableSeekbarUpdates() {
        handler.post(seekbarUpdater)
    }

    private fun updateSeekbar() {
//        playerBar.progress = mediaState.getProgress()
        handler.postDelayed(seekbarUpdater, 100)
    }
}