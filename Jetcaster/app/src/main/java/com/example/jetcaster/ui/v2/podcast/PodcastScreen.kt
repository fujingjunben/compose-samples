package com.example.jetcaster.ui.v2.podcast

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jetcaster.R
import com.example.jetcaster.data.Podcast
import com.example.jetcaster.ui.v2.common.EpisodeList

@Composable
fun PodcastScreen(
    onBackPress: () -> Unit,
    navigateToEpisode: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PodcastViewModel = viewModel()
) {

    val uiState = viewModel.uiState
    val appBarColor = MaterialTheme.colors.surface.copy(alpha = 0.87f)
    Column(modifier = modifier.systemBarsPadding()) {
        PodcastAppBar(
            backgroundColor = appBarColor,
            modifier = Modifier.fillMaxWidth(),
            onBackPress
        )
        PodcastInfo(uiState.podcast)
        EpisodeList(episodes = uiState.episodeOfPodcasts, navigateToEpisode = navigateToEpisode)
    }
}

@Composable
fun PodcastAppBar(
    backgroundColor: Color,
    modifier: Modifier,
    onBackPress: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            IconButton(onClick = onBackPress) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back)
                )
            }
        },
        backgroundColor = backgroundColor
    )
}

@Composable
fun PodcastInfo(podcast: Podcast?) {

}