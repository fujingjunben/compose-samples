package com.example.jetcaster.play

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.jetcaster.Graph
import com.example.jetcaster.data.Episode
import com.example.jetcaster.data.extension.continuePlayback
import com.example.jetcaster.data.extension.hasMediaItems
import com.example.jetcaster.data.extension.play
import com.example.jetcaster.ui.player.PlaybackPositionListener
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.lang.Runnable

private data class EpisodeState(
    val currentMediaId: String = "",
    val playerState: PlayerState = None,
    val playbackPosition: Long = 0L
)

class PlayerControllerImpl(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : PlayerController() {
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val mController: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    private val handler: Handler = Handler(Looper.getMainLooper())

    private val episodeStore = Graph.episodeStore

    private var episodeState: EpisodeState = EpisodeState()

    override fun queryEpisodeState(url: String): PlayerState {
        return if (url == episodeState.currentMediaId) {
            episodeState.playerState
        } else {
            Ready
        }
    }

    override fun init(context: Context) {
        initializeController(context)
    }

    override fun release() {
        releaseController()
    }

    override fun play(episode: Episode): PlayerState {
        return when (val playerState = episode.playerState) {
            is Ready -> {
                if (episode.url != episodeState.currentMediaId) {
                    updateEpisode(episodeState.copy(playerState = Pause))
                }
                mController?.play(episode)
                episodeState = episodeState.copy(currentMediaId = episode.url, playerState = Playing)
                Playing
            }
            is Playing -> {
                mController?.pause()
                episodeState = episodeState.copy(playerState = Pause)
                updateEpisode(episodeState)
                Pause
            }
            is Pause -> {
                mController?.continuePlayback()
                episodeState = episodeState.copy(playerState = Playing)
                Playing
            }
            is SeekTo -> {
                val isPlaying = mController!!.isPlaying
                mController?.pause()
                val position = playerState.position
                mController?.seekTo(position)

                if (isPlaying) {
                    mController?.continuePlayback()
                    Playing
                } else {
                    Pause
                }
            }
            is SeekBack -> {
                mController?.seekBack()
                Playing
            }
            is SeekForward -> {
                mController?.seekForward()
                Playing
            }
            else -> playerState
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
        val controller: MediaController = this.mController ?: return

        // update playback progress state
//        togglePeriodicProgressUpdateRequest()

        controller.addListener(playerListener)
    }

    /* Toggle periodic request of playback position from player service */
    private fun togglePeriodicProgressUpdateRequest() {
        when (mController?.isPlaying) {
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
        val position = mController?.currentPosition ?: 0L
        episodeState = episodeState.copy(playbackPosition = position)
        positionListener?.onChange(position)
        if (mController?.hasMediaItems() == true) {
            updateEpisode(episodeState)
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
            if (isPlaying) {
                togglePeriodicProgressUpdateRequest()
            }
        }
    }

    private fun updateEpisode(episodeState: EpisodeState) {
        if (mController == null) {
            return
        }

        if (episodeState.currentMediaId.isEmpty()) {
            return
        }

        val position =
            if (episodeState.playbackPosition >= mController!!.duration - 500) 0L else episodeState.playbackPosition

        scope.launch {
            val episode = episodeStore.episodeWithUri(episodeState.currentMediaId).first()
            episodeStore.updateEpisode(
                episode.copy(
                    playbackPosition = position,
                    isPlaying = episodeState.playerState is Playing
                )
            )
        }
    }


}