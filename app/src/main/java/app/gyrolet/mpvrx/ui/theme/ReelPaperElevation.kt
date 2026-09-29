/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Warm brown shadow tint used across all reelPaper elevations
val ShadowTint = Color(0xFF604C24)
val PressOverlayTint = Color(0xFF786030)
private val ShadowAmbientAlpha = 0.10f
private val ShadowSpotAlpha = 0.08f
private val InsetShadowAlpha = 0.16f
private val InsetHighlightAlpha = 0.55f
private val PressOverlayAlpha = 0.07f

/**
 * ① Card raise — cards, capsules, mini keys.
 * Soft floating shadow with warm brown tint.
 */
fun Modifier.reelPaperRaise(
  shape: Shape = RectangleShape,
  elevation: Dp = 6.dp,
): Modifier = this.then(
  Modifier.drawBehind {
    // Simulate raised shadow with inset highlight + drop shadow
    val shadowColor = ShadowTint.copy(alpha = ShadowSpotAlpha)
    drawRect(
      color = shadowColor,
      topLeft = Offset(0f, elevation.toPx()),
      size = size.copy(height = size.height - elevation.toPx()),
    )
  }
)

/**
 * ② Paper inset — input slots, sliders, active tab.
 * Sunken appearance with inner shadow + top highlight.
 */
fun Modifier.reelPaperInset(
  shape: Shape = RectangleShape,
): Modifier = this.then(
  Modifier.drawBehind {
    drawInsetShadow(shape)
  }
)

private fun DrawScope.drawInsetShadow(shape: Shape) {
  // Inner shadow approximation: darker edges + top highlight
  val insetColor = ShadowTint.copy(alpha = InsetShadowAlpha)
  val highlightColor = Color.White.copy(alpha = InsetHighlightAlpha)

  // Top inner shadow
  drawRect(
    color = insetColor,
    topLeft = Offset.Zero,
    size = size.copy(height = 3.dp.toPx()),
  )

  // Bottom highlight (inverse of shadow)
  drawRect(
    color = highlightColor,
    topLeft = Offset(0f, size.height - 1.dp.toPx()),
    size = size.copy(height = 1.dp.toPx()),
  )
}

/**
 * ③ Step shadow — rectangular primary action buttons (cinnabar keys).
 * Thick uniform bottom shadow that compresses on press.
 */
fun Modifier.reelPaperStep(
  stepColor: Color = CinnabarDk,
  stepHeight: Dp = 3.dp,
): Modifier = this.then(
  Modifier.drawBehind {
    drawRect(
      color = stepColor,
      topLeft = Offset(0f, size.height),
      size = size.copy(height = stepHeight.toPx()),
    )
  }
)

/**
 * ④ Soft float — circular keys (FAB, play button).
 * Double-layer soft shadow (no step for circles).
 */
fun Modifier.reelPaperSoftFloat(
  shape: Shape = RectangleShape,
  elevation: Dp = 8.dp,
): Modifier = this.then(
  Modifier.drawBehind {
    // Outer soft shadow
    val outerShadow = ShadowTint.copy(alpha = 0.06f)
    drawRect(
      color = outerShadow,
      topLeft = Offset(0f, elevation.toPx() * 0.5f),
      size = size.copy(
        height = size.height - elevation.toPx() * 0.5f,
      ),
    )
  }
)

/**
 * ⑤ Enamel badge — logo badge with glossy border + micro shadow.
 */
fun Modifier.reelPaperEnamelBadge(
  size: Dp = 34.dp,
): Modifier = this.then(
  Modifier
    .border(
      width = 1.dp,
      brush = Brush.linearGradient(
        colors = listOf(
          Color.White.copy(alpha = 0.45f),
          Color.White.copy(alpha = 0.10f),
        ),
      ),
      shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    )
    .drawBehind {
      // Subtle glossy sheen at top
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(
            Color.White.copy(alpha = 0.18f),
            Color.Transparent,
          ),
          startY = 0f,
          endY = size.height * 0.45f,
        ),
        size = size.copy(height = size.height * 0.45f),
      )
    }
)

/**
 * ⑥ Pressable overlay — uniform press feedback.
 * Applies a subtle dark overlay + inset shadow when pressed.
 */
fun Modifier.reelPaperPressable(
  pressColor: Color = PressOverlayTint.copy(alpha = PressOverlayAlpha),
): Modifier = composed {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  this.then(
    Modifier
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = {},
      )
      .drawBehind {
        if (isPressed) {
          drawRect(color = pressColor)
        }
      }
  )
}

/**
 * Convenience: applies raise shadow with optional press feedback.
 */
fun Modifier.reelPaperRaisedCard(
  shape: Shape = RectangleShape,
  isPressed: Boolean = false,
): Modifier = this.then(
  Modifier
    .reelPaperRaise(shape = shape)
    .drawBehind {
      if (isPressed) {
        drawRect(
          color = PressOverlayTint.copy(alpha = PressOverlayAlpha),
        )
      }
    }
)
