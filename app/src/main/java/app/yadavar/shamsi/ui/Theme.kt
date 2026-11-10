package app.yadavar.shamsi.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

fun accentColor(name: String): Color = when (name) {
    "blue" -> Color(0xFF1B6EF3)
    "teal" -> Color(0xFF0F9B8E)
    "amber" -> Color(0xFFC4841D)
    "rose" -> Color(0xFFD4527A)
    else -> Color(0xFF6750A4)
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun YadavarTheme(theme: String, accent: String, content: @Composable () -> Unit) {
    val dark = when (theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val primary = accentColor(accent)
    val scheme = if (dark) {
        // Selected chips and the main buttons share the accent hue.
        val tint = lerp(Color(0xFF181B22), primary, 0.40f)
        darkColorScheme(
            primary = primary,
            primaryContainer = tint,
            secondaryContainer = tint,
            onSecondaryContainer = Color.White,
            background = Color(0xFF101114),
            surface = Color(0xFF181B22),
            surfaceContainer = Color(0xFF22262F),
        )
    } else {
        val tint = lerp(Color.White, primary, 0.16f)
        lightColorScheme(
            primary = primary,
            primaryContainer = tint,
            secondaryContainer = tint,
            onSecondaryContainer = lerp(primary, Color.Black, 0.35f),
            background = Color(0xFFF4F6F8),
            surface = Color(0xFFFFFFFF),
            surfaceContainer = Color(0xFFEEF1F4),
        )
    }
    MaterialTheme(colorScheme = scheme, shapes = AppShapes, content = content)
}
