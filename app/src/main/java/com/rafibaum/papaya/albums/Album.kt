package com.rafibaum.papaya.albums

import android.net.Uri
import com.rafibaum.papaya.tracks.Track
import java.util.*

class Album(
    val uuid: UUID,
    val name: String,
    val artist: String,
    val cover: Uri?,
    val tracks: List<Track>
)