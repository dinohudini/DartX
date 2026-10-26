package com.dinohud.dartx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dinohud.dartx.ui.theme.RedBright
import com.dinohud.dartx.ui.theme.Stroke
import com.dinohud.dartx.ui.theme.SurfaceRaised
import com.dinohud.dartx.ui.theme.TextSecondary

private val DialogShape = RoundedCornerShape(24.dp)

@Composable
internal fun ConfirmDialog(
    title: String,
    detail: String,
    dismissLabel: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(DialogShape)
                .background(SurfaceRaised)
                .border(1.dp, Stroke, DialogShape)
                .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = RedBright,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Key(
                    label = dismissLabel,
                    numeric = false,
                    tone = KeyTone.PRIMARY,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss
                )
                Key(
                    label = confirmLabel,
                    numeric = false,
                    tone = KeyTone.QUIET,
                    height = 48.dp,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onConfirm
                )
            }
        }
    }
}
