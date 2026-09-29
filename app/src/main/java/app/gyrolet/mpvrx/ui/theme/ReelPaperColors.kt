/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ============================================================================
// reelPaper Color Scheme Functions
// Maps reelPaper design tokens to Material 3 ColorScheme
// ============================================================================

/**
 * Light reelPaper color scheme.
 * Background: warm paper (#F3EDE0), Primary: cinnabar red (#B23129)
 */
fun lightReelPaperColorScheme(): ColorScheme = lightColorScheme(
  primary = rpCinnabar,
  onPrimary = rpPaper,
  primaryContainer = rpCinnabar.copy(alpha = 0.18f).compositeOver(rpPaper),
  onPrimaryContainer = rpCinnabarDk,
  secondary = rpInk2,
  onSecondary = rpPaper,
  secondaryContainer = rpInk2.copy(alpha = 0.14f).compositeOver(rpPaper),
  onSecondaryContainer = rpInk,
  tertiary = rpMossGreen,
  onTertiary = rpPaper,
  tertiaryContainer = rpMossGreen.copy(alpha = 0.16f).compositeOver(rpPaper),
  onTertiaryContainer = Color(0xFF3A4D2E),
  error = rpCinnabarHi,
  onError = rpPaper,
  errorContainer = rpCinnabarHi.copy(alpha = 0.18f).compositeOver(rpPaper),
  onErrorContainer = rpCinnabarDk,
  background = rpPaper,
  onBackground = rpInk,
  surface = rpCardPaper,
  onSurface = rpInk,
  surfaceVariant = rpPaperInset,
  onSurfaceVariant = rpInk2,
  outline = rpLine,
  outlineVariant = rpCardLine,
  scrim = Color(0xFF342A18).copy(alpha = 0.45f),
  inverseSurface = rpDarkPaper,
  inverseOnSurface = rpDarkInk,
  inversePrimary = rpCinnabarHi,
  surfaceDim = rpPaperInset,
  surfaceBright = Color(0xFFFAF6ED),
  surfaceContainerLowest = Color(0xFFFFFFFF),
  surfaceContainerLow = rpCardPaper,
  surfaceContainer = rpPaper,
  surfaceContainerHigh = rpCardPaper,
  surfaceContainerHighest = rpPaperInset,
)

/**
 * Dark reelPaper color scheme.
 * Background: dark warm paper (#2A2620), Primary: bright cinnabar (#D65444)
 */
fun darkReelPaperColorScheme(): ColorScheme = darkColorScheme(
  primary = rpCinnabarHi,
  onPrimary = rpPaper,
  primaryContainer = rpCinnabar.copy(alpha = 0.30f).compositeOver(rpDarkPaper),
  onPrimaryContainer = rpCinnabarHi,
  secondary = rpDarkInk2,
  onSecondary = rpDarkPaper,
  secondaryContainer = rpDarkInk2.copy(alpha = 0.20f).compositeOver(rpDarkPaper),
  onSecondaryContainer = rpDarkInk,
  tertiary = rpMossGreen,
  onTertiary = rpDarkPaper,
  tertiaryContainer = rpMossGreen.copy(alpha = 0.22f).compositeOver(rpDarkPaper),
  onTertiaryContainer = Color(0xFF7A9A65),
  error = rpCinnabarHi,
  onError = rpDarkPaper,
  errorContainer = rpCinnabar.copy(alpha = 0.25f).compositeOver(rpDarkPaper),
  onErrorContainer = rpCinnabarHi,
  background = rpDarkPaper,
  onBackground = rpDarkInk,
  surface = rpDarkCard,
  onSurface = rpDarkInk,
  surfaceVariant = rpDarkPaperInset,
  onSurfaceVariant = rpDarkInk2,
  outline = rpDarkLine,
  outlineVariant = rpDarkCardLine,
  scrim = Color(0xFF000000).copy(alpha = 0.60f),
  inverseSurface = rpPaper,
  inverseOnSurface = rpInk,
  inversePrimary = rpCinnabar,
  surfaceDim = rpDarkPaperInset,
  surfaceBright = Color(0xFF3D3731),
  surfaceContainerLowest = Color(0xFF15120E),
  surfaceContainerLow = rpDarkPaperInset,
  surfaceContainer = rpDarkCard,
  surfaceContainerHigh = Color(0xFF3E3A32),
  surfaceContainerHighest = Color(0xFF4A453C),
)

/**
 * AMOLED pure-black variant of the dark reelPaper scheme.
 * Replaces surface colors with near-black for OLED power savings.
 */
fun amoledReelPaperColorScheme(): ColorScheme = darkReelPaperColorScheme().copy(
  background = rpAmoledPaper,
  surface = rpAmoledCard,
  surfaceVariant = rpAmoledPaperInset,
  surfaceDim = rpAmoledPaper,
  surfaceBright = Color(0xFF1A1A1A),
  surfaceContainerLowest = Color.Black,
  surfaceContainerLow = rpAmoledPaperInset,
  surfaceContainer = rpAmoledCard,
  surfaceContainerHigh = Color(0xFF151515),
  surfaceContainerHighest = Color(0xFF1F1F1F),
)

// Re-export reelPaper colors for convenient access from the theme package
val Paper = rpPaper
val PaperInset = rpPaperInset
val CardPaper = rpCardPaper
val CardLine = rpCardLine
val Ink = rpInk
val Ink2 = rpInk2
val Ink3 = rpInk3
val Line = rpLine
val Cinnabar = rpCinnabar
val CinnabarHi = rpCinnabarHi
val CinnabarDk = rpCinnabarDk
val MossGreen = rpMossGreen

val DarkPaper = rpDarkPaper
val DarkPaperInset = rpDarkPaperInset
val DarkCard = rpDarkCard
val DarkCardLine = rpDarkCardLine
val DarkInk = rpDarkInk
val DarkInk2 = rpDarkInk2
val DarkInk3 = rpDarkInk3
val DarkLine = rpDarkLine

val AmoledPaper = rpAmoledPaper
val AmoledPaperInset = rpAmoledPaperInset
val AmoledCard = rpAmoledCard
val AmoledCardLine = rpAmoledCardLine
val AmoledLine = rpAmoledLine
