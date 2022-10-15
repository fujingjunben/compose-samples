package com.example.jetcaster.ui.v2.playerBar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetcaster.Graph
import com.example.jetcaster.data.Episode
import com.example.jetcaster.data.EpisodeStore
import com.example.jetcaster.data.EpisodeToPodcast
import com.example.jetcaster.data.PodcastStore
import com.example.jetcaster.play.None
import com.example.jetcaster.play.PlayerController
import com.example.jetcaster.play.PlayerState
import com.example.jetcaster.play.Playing
import com.example.jetcaster.ui.player.PlayerUiState
import com.example.jetcaster.ui.v2.favourite.EpisodeOfPodcast
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlayerBarViewModel(
    private val episodeStore: EpisodeStore = Graph.episodeStore,
    private val controller: PlayerController = Graph.playerController
) : ViewModel() {
    private val viewModelState = MutableStateFlow(PlayerBarViewModelState())

    val uiState = viewModelState.map {
        it.toUiState()
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value.toUiState()
    )

    init {
//        viewModelScope.launch {
//            episodeStore.episodeWhichIsPlaying().collect {
//                episodeToPodcasts ->
//                episodeToPodcasts.forEach {
//                    val (episode) = it
//                    println("episodeToPodcasts : $episode")
//                }
//                if (episodeToPodcasts.isNotEmpty()) {
//                    viewModelState.update { it.copy(episodeToPodcast = episodeToPodcasts[0]) }
//                }
//            }
//        }
    }

    fun play(playerState: PlayerState): PlayerState {
        return when (uiState.value) {
            is PlayerBarUiState.Success -> {
                val (episodeEntity, podcast) = (uiState.value as PlayerBarUiState.Success).episodeToPodcast
                val episode = Episode(
                    url = episodeEntity.uri,
                    title = episodeEntity.title,
                    podcastImageUrl = podcast.imageUrl,
                    podcastName = podcast.title,
                    playbackPosition = episodeEntity.playbackPosition,
                    playerState = playerState,
                    duration = episodeEntity.duration
                )
                controller.play(episode)
            }
            else -> None
        }
    }
}

private data class PlayerBarViewModelState(
    val isPlaying: Boolean = false,
    val episodeToPodcast: EpisodeToPodcast? = null
) {
    fun toUiState(): PlayerBarUiState {
        return if (episodeToPodcast == null) {
            PlayerBarUiState.Loading
        } else {
            PlayerBarUiState.Success(episodeToPodcast)
        }
    }
}

sealed interface PlayerBarUiState {
    data class Success(val episodeToPodcast: EpisodeToPodcast) : PlayerBarUiState
    object Loading : PlayerBarUiState

}