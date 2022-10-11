package com.example.jetcaster.ui.v2.favourite

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetcaster.Graph
import com.example.jetcaster.data.Podcast
import com.example.jetcaster.data.PodcastStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class FavouriteViewModel(
    private val podcastStore: PodcastStore = Graph.podcastStore
) : ViewModel() {

    val favouritePodcast: Flow<FavouriteUiState> = queryFavouritePodcasts()

    private fun queryFavouritePodcasts(): Flow<FavouriteUiState> {
        val podcasts = podcastStore.followedPodcastsSortedByLastEpisode()
        return flowOf()
    }
}

sealed interface FavouriteUiState {
    data class Success(val podcasts: List<Podcast>) : FavouriteUiState
    object Loading : FavouriteUiState
    object Error : FavouriteUiState
}