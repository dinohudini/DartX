package com.dinohud.dartx

import androidx.compose.runtime.Composable
import com.dinohud.dartx.ui.navigation.DartXNavHost
import com.dinohud.dartx.ui.theme.DartXTheme

@Composable
fun DartXApp() {
    DartXTheme {
        DartXNavHost()
    }
}
