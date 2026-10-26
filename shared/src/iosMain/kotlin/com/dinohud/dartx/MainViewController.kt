package com.dinohud.dartx

import androidx.compose.ui.window.ComposeUIViewController
import com.dinohud.dartx.di.initKoin
import com.dinohud.dartx.di.iosPlatformModule
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin(iosPlatformModule)
    return ComposeUIViewController { DartXApp() }
}
