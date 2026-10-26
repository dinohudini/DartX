package com.dinohud.dartx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dinohud.dartx.data.local.Player
import com.dinohud.dartx.resources.Res
import com.dinohud.dartx.resources.statistics_empty_detail
import com.dinohud.dartx.resources.statistics_empty_title
import com.dinohud.dartx.resources.statistics_title
import com.dinohud.dartx.ui.theme.TextFaint
import com.dinohud.dartx.ui.theme.TextPrimary
import com.dinohud.dartx.viewmodel.PlayerViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    onSelectPlayer: (Long) -> Unit,
    viewModel: PlayerViewModel = koinViewModel()
) {
    val players by viewModel.players.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(title = stringResource(Res.string.statistics_title), onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (players.isEmpty()) {
                EmptyState(
                    title = stringResource(Res.string.statistics_empty_title),
                    detail = stringResource(Res.string.statistics_empty_detail),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(players, key = { it.id }) { player ->
                        PlayerPickerRow(player = player, onClick = { onSelectPlayer(player.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerPickerRow(player: Player, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .panelSurface()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AvatarDot(colorHex = player.avatarColor, size = 30.dp)
        Text(
            text = player.name,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = TextFaint
        )
    }
}
