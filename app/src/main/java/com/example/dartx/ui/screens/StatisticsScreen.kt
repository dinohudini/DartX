package com.example.dartx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dartx.data.local.Player
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
        topBar = { BackTopBar(title = "Statistics", onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (players.isEmpty()) {
                Text(
                    text = "Add a player first.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(parseHexColor(player.avatarColor))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = player.name, style = MaterialTheme.typography.titleMedium)
        }
    }
}
