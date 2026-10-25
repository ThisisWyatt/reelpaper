/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.theme.paper

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * Vector translations of the mockup's signature illustrations
 * (SVG symbols #i-rplogo / #i-folder / #i-photostack in reelPaper-UI-V8.html).
 * Coordinates below are in the mockup's viewBox units; each painter scales
 * them to the actual canvas size, so callers only pick a dp size.
 */

/** 胶片盘 Logo 徽章 — 珐琅软浮, used in page top bars. */
@Composable
fun ReelPaperLogoBadge(modifier: Modifier = Modifier, size: Dp = 34.dp) {
  val colors = LocalPaperColors.current
  Canvas(modifier.size(size)) {
    val w = this.size.width
    val h = this.size.height
    val corner = w * (10f / 34f)

    // Enamel badge: red vertical gradient + dark border.
    drawRoundRect(
      brush =
        Brush.verticalGradient(
          listOf(Color(0xFFD65444), colors.red, Color(0xFF9C2A22)),
        ),
      topLeft = Offset(0f, 0f),
      size = Size(w, h),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
    )
    drawRoundRect(
      color = colors.redDark,
      topLeft = Offset(0f, 0f),
      size = Size(w, h),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
      style = Stroke(width = w * 0.03f),
    )

    // Film-reel emblem, symbol viewBox 9..39 (30 units) rendered at 22/34 of the badge.
    val pad = w * (6f / 34f)
    val icon = w - pad * 2f
    val s = icon / 30f
    // Symbol coordinates are in 9..39; the icon origin is symbol (9,9).
    fun sx(v: Float) = (v - 9f) * s
    translate(pad, pad) {
      val disc = Brush.radialGradient(
        listOf(Color(0xFFFCF4E2), Color(0xFFF5EAD1), Color(0xFFE2CFA9)),
        center = Offset(icon * 0.5f + icon * 0.14f, icon * 0.5f - icon * 0.11f),
        radius = icon * 0.95f,
      )
      drawCircle(brush = disc, radius = icon / 2f, center = Offset(icon / 2f, icon / 2f))
      // Engraved ring.
      drawCircle(
        color = Color(0xFF7E211B).copy(alpha = 0.15f),
        radius = 12.3f * s,
        center = Offset(icon / 2f, icon / 2f),
        style = Stroke(width = 1f * s),
      )
      // Top-left sheen arc: M13.54 17.49 A12.3 12.3 → 21.03 12.07 (circle r12.3 at disc center).
      val arc =
        Path().apply {
          moveTo(sx(13.54f), sx(17.49f))
          arcTo(
            rect = Rect(sx(24f) - 12.3f * s, sx(24f) - 12.3f * s, sx(24f) + 12.3f * s, sx(24f) + 12.3f * s),
            startAngleDegrees = 212f,
            sweepAngleDegrees = 44f,
            forceMoveTo = false,
          )
        }
      drawPath(arc, Color.White.copy(alpha = 0.6f), style = Stroke(1.5f * s, cap = StrokeCap.Round))
      val hole =
        Brush.radialGradient(
          listOf(Color(0xFF7E211B), Color(0xFF93281F), Color(0xFFB03E2C)),
        )
      listOf(
        Offset(sx(24f), sx(15.2f)),
        Offset(sx(16.38f), sx(28.4f)),
        Offset(sx(31.62f), sx(28.4f)),
      ).forEach { c ->
        drawCircle(brush = hole, radius = 2.9f * s, center = c)
      }
      // Play wing with its light edge line.
      val wing =
        Path().apply {
          moveTo(sx(20.66f), sx(19.36f))
          lineTo(sx(20.66f), sx(28.64f))
          lineTo(sx(29.07f), sx(24f))
          close()
        }
      drawPath(wing, brush = hole)
      drawLine(
        Color.White.copy(alpha = 0.3f),
        Offset(sx(20.66f), sx(19.36f)),
        Offset(sx(20.66f), sx(28.64f)),
        strokeWidth = 1f * s,
      )
    }
  }
}

private val FolderFlapBack = Color(0xFFDCCBA2)
private val FolderEdge = Color(0xFFB3A075)

/** 插画拟物文件夹 — replaces photo-mosaic folder previews. Aspect 48:38. */
@Composable
fun PaperFolderIcon(modifier: Modifier = Modifier) {
  val colors = LocalPaperColors.current
  Canvas(modifier) {
    val sx = size.width / 48f
    val sy = size.height / 38f

    val flap =
      Path().apply {
        moveTo(3.5f * sx, 8.6f * sy)
        cubicTo(3.5f * sx, 6.1f * sy, 5.5f * sx, 4f * sy, 8.1f * sx, 4f * sy)
        lineTo(17.7f * sx, 4f * sy)
        lineTo(22f * sx, 9.1f * sy)
        lineTo(38.9f * sx, 9.1f * sy)
        cubicTo(41.4f * sx, 9.1f * sy, 43.5f * sx, 11.1f * sy, 43.5f * sx, 13.7f * sy)
        lineTo(43.5f * sx, 16f * sy)
        lineTo(3.5f * sx, 16f * sy)
        close()
      }
    val body =
      Path().apply {
        addRoundRect(
          androidx.compose.ui.geometry.RoundRect(
            Rect(3.5f * sx, 13.4f * sy, 44.5f * sx, 34f * sy),
            CornerRadiusCompat(4.2f * sx, 4.2f * sy),
          ),
        )
      }

    // Mockup `.row .folder` carries filter: drop-shadow(0 3px 2px rgba(96,74,36,.3));
    // Canvas has no blur, so layer three offset fills for a soft edge.
    listOf(0.9f, 1.35f, 1.8f).forEach { dy ->
      translate(0f, dy * sy) {
        drawPath(flap, Color(0xFF604A24).copy(alpha = 0.10f))
        drawPath(body, Color(0xFF604A24).copy(alpha = 0.10f))
      }
    }

    drawPath(flap, FolderFlapBack)
    drawPath(flap, FolderEdge, style = Stroke(1.4f * sx, join = StrokeJoin.Round))

    val bodyRect = Rect(3.5f * sx, 13.4f * sy, 44.5f * sx, 34f * sy)
    val corner = CornerRadiusCompat(4.2f * sx, 4.2f * sy)
    drawRoundRect(
      brush =
        Brush.verticalGradient(
          listOf(Color(0xFFF0E7CE), Color(0xFFE5D8B4), Color(0xFFD6C49A)),
          startY = bodyRect.top,
          endY = bodyRect.bottom,
        ),
      topLeft = bodyRect.topLeft,
      size = bodyRect.size,
      cornerRadius = corner,
    )
    drawRoundRect(
      color = FolderEdge,
      topLeft = bodyRect.topLeft,
      size = bodyRect.size,
      cornerRadius = corner,
      style = Stroke(1.4f * sx),
    )
    // Top rebound highlight and content lines.
    drawLine(
      Color.White.copy(alpha = 0.55f),
      Offset(6.5f * sx, 16.5f * sy),
      Offset(41.5f * sx, 16.5f * sy),
      strokeWidth = 1.2f * sx,
      cap = StrokeCap.Round,
    )
    drawLine(
      Color(0xFF8C7850).copy(alpha = 0.22f),
      Offset(8f * sx, 22.5f * sy),
      Offset(26f * sx, 22.5f * sy),
      strokeWidth = 1.6f * sx,
      cap = StrokeCap.Round,
    )
    drawLine(
      Color(0xFF8C7850).copy(alpha = 0.22f),
      Offset(8f * sx, 26.5f * sy),
      Offset(20f * sx, 26.5f * sy),
      strokeWidth = 1.6f * sx,
      cap = StrokeCap.Round,
    )
  }
}

private fun CornerRadiusCompat(x: Float, y: Float) = androidx.compose.ui.geometry.CornerRadius(x, y)

/** 快照文件夹「照片小叠」 — four slightly rotated photo cards. Aspect 48:50. */
@Composable
fun PaperPhotoStackIcon(modifier: Modifier = Modifier) {
  Canvas(modifier) {
    val sx = size.width / 48f
    val sy = size.height / 50f

    // Ground shadow ellipse.
    drawOval(
      color = Color(0xFF604A24).copy(alpha = 0.16f),
      topLeft = Offset(8.5f * sx, 41.2f * sy),
      size = Size(32f * sx, 4.8f * sy),
    )

    val cards =
      listOf(
        CardSpec(10f, 1.8f, 0.3f, Color(0xFFE0D5B8)),
        CardSpec(-8f, -1.5f, -0.4f, Color(0xFFEAE0C8)),
        CardSpec(4f, 1f, 0.2f, Color(0xFFF2EBD7)),
      )
    cards.forEach { spec ->
      rotatedCard(spec.deg, spec.dx, spec.dy) {
        drawRoundRect(
          color = spec.fill,
          topLeft = Offset(6.5f * sx, 10f * sy),
          size = Size(35f * sx, 28f * sy),
          cornerRadius = CornerRadiusCompat(2f * sx, 2f * sy),
        )
        drawRoundRect(
          color = Color(0xFFAE9B70),
          topLeft = Offset(6.5f * sx, 10f * sy),
          size = Size(35f * sx, 28f * sy),
          cornerRadius = CornerRadiusCompat(2f * sx, 2f * sy),
          style = Stroke(1f * sx),
        )
      }
    }

    // Top photo with sky, sun, and mountains.
    rotatedCard(-1.5f, 0f, 0f) {
      drawRoundRect(
        color = Color(0xFFFCF8EC),
        topLeft = Offset(6.5f * sx, 10f * sy),
        size = Size(35f * sx, 28f * sy),
        cornerRadius = CornerRadiusCompat(2f * sx, 2f * sy),
      )
      drawRoundRect(
        color = FolderEdge,
        topLeft = Offset(6.5f * sx, 10f * sy),
        size = Size(35f * sx, 28f * sy),
        cornerRadius = CornerRadiusCompat(2f * sx, 2f * sy),
        style = Stroke(1.1f * sx),
      )
      // Sky window.
      drawRoundRect(
        brush =
          Brush.linearGradient(
            listOf(Color(0xFFF5D9A6), Color(0xFFC98A52)),
            start = Offset(9.5f * sx, 13f * sy),
            end = Offset(13.85f * sx, 32f * sy),
          ),
        topLeft = Offset(9.5f * sx, 13f * sy),
        size = Size(29f * sx, 19f * sy),
        cornerRadius = CornerRadiusCompat(1.3f * sx, 1.3f * sy),
      )
      drawCircle(Color(0xFFFBEFC9), radius = 2.3f * sx, center = Offset(15.8f * sx, 17.6f * sy))
      val mountains =
        Path().apply {
          moveTo(9.5f * sx, 32f * sy)
          lineTo(17.5f * sx, 23.5f * sy)
          lineTo(23f * sx, 29f * sy)
          lineTo(28f * sx, 25f * sy)
          lineTo(33.5f * sx, 30.5f * sy)
          lineTo(38.5f * sx, 26.5f * sy)
          lineTo(38.5f * sx, 32f * sy)
          close()
        }
      drawPath(mountains, Color(0xFF3E645C))
      val cloud =
        Path().apply {
          moveTo(13.5f * sx, 20.2f * sy)
          quadraticBezierTo(20f * sx, 16.8f * sy, 27.5f * sx, 17.6f * sy)
        }
      drawPath(
        cloud,
        Color.White.copy(alpha = 0.5f),
        style = Stroke(1.5f * sx, cap = StrokeCap.Round),
      )
    }
  }
}

private data class CardSpec(val deg: Float, val dx: Float, val dy: Float, val fill: Color)

private fun DrawScope.rotatedCard(deg: Float, dx: Float, dy: Float, block: DrawScope.() -> Unit) {
  val sx = size.width / 48f
  val sy = size.height / 50f
  translate(dx * sx, dy * sy) {
    rotate(degrees = deg, pivot = Offset(24f * sx, 24f * sy)) {
      block()
    }
  }
}
