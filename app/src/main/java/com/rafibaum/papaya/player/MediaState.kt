package com.rafibaum.papaya.player

import android.media.MediaPlayer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Represents current state of media playback.
 */
class MediaState : ViewModel() {
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private val state: MutableLiveData<PlayingStatus> = MutableLiveData(
        PlayingStatus.IDLE
    )

    fun getState() = state

    fun setDataSource() {
        mediaPlayer.setDataSource()
    }
}

enum class PlayingStatus {
    IDLE, // Awaiting data source
    PREPARING, // Loading data
    PLAYING,
    PAUSED,
}