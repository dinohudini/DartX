package com.example.dartx.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dartx.ui.screens.HomeScreen
import com.example.dartx.ui.screens.LiveMatchScreen
import com.example.dartx.ui.screens.MatchSetupScreen
import com.example.dartx.ui.screens.PlayersScreen

object Routes {
    const val HOME = "home"
    const val PLAYERS = "players"
    const val SETUP = "setup"
    const val MATCH_ID_ARG = "matchId"
    const val LIVE_MATCH = "match/{$MATCH_ID_ARG}"

    fun liveMatch(matchId: Long) = "match/$matchId"
}

@Composable
fun DartXNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onPlay = { navController.navigate(Routes.SETUP) },
                onPlayers = { navController.navigate(Routes.PLAYERS) }
            )
        }

        composable(Routes.PLAYERS) {
            PlayersScreen(onBack = { navController.popBackStack() })
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
                }
            )
        }
    }
}
