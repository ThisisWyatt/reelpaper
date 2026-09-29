/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

// ============================================================================
// reelPaper Typography
//
// Headlines / Titles: Serif (elegant, editorial)
// Body / Labels: Sans-serif (clean, readable)
// Numbers / Durations: Tabular nums (monospaced digits)
// ============================================================================

private val ReelPaperSerif = FontFamily.Serif
private val ReelPaperSans = FontFamily.SansSerif

private val TabularNums = TextStyle(
  fontVariant = androidx.compose.ui.text.font.FontVariant(
    tabularNums = true,
  ),
)

/**
 * reelPaper Typography: serif for display/headlines, sans-serif for body.
 * Used when the Default (reelPaper) theme is active.
 */
val ReelPaperTypography = Typography(
  // Display — largest text, serif for editorial impact
  displayLarge = TextStyle(
    fontFamily = ReelPaperSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 57.sp,
    lineHeight = 64.sp,
    letterSpacing = (-0.25).sp,
  ),
  displayMedium = TextStyle(
    fontFamily = ReelPaperSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 45.sp,
    lineHeight = 52.sp,
    letterSpacing = 0.sp,
  ),
  displaySmall = TextStyle(
    fontFamily = ReelPaperSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    letterSpacing = 0.sp,
  ),

  // Headlines — section titles, also serif
  headlineLarge = TextStyle(
    fontFamily = ReelPaperSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = ReelPaperSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.sp,
  ),
  headlineSmall = TextStyle(
    fontFamily = ReelPaperSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp,
  ),

  // Titles — can be serif for emphasis or sans for UI
  titleLarge = TextStyle(
    fontFamily = ReelPaperSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp,
  ),
  titleMedium = TextStyle(
    fontFamily = ReelPaperSans,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp,
  ),
  titleSmall = TextStyle(
    fontFamily = ReelPaperSans,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
  ),

  // Body — sans-serif for readability
  bodyLarge = TextStyle(
    fontFamily = ReelPaperSans,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = ReelPaperSans,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp,
  ),
  bodySmall = TextStyle(
    fontFamily = ReelPaperSans,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
  ),

  // Labels — small UI text, sans-serif
  labelLarge = TextStyle(
    fontFamily = ReelPaperSans,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
  ),
  labelMedium = TextStyle(
    fontFamily = ReelPaperSans,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
  ),
  labelSmall = TextStyle(
    fontFamily = ReelPaperSans,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
  ),
)

// Convenience styles for reelPaper-specific UI elements

/** Logo style: serif italic bold — "reelPaper" */
val LogoTextStyle = TextStyle(
  fontFamily = ReelPaperSerif,
  fontWeight = FontWeight.Bold,
  fontStyle = FontStyle.Italic,
  fontSize = 24.sp,
  lineHeight = 32.sp,
  letterSpacing = 0.sp,
)

/** Section header: small caps feel with wide tracking */
val SectionLabelStyle = TextStyle(
  fontFamily = ReelPaperSans,
  fontWeight = FontWeight.Bold,
  fontSize = 12.5.sp,
  lineHeight = 16.sp,
  letterSpacing = 3.sp,
)

/** Duration / timecode: tabular numbers for alignment */
val DurationTextStyle = TextStyle(
  fontFamily = ReelPaperSans,
  fontWeight = FontWeight.Medium,
  fontSize = 12.sp,
  lineHeight = 16.sp,
  letterSpacing = 0.25.sp,
  fontVariant = androidx.compose.ui.text.font.FontVariant(tabularNums = true),
)

/** Button text: medium weight with wide letter spacing */
val ButtonTextStyle = TextStyle(
  fontFamily = ReelPaperSans,
  fontWeight = FontWeight.Medium,
  fontSize = 15.5.sp,
  lineHeight = 20.sp,
  letterSpacing = 3.sp,
)
