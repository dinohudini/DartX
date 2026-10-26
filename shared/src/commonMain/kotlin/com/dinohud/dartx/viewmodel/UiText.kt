package com.dinohud.dartx.viewmodel

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {
    data class Plain(val value: String) : UiText
    data class Resource(val res: StringResource, val args: List<Any> = emptyList()) : UiText
}

fun uiText(res: StringResource, vararg args: Any): UiText = UiText.Resource(res, args.toList())

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Plain -> value
    is UiText.Resource -> stringResource(
        res,
        *args.map { if (it is UiText) it.asString() else it }.toTypedArray()
    )
}
