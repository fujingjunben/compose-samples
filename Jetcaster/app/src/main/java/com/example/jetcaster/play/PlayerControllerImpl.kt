package com.example.jetcaster.play

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.MutableState
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.jetcaster.Graph
import com.example.jetcaster.data.extension.continuePlayback
import com.example.jetcaster.data.extension.hasMediaItems
import com.example.jetcaster.data.extension.play
import com.example.jetcaster.ui.player.PlaybackPositionListener
import com.example.jetcaster.ui.player.PlayerUiState
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.lang.Runnable

class PlayerControllerImpl(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : PlayerController() {
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    private val handler: Handler = Handler(Looper.getMainLooper())

    private val episodeStore = Graph.episodeStore

    private var playState: PlayState = PlayState()

    override fun isPlaying(url: String): Boolean {
        return if (url == playState.currentMediaId) {
            playState.isPlaying
        } else {
            false
        }
    }

    override fun init(context: Context) {
        initializeController(context)
    }

    override fun release() {
        releaseController()
    }

    override fun play(uiState: PlayerUiState): PlayerState {
        println("uiState: $uiState")
        return when (uiState.playState) {
            is PlayerReady -> {
                if (playState.currentMediaId.isNotEmpty() && playState.currentMediaId != uiState.url) {
                    updateEpisode(playState.copy(isPlaying = false))
                }
                playState = playState.copy(currentMediaId = uiState.url, isPlaying = true)
                if (uiState.playState.position > 0L) {
                    controller?.seekTo(uiState.playState.position)
                }
                controller?.play(uiState)
                Playing(0L)
            }
            is Playing -> {
                controller?.pause()
                PlayerPause(playState.playbackPosition)
            }
            is PlayerPause -> {
                controller?.continuePlayback()
                Playing(playState.playbackPosition)
            }
            is PlayerSeek -> {
                val position = uiState.playState.position
                println("playbackPosition: $position")
                controller?.seekTo(position)

                if (controller!!.isPlaying) {
                    Playing(position)
                } else {
                    PlayerPause(position)
                }
            }
            is PlayerError -> {
                PlayerError
            }
        }
    }

    private var positionListener: PlaybackPositionListener? = null
    override fun bind(listener: PlaybackPositionListener) {
        this.positionListener = listener
    }


    /* Initializes the MediaController - handles connection to PlayerService under the hood */
    private fun initializeController(context: Context) {
        controllerFuture = MediaController.Builder(
            context, SessionToken(
                context,
                ComponentName(context, PlaybackService::class.java)
            )
        ).buildAsync()
        controllerFuture.addListener({ setupController() }, MoreExecutors.directExecutor())
    }


    /* Sets up the MediaController  */
    private fun setupController() {
        val controller: MediaController = this.controller ?: return

        // update playback progress state
//        togglePeriodicProgressUpdateRequest()

        controller.addListener(playerListener)
    }

    /* Toggle periodic request of playback position from player service */
    private fun togglePeriodicProgressUpdateRequest() {
        when (controller?.isPlaying) {
            true -> {
                handler.removeCallbacks(periodicProgressUpdateRequestRunnable)
                handler.postDelayed(periodicProgressUpdateRequestRunnable, 0)
            }
            else -> {
                handler.removeCallbacks(periodicProgressUpdateRequestRunnable)
            }
        }
    }


    /* Releases MediaController */
    private fun releaseController() {
        MediaController.releaseFuture(controllerFuture)
    }


    /*
     * Runnable: Periodically requests playback position (and sleep timer if running)
     */
    private val periodicProgressUpdateRequestRunnable: Runnable = object : Runnable {
        override fun run() {
            // update progress bar
            updateProgressBar()
            // use the handler to start runnable again after specified delay
            handler.postDelayed(this, 1000)
        }
    }
    /*
     * End of declaration
     */

    /* Updates the progress bar */
    private fun updateProgressBar() {
        // update progress bar - only if controller is prepared with a media item
        val position = controller?.currentPosition ?: 0L
        playState = playState.copy(playbackPosition = position)
        positionListener?.onChange(position)
        if (controller?.hasMediaItems() == true) {
            updateEpisode(playState)
        }
    }


    /*
     * Player.Listener: Called when one or more player states changed.
     */
    private var playerListener: Player.Listener = object : Player.Listener {

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            println("onIsPlayingChanged: $isPlaying")
            if (isPlaying) {
                togglePeriodicProgressUpdateRequest()
            }
        }
    }

    private fun updateEpisode(playState: PlayState) {
        if (controller == null) {
            return
        }
        val position =
            if (playState.playbackPosition >= controller!!.duration) 0L else playState.playbackPosition

        scope.launch {
            val episode = episodeStore.episodeWithUri(playState.currentMediaId).first()
            episodeStore.updateEpisode(
                episode.copy(
                    playbackPosition = position,
                    isPlaying = playState.isPlaying
                )
            )
        }
    }

    data class PlayState(
        val currentMediaId: String = "",
        val isPlaying: Boolean = false,
        val playbackPosition: Long = 0L
    )
}