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
import com.example.jetcaster.data.*
import com.example.jetcaster.play.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration


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

    private val viewModelState = MutableStateFlow(PlayerViewModelState())
    val uiState = viewModelState.map { it.toUiState() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )

    val playbackPositionState = MutableStateFlow(0L)

    init {
        viewModelScope.launch {
            episodeStore.episodeWithUri(episodeUri).collect { episode ->
                podcastStore.podcastWithUri(episode.podcastUri).collect { podcast ->
                    viewModelState.update { it.copy(episode = episode, podcast = podcast) }
                    playbackPositionState.update { episode.playbackPosition }
                }
            }

            playerController.positionState.collect { position ->
                playbackPositionState.update { position }
            }
        }
    }


    fun play(playerAction: PlayerAction) {
        return playerController.play(
            uiState.value.toEpisode().copy(
                playerAction = playerAction,
                playbackPosition = playbackPositionState.value
            )
        )
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
                    return PlayerViewModel(
                        episodeStore,
                        podcastStore,
                        handle,
                        playerController
                    ) as T
                }
            }
    }

}

data class PlayerViewModelState(
    val episode: EpisodeEntity? = null,
    val podcast: Podcast? = null
) {
    fun toUiState(): PlayerUiState {
        return if (episode == null || podcast == null) {
            PlayerUiState()
        } else {
            println("Player: $episode")
            PlayerUiState(
                title = episode.title,
                duration = episode.duration,
                podcastName = podcast.title,
                summary = episode.summary ?: "",
                podcastImageUrl = podcast.imageUrl ?: "",
                url = episode.uri,
                playbackPosition = episode.playbackPosition,
                playState = episode.playState
            )
        }
    }
}

data class PlayerUiState(
    val title: String = "",
    val subTitle: String = "",
    val duration: Duration? = null,
    val podcastName: String = "",
    val author: String = "",
    val summary: String = "",
    val podcastImageUrl: String = "",
    val url: String = "",
    val playState: PlayState = PlayState.PREPARE,
    val playbackPosition: Long = 0L
) {
    fun toEpisode(): Episode {
        return Episode(
            playState = playState,
            title = title,
            duration = duration,
            playbackPosition = playbackPosition,
            podcastImageUrl = podcastImageUrl,
            podcastName = podcastName,
            url = url
        )
    }
}