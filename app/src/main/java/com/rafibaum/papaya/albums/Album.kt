package com.rafibaum.papaya.albums

import android.net.Uri

class Album(val name: String, val artist: String, val cover: Uri?, val tracks: List<Track>)

class Track(val name: String, val position: Int, val location: Uri)