package com.example.dartx.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dartx.data.local.Player
import com.example.dartx.ui.theme.TextFaint
import com.example.dartx.ui.theme.TextPrimary
import com.example.dartx.viewmodel.PlayerViewModel
import com.example.dartx.viewmodel.PlayerViewModelFactory

@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    onSelectPlayer: (Long) -> Unit,
    viewModel: PlayerViewModel = viewModel(factory = PlayerViewModelFactory(LocalContext.current))
) {
    val players by viewModel.players.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(title = "Statistics", onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (players.isEmpty()) {
                EmptyState(
                    title = "No players yet",
                    detail = "Add a player before there is anything to measure.",
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
