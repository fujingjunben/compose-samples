/*
 * Copyright 2021 The Android Open Source Project
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

package com.example.jetcaster.ui.player

import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.jetcaster.Graph
import com.example.jetcaster.data.EpisodeStore
import com.example.jetcaster.data.PodcastStore
import com.example.jetcaster.play.PlayReady
import com.example.jetcaster.play.PlayState
import com.example.jetcaster.play.PlayerController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration

data class PlayerUiState(
    val title: String = "",
    val subTitle: String = "",
    val duration: Duration? = null,
    val podcastName: String = "",
    val author: String = "",
    val summary: String = "",
    val podcastImageUrl: String = "",
    val url: String = "",
    val playState: PlayState = PlayReady,
    val playbackPosition: Long = 0L,
    val isPlaying: Boolean = false
)

/**
 * ViewModel that handles the business logic and screen state of the Player screen
 */
class PlayerViewModel(
    episodeStore: EpisodeStore,
    podcastStore: PodcastStore,
    savedStateHandle: SavedStateHandle,
    private val playerController: PlayerController
) : ViewModel() {

    // episodeUri should always be present in the PlayerViewModel.
    // If that's not the case, fail crashing the app!
    private val episodeUri: String = Uri.decode(savedStateHandle.get<String>("episodeUri")!!)

    var uiState by mutableStateOf(PlayerUiState())
        private set

    var paybackPosition by mutableStateOf(uiState.playbackPosition)

    init {
        viewModelScope.launch {
            val episode = episodeStore.episodeWithUri(episodeUri).first()
            println("episode: $episode")
            val podcast = podcastStore.podcastWithUri(episode.podcastUri).first()
            uiState = PlayerUiState(
                title = episode.title,
                duration = episode.duration,
                podcastName = podcast.title,
                summary = episode.summary ?: "",
                podcastImageUrl = podcast.imageUrl ?: "",
                url = episode.uri,
                playbackPosition = episode.playbackPosition,
                isPlaying = episode.isPlaying
            )
        }
    }

    fun play(playState: PlayState): PlayState {
        return playerController.play(uiState.copy(playState = playState))
    }


    /**
     * Factory for PlayerViewModel that takes EpisodeStore and PodcastStore as a dependency
     */
    companion object {
        fun provideFactory(
            episodeStore: EpisodeStore = Graph.episodeStore,
            podcastStore: PodcastStore = Graph.podcastStore,
            playerController: PlayerController = Graph.playerController,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return PlayerViewModel(episodeStore, podcastStore, handle, playerController) as T
                }
            }
    }
}
