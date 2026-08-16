package com.kcmitch.v2p.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = ThemeConfig.isDarkTheme,
  // Dynamic color is available on Android 12+ but disabled to force theme choice
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val darkColorScheme = darkColorScheme(
    primary = TechCyan,
    secondary = CoolGrey,
    tertiary = WarningAmber,
    background = DarkSlateBg,
    surface = SlateCard,
    onBackground = TextLight,
    onSurface = TextLight,
    onPrimary = Color(0xFF22282A),
    onSecondary = Color(0xFF1B2021),
    onTertiary = Color.White,
    surfaceVariant = SlateCardHeader,
    outline = BorderSlate
  )

  val lightColorScheme = lightColorScheme(
    primary = TechCyan,
    secondary = CoolGrey,
    tertiary = WarningAmber,
    background = DarkSlateBg,
    surface = SlateCard,
    onBackground = TextLight,
    onSurface = TextLight,
    onPrimary = Color.White,
    onSecondary = Color(0xFFF1F5F9),
    onTertiary = Color.White,
    surfaceVariant = SlateCardHeader,
    outline = BorderSlate
  )

  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> darkColorScheme
      else -> lightColorScheme
    }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
      }
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
