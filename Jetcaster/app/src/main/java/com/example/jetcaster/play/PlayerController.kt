package com.example.jetcaster.play

import android.content.Context
import com.example.jetcaster.data.Episode
import com.example.jetcaster.ui.player.PlaybackPositionListener
import kotlinx.coroutines.flow.MutableStateFlow

abstract class PlayerController {
    val positionState =  MutableStateFlow(0L)
    abstract fun init(context: Context)
    abstract fun release()
    abstract fun play(episode: Episode): PlayerAction
    abstract fun bind(listener: PlaybackPositionListener)

}