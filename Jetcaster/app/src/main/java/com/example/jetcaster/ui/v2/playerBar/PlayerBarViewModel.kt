package com.example.jetcaster.ui.v2.playerBar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetcaster.Graph
import com.example.jetcaster.data.EpisodeStore
import com.example.jetcaster.data.EpisodeToPodcast
import com.example.jetcaster.data.PodcastStore
import com.example.jetcaster.play.PlayerState
import com.example.jetcaster.play.Playing
import com.example.jetcaster.ui.player.PlayerUiState
import com.example.jetcaster.ui.v2.favourite.EpisodeOfPodcast
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlayerBarViewModel(
    private val podcastStore: PodcastStore = Graph.podcastStore,
    private val episodeStore: EpisodeStore = Graph.episodeStore
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
        viewModelScope.launch {
            val episodeToPodcast: EpisodeToPodcast = episodeStore.episodeWhichIsPlaying().first()
            viewModelState.update { it.copy(episodeToPodcast = episodeToPodcast) }
        }
    }

    fun play(playerState: PlayerState): PlayerState {
        return Playing(0)
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