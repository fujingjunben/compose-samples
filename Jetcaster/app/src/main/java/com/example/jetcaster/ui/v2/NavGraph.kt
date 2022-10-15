package com.example.jetcaster.ui.v2

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.jetcaster.R
import com.example.jetcaster.play.PlayerAction
import com.example.jetcaster.ui.JetcasterAppState
import com.example.jetcaster.ui.Screen
import com.example.jetcaster.ui.player.PlayerScreen
import com.example.jetcaster.ui.player.PlayerViewModel
import com.example.jetcaster.ui.rememberJetcasterAppState
import com.example.jetcaster.ui.v2.explore.Explore
import com.example.jetcaster.ui.v2.favourite.Favourite
import com.example.jetcaster.ui.v2.manage.Manage

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier,
    onPlayerChange: (playerAction: PlayerAction) -> Unit,
    finishActivity: () -> Unit = {},
    appState: JetcasterAppState = rememberJetcasterAppState(navController = navController)
) {
    NavHost(navController = navController, startDestination = Destination.EXPLORE_ROUTE) {
        composable(Destination.FAVOURITE_ROUTE) { backStackEntry ->
            Favourite(
                navigateToPlayer = { episodeUri ->
                    appState.navigateToPlayer(episodeUri, backStackEntry)
                }
            )
        }
        composable(Destination.EXPLORE_ROUTE) { backStackEntry ->
            BackHandler {
                finishActivity()
            }

            Explore(
                modifier = modifier,
                navigateToPlayer = { episodeUri ->
                    appState.navigateToPlayer(episodeUri, backStackEntry)
                }
            )
        }

        composable(Screen.Player.route) { backStackEntry ->
            val playerViewModel: PlayerViewModel = viewModel(
                factory = PlayerViewModel.provideFactory(
                    owner = backStackEntry,
                    defaultArgs = backStackEntry.arguments
                )
            )
            PlayerScreen(playerViewModel, onBackPress = appState::navigateBack, onPlayerChange)
        }
        composable(Destination.MANAGE_ROUTE) {
            Manage()
        }
    }
}

enum class Tabs(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val route: String
) {
    FAVOURITE(R.string.favourite, R.drawable.ic_grain, Destination.FAVOURITE_ROUTE),
    EXPLORE(R.string.explore, R.drawable.ic_search, Destination.EXPLORE_ROUTE),
    MANAGE(R.string.manage, R.drawable.ic_featured, Destination.MANAGE_ROUTE),
}

object Destination {
    const val FAVOURITE_ROUTE = "favourite"
    const val EXPLORE_ROUTE = "explore"
    const val MANAGE_ROUTE = "manage"
}