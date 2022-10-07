package com.example.jetcaster.data

data class Episode(val isPaying: Boolean,
                   val isFinished: Boolean,
                   val playbackPosition: Long,
                   val url: String,
                   val podcastName: String,
                   val podcastImageUrl: String,
                   val title: String
)
