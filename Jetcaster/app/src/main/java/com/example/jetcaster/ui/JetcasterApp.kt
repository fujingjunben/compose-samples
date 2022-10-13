package com.example.jetcaster.ui.v2

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.rounded.PauseCircleFilled
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jetcaster.R
import com.example.jetcaster.play.*
import com.example.jetcaster.ui.theme.JetcasterTheme
import com.example.jetcaster.ui.v2.playerBar.PlayerBar
import java.util.*

@Composable
fun PodcastApp() {
    val tabs = remember {
        Tabs.values()
    }
    val navController = rememberNavController()
    JetcasterTheme {
        Scaffold(
            bottomBar = { PodcastBottomBar(navController, tabs = tabs) }
        ) { paddingValues ->
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun PodcastBottomBar(navController: NavController, tabs: Array<Tabs>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
        ?: Tabs.EXPLORE.route

    val routes = remember { Tabs.values().map { it.route } }
    if (currentRoute in routes) {
        Column {
            PlayerBar()
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