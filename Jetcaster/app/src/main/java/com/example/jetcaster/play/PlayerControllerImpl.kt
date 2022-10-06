package com.example.jetcaster.play

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.jetcaster.Graph
import com.example.jetcaster.ui.player.PlayerUiState
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.lang.Runnable

class PlayerControllerImpl(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : PlayerController {
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    private val handler: Handler = Handler(Looper.getMainLooper())

    private val episodeStore = Graph.episodeStore

    private var uiState: PlayerUiState? = null

    private var playbackPosition: Long = 0L

    override fun getPlaybackPosition(): Long {
        return playbackPosition
    }

    override fun init(context: Context) {
        initializeController(context)
    }

    override fun release() {
        releaseController()
    }

    override fun play(uiState: PlayerUiState): PlayState {
        this.uiState = uiState
        return when (uiState.playState) {
            is PlayReady -> {
                controller?.setMediaItem(buildMediaItem(uiState))
                controller?.prepare()
                controller?.play()
                Playing(0L)
            }
            is Playing -> {
                controller?.pause()
                PlayPause(playbackPosition)
            }
            is PlayPause -> {
                controller?.play()
                Playing(playbackPosition)
            }
            is PlaySeek -> {
                controller?.seekTo((uiState.playState.position * uiState.duration!!.seconds).toLong())
                playbackPosition = uiState.playState.position
                Playing(uiState.playState.position)
            }
            is PlayError -> {
                PlayError
            }
        }
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
        togglePeriodicProgressUpdateRequest()

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

    private fun buildMediaItem(state: PlayerUiState): MediaItem {
        // get the correct source for streaming / local playback
        // put uri in RequestMetadata - credit: https://stackoverflow.com/a/70103460
        val source = state.url
        val requestMetadata = MediaItem.RequestMetadata.Builder().apply {
            setMediaUri(source.toUri())
        }.build()
        // build MediaItem and return it
        val mediaMetadata = MediaMetadata.Builder().apply {
            setAlbumTitle(state.podcastName)
            setTitle(state.title)
            setArtworkUri(state.podcastImageUrl.toUri())
        }.build()
        return MediaItem.Builder().apply {
            setRequestMetadata(requestMetadata)
            setMediaMetadata(mediaMetadata)
            setUri(source.toUri())
        }.build()
    }


    /*
     * Runnable: Periodically requests playback position (and sleep timer if running)
     */
    private val periodicProgressUpdateRequestRunnable: Runnable = object : Runnable {
        override fun run() {
            // update progress bar
            updateProgressBar()
            // use the handler to start runnable again after specified delay
            handler.postDelayed(this, 500)
        }
    }
    /*
     * End of declaration
     */

    /* Updates the progress bar */
    private fun updateProgressBar() {
        // update progress bar - only if controller is prepared with a media item
        val position = controller?.currentPosition ?: 0L
        playbackPosition = position
        if (this.uiState != null) {
            val url = this.uiState!!.url
            scope.launch {
                val episode = episodeStore.episodeWithUri(url).first()
                episodeStore.updateEpisode(episode.copy(position = playbackPosition))

            }
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
            togglePeriodicProgressUpdateRequest()
        }
    }
}