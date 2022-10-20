package com.example.jetcaster.ui.v2.episode

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.jetcaster.Graph
import com.example.jetcaster.data.Episode
import com.example.jetcaster.data.EpisodeStore
import com.example.jetcaster.data.PodcastStore
import com.example.jetcaster.ui.v2.Destination

class EpisodeViewModel(
    val episodeStore: EpisodeStore,
    val podcastStore: PodcastStore,
    savedStateHandle: SavedStateHandle,
): ViewModel(){
    private val episodeUri: String = Uri.decode(savedStateHandle.get<String>(Destination.EPISODE)!!)

    companion object {
        fun provideFactory(
            episodeStore: EpisodeStore = Graph.episodeStore,
            podcastStore: PodcastStore = Graph.podcastStore,
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
                    return EpisodeViewModel(
                        episodeStore,
                        podcastStore,
                        handle
                    ) as T
                }
            }
    }
}

data class EpisodeUiState(
    val episode: Episode
)