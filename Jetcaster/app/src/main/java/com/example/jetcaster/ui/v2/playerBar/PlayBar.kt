package com.example.jetcaster.ui.v2.playerBar

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PauseCircleFilled
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.jetcaster.play.PlayerState
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jetcaster.R
import com.example.jetcaster.data.EpisodeToPodcast
import com.example.jetcaster.play.Pause
import com.example.jetcaster.play.Playing
import com.example.jetcaster.play.Ready

@Composable
fun PlayerBar(
    playerState: PlayerState,
    modifier: Modifier = Modifier,
    viewModel: PlayerBarViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is PlayerBarUiState.Loading -> {
        }

        is PlayerBarUiState.Success -> {
            PlayerBarContent(
                modifier,
                (uiState as PlayerBarUiState.Success).episodeToPodcast,
                playerState,
                viewModel::play
            )
        }
    }
}

@Composable
fun PlayerBarContent(
    modifier: Modifier,
    episodeToPodcast: EpisodeToPodcast,
    playerState: PlayerState,
    play: (playerState: PlayerState) -> PlayerState
) {
    var state by remember {
        mutableStateOf(playerState)
    }

    val icon = when (state) {
        is Playing -> Icons.Rounded.PauseCircleFilled
        else -> Icons.Rounded.PlayCircleFilled
    }

    val (episode, podcast) = episodeToPodcast
    Surface {
        Row(
            modifier = modifier
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // If we have an image Url, we can show it using Coil
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(podcast.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .padding(5.dp)
            )

            Text(
                text = episode.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier.weight(1f)
            )

            Image(
                imageVector = icon,
                contentDescription = stringResource(R.string.cd_play),
                contentScale = ContentScale.FillHeight,
                colorFilter = ColorFilter.tint(LocalContentColor.current),
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = false, radius = 24.dp)
                    ) { state = play(state) }
                    .padding(end = 5.dp)
            )

        }
    }
}