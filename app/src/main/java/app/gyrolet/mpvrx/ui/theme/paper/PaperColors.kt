/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.theme.paper

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * reelPaper design tokens — 纸 · 墨 · 朱砂.
 *
 * Extracted from the approved mockup `docs/新需求文档/UI改造/reelPaper-UI-V8.html`
 * (`.screen` / `.screen.dark` / `.screen.dark.amoled` CSS variables). Keep the
 * values in sync with that file only; do not hand-tune single tokens.
 */
data class PaperColors(
  // 纸面 (page background)
  val paper: Color,
  val paperInset: Color,
  // 卡纸 (raised card surface)
  val card: Color,
  val cardLine: Color,
  // 墨 (text)
  val ink: Color,
  val inkSecondary: Color,
  val inkTertiary: Color,
  // 分隔线
  val line: Color,
  // 朱砂 (primary accent)
  val red: Color,
  val redHighlight: Color,
  val redDark: Color,
  // 苔绿 (success / positive)
  val green: Color,
  // 暖白: text/icons placed on 朱砂 surfaces
  val onRed: Color,
  // paper grain overlay opacity
  val grainAlpha: Float,
  // elevation tint used by raised-card drop shadows (warm brown)
  val shadowTint: Color,
  // top inner highlight drawn on raised cards
  val highlight: Color,
  val isDark: Boolean,
)

val PaperPaletteLight = PaperColors(
  paper = Color(0xFFF3EDE0),
  paperInset = Color(0xFFEAE2CD),
  card = Color(0xFFFBF7EC),
  cardLine = Color(0xFFE6DCC3),
  ink = Color(0xFF3C362C),
  inkSecondary = Color(0xFF8F8471),
  inkTertiary = Color(0xFFB7AC92),
  line = Color(0xFFE4DBC5),
  red = Color(0xFFB23129),
  redHighlight = Color(0xFFD65444),
  redDark = Color(0xFF7E211B),
  green = Color(0xFF5F7A4A),
  onRed = Color(0xFFFBEFE1),
  grainAlpha = 0.07f,
  shadowTint = Color(0xFF604C24),
  highlight = Color(0xFFFFFFFF),
  isDark = false,
)

val PaperPaletteDark = PaperColors(
  paper = Color(0xFF2A2620),
  paperInset = Color(0xFF211E18),
  card = Color(0xFF343028),
  cardLine = Color(0xFF423D31),
  ink = Color(0xFFEAE2CD),
  inkSecondary = Color(0xFFA89D85),
  inkTertiary = Color(0xFF7E7663),
  line = Color(0xFF3A352B),
  red = Color(0xFFB23129),
  redHighlight = Color(0xFFD65444),
  redDark = Color(0xFF7E211B),
  green = Color(0xFF7B9460),
  onRed = Color(0xFFFBEFE1),
  grainAlpha = 0.09f,
  shadowTint = Color(0xFF000000),
  highlight = Color(0x14FFFFFF),
  isDark = true,
)

private val PaperPaletteAmoled = PaperPaletteDark.copy(
  paper = Color(0xFF050505),
  paperInset = Color(0xFF101010),
  card = Color(0xFF0B0B0B),
  cardLine = Color(0xFF2A2A2A),
  line = Color(0xFF1D1D1D),
)

fun paperPalette(
  useDarkTheme: Boolean,
  amoledMode: Boolean,
): PaperColors = when {
  useDarkTheme && amoledMode -> PaperPaletteAmoled
  useDarkTheme -> PaperPaletteDark
  else -> PaperPaletteLight
}

/**
 * Maps the paper palette onto a Material 3 [ColorScheme] so every screen that has
 * not been restyled yet still follows the paper ink palette automatically.
 */
fun paperColorScheme(c: PaperColors): ColorScheme {
  val base =
    if (c.isDark) {
      androidx.compose.material3.darkColorScheme()
    } else {
      androidx.compose.material3.lightColorScheme()
    }
  return base.copy(
    primary = if (c.isDark) c.redHighlight else c.red,
    onPrimary = c.onRed,
    primaryContainer = if (c.isDark) c.redDark else Color(0xFFF6D8D3),
    onPrimaryContainer = if (c.isDark) Color(0xFFF6D8D3) else c.redDark,
    secondary = c.inkSecondary,
    onSecondary = c.onRed,
    secondaryContainer = c.paperInset,
    onSecondaryContainer = c.ink,
    tertiary = c.green,
    onTertiary = c.onRed,
    tertiaryContainer = if (c.isDark) Color(0xFF3C4A2E) else Color(0xFFE2EAD6),
    onTertiaryContainer = if (c.isDark) Color(0xFFD6E4C4) else Color(0xFF3E5230),
    background = c.paper,
    onBackground = c.ink,
    surface = c.paper,
    onSurface = c.ink,
    surfaceVariant = c.paperInset,
    onSurfaceVariant = c.inkSecondary,
    surfaceTint = c.red,
    inverseSurface = c.ink,
    inverseOnSurface = c.paper,
    inversePrimary = c.redHighlight,
    error = if (c.isDark) Color(0xFFFFB4AB) else Color(0xFFB3261E),
    onError = if (c.isDark) Color(0xFF690005) else Color(0xFFFFFFFF),
    errorContainer = if (c.isDark) Color(0xFF93000A) else Color(0xFFF9DEDC),
    onErrorContainer = if (c.isDark) Color(0xFFFFDAD6) else Color(0xFF410E0B),
    outline = c.inkTertiary,
    outlineVariant = c.line,
    scrim = Color(0x66000000),
    surfaceBright = if (c.isDark) Color(0xFF3C362C) else Color(0xFFFBF7EC),
    surfaceDim = if (c.isDark) c.paper else c.paperInset,
    surfaceContainerLowest = c.paper,
    surfaceContainerLow = c.card,
    surfaceContainer = c.card,
    surfaceContainerHigh = c.card,
    surfaceContainerHighest = if (c.isDark) c.cardLine else c.paperInset,
  )
}

val LocalPaperColors = staticCompositionLocalOf { PaperPaletteLight }
