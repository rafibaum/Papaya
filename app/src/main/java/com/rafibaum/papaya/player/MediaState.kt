package com.rafibaum.papaya.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rafibaum.papaya.R

/**
 * Represents current state of media playback.
 */
class MediaState : ViewModel() {
    private lateinit var mediaPlayer: MediaPlayer
    private val state: MutableLiveData<PlayingStatus> = MutableLiveData(
        PlayingStatus.IDLE
    )

    fun getState() = state

    fun setDataSource(context: Context, uri: Uri) {
//        mediaPlayer.setDataSource(context, uri)
//        mediaPlayer.setOnPreparedListener {
//            state.value = PlayingStatus.PAUSED
//        }
//        state.value = PlayingStatus.PREPARING
//        mediaPlayer.prepareAsync()
//        mediaPlayer = MediaPlayer.create(context, R.raw.clair)
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener {
            state.value = PlayingStatus.IDLE
        }
        state.value = PlayingStatus.PAUSED
    }

    fun play() {
        mediaPlayer.start()
        state.value = PlayingStatus.PLAYING
    }

    fun pause() {
        mediaPlayer.pause()
        state.value = PlayingStatus.PAUSED
    }

    fun getProgress(): Int = mediaPlayer.currentPosition

    fun getDuration(): Int = mediaPlayer.duration

    fun seekTo(progress: Int) = mediaPlayer.seekTo(progress)
}

enum class PlayingStatus {
    IDLE, // Awaiting data source
    PREPARING, // Loading data
    PLAYING,
    PAUSED,
}