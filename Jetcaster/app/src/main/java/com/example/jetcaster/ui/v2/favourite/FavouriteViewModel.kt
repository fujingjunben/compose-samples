package com.example.jetcaster.ui.v2.favourite

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetcaster.Graph
import com.example.jetcaster.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FavouriteViewModel(
    private val podcastStore: PodcastStore = Graph.podcastStore,
    private val episodeStore: EpisodeStore = Graph.episodeStore
) : ViewModel() {

    private val viewModelState = MutableStateFlow(FavouriteViewModelState(isLoading = true))

    val uiState = viewModelState
        .map { it.toUiState() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )

    init {
        viewModelScope.launch {
            podcastStore.followedPodcastsSortedByLastEpisode()
                .flatMapLatest { podcastWithExtraInfoList: List<PodcastWithExtraInfo> ->
                    val episodeToPodcasts: List<Flow<Pair<Podcast, List<EpisodeEntity>>>> =
                        podcastWithExtraInfoList.map { podcastWithExtraInfo ->
                            episodeStore.episodesInPodcast(
                                podcastWithExtraInfo.podcast.uri,
                                limit = 5
                            )
                                .map { episodeEntities: List<EpisodeEntity> ->
                                    podcastWithExtraInfo.podcast to episodeEntities
                                }
                        }
                    combine(episodeToPodcasts) { combined: Array<Pair<Podcast, List<EpisodeEntity>>> ->
                        combined.map { (podcast, episodes) ->
                            episodes.map { episodeEntity ->
                                EpisodeOfPodcast(podcast, episodeEntity)
                            }
                        }.flatten()
                    }
                }.collect { episodeOfPodcasts ->
                    viewModelState.update {
                        it.copy(
                            episodeOfPodcasts = episodeOfPodcasts,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onPodcastUnfollowed(podcastUri: String) {
        viewModelScope.launch {
            podcastStore.unfollowPodcast(podcastUri)
        }
    }

}

data class FavouriteViewModelState(
    val episodeOfPodcasts: List<EpisodeOfPodcast>? = null,
    val isLoading: Boolean = false
) {
    fun toUiState(): FavouriteUiState {
        println("episodeOfPodcasts: ${episodeOfPodcasts?.size}")
        return if (episodeOfPodcasts.isNullOrEmpty()) {
            FavouriteUiState.Loading
        } else {
            FavouriteUiState.Success(episodeOfPodcasts)
        }
    }
}

sealed interface FavouriteUiState {
    @Immutable
    data class Success(
        val episodeOfPodcasts: List<EpisodeOfPodcast> = listOf()
    ) : FavouriteUiState

    object Loading : FavouriteUiState
}

data class EpisodeOfPodcast(
    val podcast: Podcast,
    val episode: EpisodeEntity
)