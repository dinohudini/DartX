package com.example.dartx.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dartx.ui.theme.GreenBright
import com.example.dartx.ui.theme.TextFaint
import com.example.dartx.ui.theme.TextPrimary
import com.example.dartx.ui.theme.TextSecondary
import com.example.dartx.viewmodel.StatItem
import com.example.dartx.viewmodel.StatSection

@Composable
internal fun PlayerHeading(name: String, avatarColor: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AvatarDot(colorHex = avatarColor, size = 22.dp)
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )
    }
}

@Composable
internal fun StatSectionCard(section: StatSection) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .panelSurface()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionLabel(text = section.title, color = GreenBright)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            section.items.forEach { StatRow(it) }
        }
    }
}

@Composable
private fun StatRow(item: StatItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = item.value,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
    }
}

@Composable
internal fun BasketRow(section: StatSection) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(text = section.title, color = GreenBright)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            section.items.forEach { item ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(78.dp)
                        .panelSurface(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.displaySmall,
                        color = if (item.label == "180" && item.value != "0") {
                            GreenBright
                        } else {
                            TextPrimary
                        }
                    )
                    Box(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextFaint
                    )
                }
            }
        }
    }
}
