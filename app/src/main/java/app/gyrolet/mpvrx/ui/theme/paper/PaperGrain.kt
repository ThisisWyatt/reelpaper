/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.theme.paper

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Global paper-grain overlay, the counterpart of the mockup's `.screen::after`
 * fractal-noise layer (7% light / 9% dark). One seamless grayscale tile is
 * generated once per process and repeated 1:1 across the whole surface.
 */
@Composable
fun PaperGrainOverlay(modifier: Modifier = Modifier) {
  val colors = LocalPaperColors.current
  val tile = remember { grainTile }
  Box(
    modifier
      .fillMaxSize()
      .drawWithCache {
        onDrawWithContent {
          drawContent()
          drawGrain(tile, colors.grainAlpha)
        }
      },
  )
}

private fun DrawScope.drawGrain(tile: ImageBitmap, alpha: Float) {
  val tileWidth = tile.width.toFloat()
  val tileHeight = tile.height.toFloat()
  var y = 0f
  while (y < size.height) {
    var x = 0f
    while (x < size.width) {
      drawImage(image = tile, topLeft = androidx.compose.ui.geometry.Offset(x, y), alpha = alpha)
      x += tileWidth
    }
    y += tileHeight
  }
}

private val grainTile: ImageBitmap by lazy {
  val size = 280
  val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
  val pixels = IntArray(size * size)
  val rnd = java.util.Random(0x5EED1E)
  // Two octaves of tileable value noise approximate SVG feTurbulence fractalNoise.
  val coarse = FloatArray(GRID * GRID) { rnd.nextFloat() }
  val fine = FloatArray(FINE_GRID * FINE_GRID) { rnd.nextFloat() }
  for (y in 0 until size) {
    for (x in 0 until size) {
      val u = x.toFloat() / size
      val v = y.toFloat() / size
      val value = 0.62f * valueNoise(coarse, GRID, u, v) + 0.38f * valueNoise(fine, FINE_GRID, u, v)
      val gray = (value * 255f).toInt().coerceIn(0, 255)
      pixels[y * size + x] = 0xFF000000.toInt() or (gray shl 16) or (gray shl 8) or gray
    }
  }
  bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
  bitmap.asImageBitmap()
}

private const val GRID = 7
private const val FINE_GRID = 15

private fun valueNoise(grid: FloatArray, gw: Int, u: Float, v: Float): Float {
  val fx = u * gw
  val fy = v * gw
  val x0 = fx.toInt().coerceIn(0, gw - 1)
  val y0 = fy.toInt().coerceIn(0, gw - 1)
  // Wrap the neighbour indices so the tile repeats without a seam.
  val x1 = (x0 + 1) % gw
  val y1 = (y0 + 1) % gw
  val tx = smooth(fx - x0)
  val ty = smooth(fy - y0)
  val a = grid[y0 * gw + x0]
  val b = grid[y0 * gw + x1]
  val c = grid[y1 * gw + x0]
  val d = grid[y1 * gw + x1]
  val top = a + (b - a) * tx
  val bottom = c + (d - c) * tx
  return top + (bottom - top) * ty
}

private fun smooth(t: Float) = t * t * (3f - 2f * t)
