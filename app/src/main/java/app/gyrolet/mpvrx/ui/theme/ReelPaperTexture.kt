/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.theme

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlin.random.Random

// Default grain opacity: 7% light, 9% dark
private const val GrainOpacityLight = 0.07f
private const val GrainOpacityDark = 0.09f

// Grain bitmap size (power of 2 for efficient tiling)
private const val GrainSize = 128

/**
 * Creates a tileable grain/noise bitmap programmatically.
 */
private fun createGrainBitmap(seed: Int = 42): Bitmap {
  val bitmap = Bitmap.createBitmap(GrainSize, GrainSize, Bitmap.Config.ALPHA_8)
  val canvas = Canvas(bitmap)
  val paint = Paint().apply { isAntiAlias = false }
  val random = Random(seed)

  for (x in 0 until GrainSize) {
    for (y in 0 until GrainSize) {
      val alpha = random.nextInt(0, 40) // 0-40 alpha range
      if (alpha > 5) {
        paint.alpha = alpha
        canvas.drawPoint(x.toFloat(), y.toFloat(), paint)
      }
    }
  }
  return bitmap
}

/**
 * Applies a paper grain texture overlay + top radial sheen.
 *
 * @param isDark Whether dark mode is active (affects grain opacity)
 * @param showSheen Whether to show the top radial light sheen (paper reflection)
 */
fun Modifier.paperGrainBackground(
  isDark: Boolean = false,
  showSheen: Boolean = true,
): Modifier = drawBehind {
  // Grain texture overlay
  val grainOpacity = if (isDark) GrainOpacityDark else GrainOpacityLight
  val grainColor = if (isDark) Color.White.copy(alpha = grainOpacity)
  else Color.Black.copy(alpha = grainOpacity)

  // Use a deterministic random sequence for consistent grain pattern
  val random = Random(42)

  // Draw grain as scattered small dots
  val dotCount = (size.width * size.height / 400).toInt().coerceAtMost(3000)
  for (i in 0 until dotCount) {
    val x = random.nextFloat() * size.width
    val y = random.nextFloat() * size.height
    val radius = random.nextFloat() * 0.6f + 0.2f
    val alpha = random.nextFloat() * grainOpacity
    drawCircle(
      color = grainColor.copy(alpha = alpha),
      radius = radius,
      center = androidx.compose.ui.geometry.Offset(x, y),
    )
  }

  // Top radial sheen (paper reflection)
  if (showSheen) {
    drawRect(
      brush = Brush.radialGradient(
        colors = listOf(
          Color.White.copy(alpha = if (isDark) 0.03f else 0.05f),
          Color.Transparent,
        ),
        center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, 0f),
        radius = size.width * 0.8f,
      ),
      size = size,
    )
  }
}

/**
 * Composable version that auto-detects dark mode from system or theme.
 */
@Composable
fun rememberPaperGrainModifier(isDark: Boolean? = null): Modifier {
  val context = LocalContext.current
  val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
  val dark = isDark ?: systemDark

  return remember(dark) {
    Modifier.paperGrainBackground(isDark = dark)
  }
}
