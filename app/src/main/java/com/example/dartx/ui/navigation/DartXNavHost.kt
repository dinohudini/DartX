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
import com.example.dartx.ui.screens.MatchStatsScreen
import com.example.dartx.ui.screens.PlayerStatsScreen
import com.example.dartx.ui.screens.PlayersScreen
import com.example.dartx.ui.screens.StatisticsScreen
import com.example.dartx.viewmodel.LiveMatchViewModelFactory

object Routes {
    const val HOME = "home"
    const val PLAYERS = "players"
    const val HISTORY = "history"
    const val SETUP = "setup"
    const val MATCH_ID_ARG = "matchId"
    const val LIVE_MATCH = "match/{$MATCH_ID_ARG}"
    const val MATCH_SETTINGS = "match/{$MATCH_ID_ARG}/settings"
    const val MATCH_STATS = "match/{$MATCH_ID_ARG}/stats"
    const val PLAYER_ID_ARG = "playerId"
    const val STATISTICS = "statistics"
    const val PLAYER_STATS = "statistics/{$PLAYER_ID_ARG}"

    fun liveMatch(matchId: Long) = "match/$matchId"

    fun matchSettings(matchId: Long) = "match/$matchId/settings"

    fun matchStats(matchId: Long) = "match/$matchId/stats"

    fun playerStats(playerId: Long) = "statistics/$playerId"
}

@Composable
fun DartXNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onPlay = { navController.navigate(Routes.SETUP) },
                onPlayers = { navController.navigate(Routes.PLAYERS) },
                onStatistics = { navController.navigate(Routes.STATISTICS) },
                onHistory = { navController.navigate(Routes.HISTORY) }
            )
        }

        composable(Routes.PLAYERS) {
            PlayersScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.STATISTICS) {
            StatisticsScreen(
                onBack = { navController.popBackStack() },
                onSelectPlayer = { playerId -> navController.navigate(Routes.playerStats(playerId)) }
            )
        }

        composable(
            route = Routes.PLAYER_STATS,
            arguments = listOf(navArgument(Routes.PLAYER_ID_ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val playerId = backStackEntry.arguments?.getLong(Routes.PLAYER_ID_ARG) ?: return@composable
            PlayerStatsScreen(playerId = playerId, onBack = { navController.popBackStack() })
        }

        composable(Routes.HISTORY) {
            MatchHistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenStats = { matchId -> navController.navigate(Routes.matchStats(matchId)) }
            )
        }

        composable(Routes.SETUP) {
            MatchSetupScreen(
                onBack = { navController.popBackStack() },
                onMatchCreated = { matchId ->
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
                onOpenSettings = { navController.navigate(Routes.matchSettings(matchId)) },
                onViewStats = { navController.navigate(Routes.matchStats(matchId)) }
            )
        }

        composable(
            route = Routes.MATCH_STATS,
            arguments = listOf(navArgument(Routes.MATCH_ID_ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong(Routes.MATCH_ID_ARG) ?: return@composable
            MatchStatsScreen(matchId = matchId, onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.MATCH_SETTINGS,
            arguments = listOf(navArgument(Routes.MATCH_ID_ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong(Routes.MATCH_ID_ARG) ?: return@composable
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
