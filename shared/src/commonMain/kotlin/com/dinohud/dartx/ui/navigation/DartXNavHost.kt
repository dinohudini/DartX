package com.dinohud.dartx.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.dinohud.dartx.ui.screens.HomeScreen
import com.dinohud.dartx.ui.screens.LiveMatchScreen
import com.dinohud.dartx.ui.screens.MatchHistoryScreen
import com.dinohud.dartx.ui.screens.MatchSettingsScreen
import com.dinohud.dartx.ui.screens.MatchSetupScreen
import com.dinohud.dartx.ui.screens.MatchStatsScreen
import com.dinohud.dartx.ui.screens.PlayerStatsScreen
import com.dinohud.dartx.ui.screens.PlayersScreen
import com.dinohud.dartx.ui.screens.StatisticsScreen
import com.dinohud.dartx.viewmodel.LiveMatchViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data object HomeRoute

@Serializable
data object PlayersRoute

@Serializable
data object HistoryRoute

@Serializable
data object SetupRoute

@Serializable
data object StatisticsRoute

@Serializable
data class PlayerStatsRoute(val playerId: Long)

@Serializable
data class LiveMatchRoute(val matchId: Long)

@Serializable
data class MatchSettingsRoute(val matchId: Long)

@Serializable
data class MatchStatsRoute(val matchId: Long)

@Composable
fun DartXNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onPlay = { navController.navigate(SetupRoute) },
                onPlayers = { navController.navigate(PlayersRoute) },
                onStatistics = { navController.navigate(StatisticsRoute) },
                onHistory = { navController.navigate(HistoryRoute) }
            )
        }

        composable<PlayersRoute> {
            PlayersScreen(onBack = { navController.popBackStack() })
        }

        composable<StatisticsRoute> {
            StatisticsScreen(
                onBack = { navController.popBackStack() },
                onSelectPlayer = { playerId -> navController.navigate(PlayerStatsRoute(playerId)) }
            )
        }

        composable<PlayerStatsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<PlayerStatsRoute>()
            PlayerStatsScreen(playerId = route.playerId, onBack = { navController.popBackStack() })
        }

        composable<HistoryRoute> {
            MatchHistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenStats = { matchId -> navController.navigate(MatchStatsRoute(matchId)) }
            )
        }

        composable<SetupRoute> {
            MatchSetupScreen(
                onBack = { navController.popBackStack() },
                onMatchCreated = { matchId ->
                    navController.navigate(LiveMatchRoute(matchId)) {
                        popUpTo<SetupRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<LiveMatchRoute> { backStackEntry ->
            val matchId = backStackEntry.toRoute<LiveMatchRoute>().matchId
            LiveMatchScreen(
                matchId = matchId,
                onExit = {
                    navController.navigate(HomeRoute) {
                        popUpTo<HomeRoute> { inclusive = true }
                    }
                },
                onOpenSettings = { navController.navigate(MatchSettingsRoute(matchId)) },
                onViewStats = { navController.navigate(MatchStatsRoute(matchId)) }
            )
        }

        composable<MatchStatsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<MatchStatsRoute>()
            MatchStatsScreen(matchId = route.matchId, onBack = { navController.popBackStack() })
        }

        composable<MatchSettingsRoute> { backStackEntry ->
            val matchId = backStackEntry.toRoute<MatchSettingsRoute>().matchId
            val matchEntry = remember(backStackEntry) {
                navController.getBackStackEntry<LiveMatchRoute>()
            }
            MatchSettingsScreen(
                viewModel = koinViewModel<LiveMatchViewModel>(viewModelStoreOwner = matchEntry) {
                    parametersOf(matchId)
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
