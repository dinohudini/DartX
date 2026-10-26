package com.dinohud.dartx.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dinohud.dartx.resources.Res
import com.dinohud.dartx.resources.player_stats_empty_detail
import com.dinohud.dartx.resources.player_stats_empty_title
import com.dinohud.dartx.resources.player_stats_missing
import com.dinohud.dartx.resources.statistics_title
import com.dinohud.dartx.ui.theme.Green
import com.dinohud.dartx.ui.theme.TextSecondary
import com.dinohud.dartx.viewmodel.PlayerStatsViewModel
import com.dinohud.dartx.viewmodel.StatSectionStyle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PlayerStatsScreen(
    playerId: Long,
    onBack: () -> Unit,
    viewModel: PlayerStatsViewModel = koinViewModel { parametersOf(playerId) }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(title = state.name ?: stringResource(Res.string.statistics_title), onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Green
                )

                state.name == null -> Text(
                    text = stringResource(Res.string.player_stats_missing),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Center)
                )

                !state.hasPlayed -> EmptyState(
                    title = stringResource(Res.string.player_stats_empty_title),
                    detail = stringResource(Res.string.player_stats_empty_detail),
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        PlayerHeading(
                            name = state.name.orEmpty(),
                            avatarColor = state.avatarColor
                        )
                    }
                    items(state.sections) { section ->
                        if (section.style == StatSectionStyle.BASKETS) {
                            BasketRow(section)
                        } else {
                            StatSectionCard(section)
                        }
                    }
                }
            }
        }
    }
}
