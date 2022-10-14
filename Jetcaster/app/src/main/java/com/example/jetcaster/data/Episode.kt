package com.example.jetcaster.data

import java.time.Duration

const val THRESHOLD = 500

data class Episode(val isPlaying: Boolean,
                   val duration: Duration?,
                   val playbackPosition: Long,
                   val url: String,
                   val podcastName: String,
                   val podcastImageUrl: String?,
                   val title: String
) {
    fun isFinished(): Boolean {
        return if (duration == null){
            false
        } else {
            playbackPosition > duration.toMillis() - THRESHOLD
        }
    }
}
