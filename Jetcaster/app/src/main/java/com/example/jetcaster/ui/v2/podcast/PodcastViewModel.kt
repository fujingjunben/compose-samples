package com.example.jetcaster.ui.v2.podcast

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.jetcaster.Graph
import com.example.jetcaster.data.EpisodeStore
import com.example.jetcaster.data.Podcast
import com.example.jetcaster.data.PodcastStore
import com.example.jetcaster.ui.v2.Destination

class PodcastViewModel(
    val episodeStore: EpisodeStore,
    val podcastStore: PodcastStore,
    savedStateHandle: SavedStateHandle,
): ViewModel(){
    private val podcastUrI = Uri.decode(savedStateHandle.get<String>(Destination.PODCAST))

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
                    return PodcastViewModel(
                        episodeStore,
                        podcastStore,
                        handle
                    ) as T
                }
            }
    }
}

data class PodcastUiState(
    val podcast: Podcast
)