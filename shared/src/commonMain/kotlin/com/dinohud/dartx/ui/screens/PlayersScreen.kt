package com.dinohud.dartx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dinohud.dartx.resources.Res
import com.dinohud.dartx.resources.action_delete_player
import com.dinohud.dartx.resources.matches_count
import com.dinohud.dartx.resources.players_add
import com.dinohud.dartx.resources.players_empty_detail
import com.dinohud.dartx.resources.players_empty_title
import com.dinohud.dartx.resources.players_name_hint
import com.dinohud.dartx.resources.players_new
import com.dinohud.dartx.resources.players_no_matches
import com.dinohud.dartx.resources.players_record
import com.dinohud.dartx.resources.players_title
import com.dinohud.dartx.resources.wins_count
import com.dinohud.dartx.ui.theme.Green
import com.dinohud.dartx.ui.theme.StrokeFaint
import com.dinohud.dartx.ui.theme.SurfaceCard
import com.dinohud.dartx.ui.theme.SurfaceRecessed
import com.dinohud.dartx.ui.theme.TextFaint
import com.dinohud.dartx.ui.theme.TextPrimary
import com.dinohud.dartx.ui.theme.TextSecondary
import com.dinohud.dartx.viewmodel.PlayerRow
import com.dinohud.dartx.viewmodel.PlayerViewModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlayersScreen(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = koinViewModel()
) {
    val rows by viewModel.rows.collectAsStateWithLifecycle()
    var nameInput by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(avatarColorOptions.first()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(title = stringResource(Res.string.players_title), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceRecessed)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel(stringResource(Res.string.players_new))
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        placeholder = { Text(stringResource(Res.string.players_name_hint), color = TextFaint) },
                        singleLine = true,
                        shape = ControlShape,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedBorderColor = Green,
                            unfocusedBorderColor = StrokeFaint,
                            cursorColor = Green,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    avatarColorOptions.forEach { colorHex ->
                        ColorDot(
                            colorHex = colorHex,
                            selected = colorHex == selectedColor,
                            onClick = { selectedColor = colorHex }
                        )
                    }
                }

                PrimaryButton(
                    label = stringResource(Res.string.players_add),
                    enabled = nameInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    viewModel.addPlayer(nameInput, selectedColor)
                    nameInput = ""
                }
            }

            HorizontalDivider(color = StrokeFaint)

            if (rows.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = stringResource(Res.string.players_empty_title),
                        detail = stringResource(Res.string.players_empty_detail)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rows, key = { it.player.id }) { row ->
                        PlayerListRow(row = row, onDelete = { viewModel.deletePlayer(row.player) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorDot(colorHex: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .requiredSize(44.dp)
            .clip(CircleShape)
            .then(if (selected) Modifier.border(2.dp, TextPrimary, CircleShape) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 30.dp else 28.dp)
                .clip(CircleShape)
                .background(parseHexColor(colorHex))
        )
    }
}

@Composable
private fun PlayerListRow(row: PlayerRow, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .panelSurface()
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarDot(colorHex = row.player.avatarColor, size = 32.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(
                text = row.player.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = if (row.matchesPlayed == 0) {
                    stringResource(Res.string.players_no_matches)
                } else {
                    stringResource(
                        Res.string.players_record,
                        pluralStringResource(Res.plurals.matches_count, row.matchesPlayed, row.matchesPlayed),
                        pluralStringResource(Res.plurals.wins_count, row.matchesWon, row.matchesWon)
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextFaint,
                maxLines = 1
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = stringResource(Res.string.action_delete_player, row.player.name),
                tint = TextSecondary
            )
        }
    }
}
