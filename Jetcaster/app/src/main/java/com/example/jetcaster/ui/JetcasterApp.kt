package com.example.jetcaster.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jetcaster.play.*
import com.example.jetcaster.ui.theme.JetcasterTheme
import com.example.jetcaster.ui.v2.NavGraph
import com.example.jetcaster.ui.v2.Tabs
import com.example.jetcaster.ui.v2.playerBar.PlayerBar

@Composable
fun PodcastApp() {
    val tabs = remember {
        Tabs.values()
    }
    val navController = rememberNavController()
    var playerAction by remember {
        mutableStateOf<PlayerAction>(Default)
    }
    JetcasterTheme {
        Scaffold(
            bottomBar = { PodcastBottomBar(navController, tabs = tabs, playerAction) }
        ) { paddingValues ->
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(paddingValues),
                onPlayerChange = { nextPlayerState ->
                    playerAction = nextPlayerState
                }
            )
        }
    }
}

@Composable
fun PodcastBottomBar(navController: NavController, tabs: Array<Tabs>, playerAction: PlayerAction) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
        ?: Tabs.EXPLORE.route

    val routes = remember { Tabs.values().map { it.route } }
    if (currentRoute in routes) {
        Column {
            if (playerAction != Default) {
                PlayerBar(playerAction)
            }
            BottomNavigation(
                Modifier.windowInsetsBottomHeight(
                    WindowInsets.navigationBars.add(WindowInsets(bottom = 56.dp))
                )
            ) {
                tabs.forEach { tab ->
                    BottomNavigationItem(
                        icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                        label = { Text(stringResource(tab.title)) },
                        selected = currentRoute == tab.route,
                        onClick = {
                            if (tab.route != currentRoute) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        alwaysShowLabel = true,
                        selectedContentColor = MaterialTheme.colors.secondary,
                        unselectedContentColor = LocalContentColor.current,
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
            }
        }
    }
}