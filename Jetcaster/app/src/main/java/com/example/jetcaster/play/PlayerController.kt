package com.example.jetcaster.play

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.jetcaster.data.Episode
import com.example.jetcaster.ui.player.PlaybackPositionListener
import com.example.jetcaster.ui.player.PlayerUiState

abstract class PlayerController {
    abstract fun init(context: Context)
    abstract fun release()
    abstract fun play(episode: Episode): PlayerState
    abstract fun bind(listener: PlaybackPositionListener)
    abstract fun queryEpisodeState(url: String) : PlayerState
}