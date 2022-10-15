package com.example.jetcaster.play

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.jetcaster.Graph
import com.example.jetcaster.data.Episode
import com.example.jetcaster.data.extension.*
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import java.lang.Runnable

private data class EpisodeState(
    val currentMediaId: String = "",
    val playState: PlayState = PlayState.PREPARE,
    val playbackPosition: Long = 0L
) {
    fun pause(): EpisodeState {
        return this.copy(playState = PlayState.PAUSE)
    }

    fun playing(): EpisodeState {
        return this.copy(playState = PlayState.PLAYING)
    }

    fun position(position: Long): EpisodeState {
        return this.copy(playbackPosition = position)
    }
    fun mediaId(mediaId: String): EpisodeState {
        return this.copy(currentMediaId = mediaId)
    }

    fun updateMedia(episode: Episode): EpisodeState {
        return this.mediaId(episode.url).position(episode.playbackPosition)
    }
}

class PlayerControllerImpl(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : PlayerController() {
    private val TAG = "PlayerController"
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val mController: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    private val handler: Handler = Handler(Looper.getMainLooper())

    private val episodeStore = Graph.episodeStore

    private var episodeState: EpisodeState = EpisodeState()


    override fun init(context: Context) {
        initializeController(context)
    }

    override fun release() {
        updateEpisode(episodeState.pause())
        releaseController()
    }

    /**
     * 上一次播放的状态需要保存，不然每次都是ready
     */
    override fun play(episode: Episode) {
        Log.d(TAG, "$episode")
        when (val playerState = episode.playerAction) {
            is Play -> {
                when (mController?.isPlaying) {
                    true ->
                        if (episodeState.currentMediaId == episode.url) {
                            Log.d(TAG, "pause")
                            mController?.pause()
                            updateEpisode(episodeState.pause())
                        } else {
                            Log.d(TAG, "switch")
                            updateEpisode(episodeState.pause(), false)
                            updateEpisode(episodeState.updateMedia(episode).playing())
                            mController?.play(episode)
                        }
                    else ->
                        startPlayback(episode)
                }
            }
            is SeekTo -> {
                mController?.play(episode)
            }
            is SeekBack -> {
                mController?.seekBack()
            }
            is SeekForward -> {
                mController?.seekForward()
            }
            else -> playerState
        }
    }

    private fun startPlayback(episode: Episode){
        when(episode.url) {
            episodeState.currentMediaId -> {
                Log.d(TAG, "continue")
                updateEpisode(episodeState.playing())
                mController?.continuePlayback()
            }
            else -> {
                Log.d(TAG, "start")
                updateEpisode(episodeState.updateMedia(episode).playing())
                mController?.play(episode)
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
        positionState.update { position }
        if (mController?.hasMediaItems() == true) {
            updateEpisode(episodeState.position(position), true)
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

    private fun updateEpisode(episodeState: EpisodeState, isPlaying: Boolean = true) {
        this.episodeState = episodeState
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
                    isPlaying = isPlaying,
                    playState = episodeState.playState
                )
            )
        }
    }


}