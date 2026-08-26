package com.example.dartx.ui.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.dartx.R
import com.example.dartx.ui.theme.Ground
import com.example.dartx.ui.theme.GreenBright
import com.example.dartx.ui.theme.TextPrimary

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
                painter = painterResource(R.drawable.ic_launcher_monochrome),
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
                    label = "PLAY",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onPlay
                )
                SecondaryButton(
                    label = "Players",
                    modifier = Modifier.fillMaxWidth(),
                    trailingChevron = true,
                    onClick = onPlayers
                )
                SecondaryButton(
                    label = "Statistics",
                    modifier = Modifier.fillMaxWidth(),
                    trailingChevron = true,
                    onClick = onStatistics
                )
                SecondaryButton(
                    label = "Match history",
                    modifier = Modifier.fillMaxWidth(),
                    trailingChevron = true,
                    onClick = onHistory
                )
            }
        }
    }
}
