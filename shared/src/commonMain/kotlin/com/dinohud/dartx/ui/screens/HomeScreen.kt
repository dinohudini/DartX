package com.dinohud.dartx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dinohud.dartx.resources.Res
import com.dinohud.dartx.resources.dartx_mark
import com.dinohud.dartx.resources.home_history
import com.dinohud.dartx.resources.home_play
import com.dinohud.dartx.resources.home_players
import com.dinohud.dartx.resources.home_statistics
import com.dinohud.dartx.ui.theme.GreenBright
import com.dinohud.dartx.ui.theme.Ground
import com.dinohud.dartx.ui.theme.TextPrimary
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    onPlay: () -> Unit,
    onPlayers: () -> Unit,
    onStatistics: () -> Unit,
    onHistory: () -> Unit
) {
    Scaffold(containerColor = Ground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.radialGradient(listOf(Color(0xFF17202A), Ground)))
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.dartx_mark),
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(180.dp)
            )

            Row {
                Text(
                    text = "DART",
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary
                )
                Text(
                    text = "X",
                    style = MaterialTheme.typography.displayLarge,
                    color = GreenBright
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton(
                    label = stringResource(Res.string.home_play),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onPlay
                )
                SecondaryButton(
                    label = stringResource(Res.string.home_players),
                    modifier = Modifier.fillMaxWidth(),
                    trailingChevron = true,
                    onClick = onPlayers
                )
                SecondaryButton(
                    label = stringResource(Res.string.home_statistics),
                    modifier = Modifier.fillMaxWidth(),
                    trailingChevron = true,
                    onClick = onStatistics
                )
                SecondaryButton(
                    label = stringResource(Res.string.home_history),
                    modifier = Modifier.fillMaxWidth(),
                    trailingChevron = true,
                    onClick = onHistory
                )
            }
        }
    }
}
