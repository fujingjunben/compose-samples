package com.example.jetcaster.play

import android.content.Context
import com.example.jetcaster.ui.player.PlayerUiState

interface PlayerController {
    fun init(context: Context);
    fun release();
    fun play(uiState: PlayerUiState): PlayState;
    fun getPlaybackPosition(): Long
}