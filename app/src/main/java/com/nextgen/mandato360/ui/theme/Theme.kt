package com.nextgen.mandato360.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = TealMandato,
    secondary = BlueMandato,
    background = White,
    onPrimary = White,
    onBackground = Black
)

@Composable
fun Mandato360Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography(),
        content = content
    )
}