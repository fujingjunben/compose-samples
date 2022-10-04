package com.example.jetcaster.play

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaybackService: MediaSessionService() {
    public val URL = "MEDIA_URL";
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true)
            .build()
        player.addListener(playerListener)
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(CustomSessionCallback())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.hasExtra(URL)) {
            intent.getStringExtra(URL)?.let {
                player.setMediaItem(MediaItem.fromUri(it))
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    /*
     * Player.Listener: Called when one or more player states changed.
     */
    private val playerListener: Player.Listener = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            val mediaId: String = player.currentMediaItem?.mediaId ?: String()
            val currentPosition: Long = player.currentPosition
            // store state of playback

            if (isPlaying) {
                // playback is active - start to periodically save the playback position to database
                println("playing")
            } else {
                println("state: ${player.playbackState}")
                // playback is not active - stop periodically saving the playback position to database

                // Not playing because playback is paused, ended, suppressed, or the player
                // is buffering, stopped or failed. Check player.getPlayWhenReady,
                // player.getPlaybackState, player.getPlaybackSuppressionReason and
                // player.getPlaybackError for details.
                when (player.playbackState) {
                    // player is able to immediately play from its current position
                    Player.STATE_READY -> {
                        // todo
                    }
                    // buffering - data needs to be loaded
                    Player.STATE_BUFFERING -> {
                        // todo
                    }
                    // player finished playing all media
                    Player.STATE_ENDED -> {
                    }
                    // initial state or player is stopped or playback failed
                    Player.STATE_IDLE -> {
                        // todo
                    }
                }
            }
        }
    }

    private inner class CustomSessionCallback : MediaSession.Callback {

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<List<MediaItem>> {
            val updatedMediaItems = mediaItems.map { mediaItem ->
                mediaItem.buildUpon().apply {
                    setUri(mediaItem.requestMetadata.mediaUri)
                }.build()
            }
            return Futures.immediateFuture(updatedMediaItems)
        }

    }
}