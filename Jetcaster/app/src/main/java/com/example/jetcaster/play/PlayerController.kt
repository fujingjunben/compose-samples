package com.example.jetcaster.play

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.jetcaster.ui.player.PlayerUiState

abstract class PlayerController {
    var playbackPosition by mutableStateOf(0L)

    abstract fun init(context: Context)
    abstract fun release()
    abstract fun play(uiState: PlayerUiState): PlayState
}