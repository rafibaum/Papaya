package com.rafibaum.papaya.albums

import android.net.Uri
import com.rafibaum.papaya.tracks.Track

class Album(val name: String, val artist: String, val cover: Uri?, val tracks: List<Track>)