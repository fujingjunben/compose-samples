/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jetcaster.ui

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker.Companion.getOrCreate
import com.example.jetcaster.play.PlaybackService
import com.example.jetcaster.ui.player.PlayerUiState
import com.example.jetcaster.ui.theme.JetcasterTheme
import com.example.jetcaster.util.DevicePosture
import com.example.jetcaster.util.isBookPosture
import com.example.jetcaster.util.isSeparatingPosture
import com.example.jetcaster.util.isTableTopPosture
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainActivity : ComponentActivity() {
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This app draws behind the system bars, so we want to handle fitting system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)

        /**
         * Flow of [DevicePosture] that emits every time there's a change in the windowLayoutInfo
         */
        val devicePosture = getOrCreate(this).windowLayoutInfo(this)
            .flowWithLifecycle(this.lifecycle)
            .map { layoutInfo ->
                val foldingFeature =
                    layoutInfo.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
                when {
                    isTableTopPosture(foldingFeature) ->
                        DevicePosture.TableTopPosture(foldingFeature.bounds)
                    isBookPosture(foldingFeature) ->
                        DevicePosture.BookPosture(foldingFeature.bounds)
                    isSeparatingPosture(foldingFeature) ->
                        DevicePosture.SeparatingPosture(
                            foldingFeature.bounds,
                            foldingFeature.orientation
                        )
                    else -> DevicePosture.NormalPosture
                }
            }
            .stateIn(
                scope = lifecycleScope,
                started = SharingStarted.Eagerly,
                initialValue = DevicePosture.NormalPosture
            )

        setContent {
            JetcasterTheme {
                JetcasterApp(devicePosture, play = { state ->  play(state)})
            }
        }
    }

    override fun onStart() {
        super.onStart()
        initializeController()
    }

    /* Overrides onStop from Fragment */
    override fun onStop() {
        super.onStop()
        // release MediaController - cut connection to PlayerService
        releaseController()
    }

    /* Initializes the MediaController - handles connection to PlayerService under the hood */
    private fun initializeController() {
        controllerFuture = MediaController.Builder(this, SessionToken(this, ComponentName(this, PlaybackService::class.java))).buildAsync()
        controllerFuture.addListener({ setupController() }, MoreExecutors.directExecutor())
    }


    /* Releases MediaController */
    private fun releaseController() {
        MediaController.releaseFuture(controllerFuture)
    }


    /* Sets up the MediaController  */
    private fun setupController() {
    }

    private fun play(state: PlayerUiState): Boolean {
        if (state.isPlaying) {
            controller?.pause()
        } else {
            controller?.setMediaItem(buildMediaItem(state))
            controller?.prepare()
            controller?.play()
        }
        return true
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


}
