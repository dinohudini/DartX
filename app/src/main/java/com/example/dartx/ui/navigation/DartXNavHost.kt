package com.example.dartx.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dartx.ui.screens.HomeScreen
import com.example.dartx.ui.screens.LiveMatchScreen
import com.example.dartx.ui.screens.MatchHistoryScreen
import com.example.dartx.ui.screens.MatchSettingsScreen
import com.example.dartx.ui.screens.MatchSetupScreen
import com.example.dartx.ui.screens.PlayersScreen
import com.example.dartx.viewmodel.LiveMatchViewModelFactory

object Routes {
    const val HOME = "home"
    const val PLAYERS = "players"
    const val HISTORY = "history"
    const val SETUP = "setup"
    const val MATCH_ID_ARG = "matchId"
    const val LIVE_MATCH = "match/{$MATCH_ID_ARG}"
    const val MATCH_SETTINGS = "match/{$MATCH_ID_ARG}/settings"

    fun liveMatch(matchId: Long) = "match/$matchId"

    fun matchSettings(matchId: Long) = "match/$matchId/settings"
}

@Composable
fun DartXNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onPlay = { navController.navigate(Routes.SETUP) },
                onPlayers = { navController.navigate(Routes.PLAYERS) },
                onHistory = { navController.navigate(Routes.HISTORY) }
            )
        }

        composable(Routes.PLAYERS) {
            PlayersScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.HISTORY) {
            MatchHistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETUP) {
            MatchSetupScreen(
                onBack = { navController.popBackStack() },
                onMatchCreated = { matchId ->
                    // Setup is done once per match; leaving the live screen goes back home.
                    navController.navigate(Routes.liveMatch(matchId)) {
                        popUpTo(Routes.SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.LIVE_MATCH,
            arguments = listOf(navArgument(Routes.MATCH_ID_ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong(Routes.MATCH_ID_ARG) ?: return@composable
            LiveMatchScreen(
                matchId = matchId,
                onExit = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onOpenSettings = { navController.navigate(Routes.matchSettings(matchId)) }
            )
        }

        composable(
            route = Routes.MATCH_SETTINGS,
            arguments = listOf(navArgument(Routes.MATCH_ID_ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong(Routes.MATCH_ID_ARG) ?: return@composable
            // Scope the ViewModel to the live match entry rather than to this one, so settings
            // changes reach the match in progress instead of a second, throwaway controller.
            val matchEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.liveMatch(matchId))
            }
            MatchSettingsScreen(
                viewModel = viewModel(
                    viewModelStoreOwner = matchEntry,
                    factory = LiveMatchViewModelFactory(LocalContext.current, matchId)
                ),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
