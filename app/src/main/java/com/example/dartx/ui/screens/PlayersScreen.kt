package com.example.dartx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dartx.data.local.Player
import com.example.dartx.viewmodel.PlayerViewModel
import com.example.dartx.viewmodel.PlayerViewModelFactory

private val avatarColorOptions = listOf(
    "#EF5350", "#42A5F5", "#66BB6A", "#FFA726", "#AB47BC", "#26C6DA"
)

@Composable
fun PlayersScreen(
    viewModel: PlayerViewModel = viewModel(
        factory = PlayerViewModelFactory(LocalContext.current)
    )
) {
    val players by viewModel.players.collectAsStateWithLifecycle()
    var nameInput by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(avatarColorOptions.first()) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(text = "Players", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Player name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                avatarColorOptions.forEach { colorHex ->
                    ColorDot(
                        colorHex = colorHex,
                        selected = colorHex == selectedColor,
                        onClick = { selectedColor = colorHex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.addPlayer(nameInput, selectedColor)
                    nameInput = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add player")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(players, key = { it.id }) { player ->
                    PlayerRow(player = player, onDelete = { viewModel.deletePlayer(player) })
                }
            }
        }
    }
}

@Composable
private fun ColorDot(colorHex: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(if (selected) 32.dp else 24.dp)
            .clip(CircleShape)
            .background(parseHexColor(colorHex))
            .clickable { onClick() }
    )
}

@Composable
private fun PlayerRow(player: Player, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(parseHexColor(player.avatarColor))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = player.name, style = MaterialTheme.typography.bodyLarge)
        }
        TextButton(onClick = onDelete) {
            Text("Delete")
        }
    }
}

private fun parseHexColor(hex: String): Color = Color(android.graphics.Color.parseColor(hex))
