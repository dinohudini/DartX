package com.example.dartx.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onPlay: () -> Unit, onPlayers: () -> Unit, onHistory: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "DartX", style = MaterialTheme.typography.displayMedium)

            Spacer(modifier = Modifier.height(48.dp))

            Button(onClick = onPlay, modifier = Modifier.fillMaxWidth()) {
                Text("Play")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(onClick = onPlayers, modifier = Modifier.fillMaxWidth()) {
                Text("Players")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth()) {
                Text("Match history")
            }
        }
    }
}
