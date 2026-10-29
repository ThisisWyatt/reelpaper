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

/**
 * V6 墨卷 Logo 徽章 —— 一纸 · 一卷 · 一键：米纸卡上放同心墨卷、纸芯与朱砂播放键。
 * 1024 基准坐标按画布尺寸缩放到任意 dp。
 */
@Composable
fun ReelPaperLogoBadge(modifier: Modifier = Modifier, size: Dp = 34.dp) {
  Canvas(modifier.size(size)) {
    val w = this.size.width
    val s = w / 1024f
    fun r(units: Float) = units * s
    val center = Offset(w / 2f, w / 2f)
    val corner = androidx.compose.ui.geometry.CornerRadius(w * (225f / 1024f), w * (225f / 1024f))

    // ① 纸卡：米纸竖向渐变 + 纸边 + 受光内边一线。
    drawRoundRect(
      brush = Brush.verticalGradient(listOf(Color(0xFFF8F2E4), Color(0xFFEDE3CB))),
      size = Size(w, w),
      cornerRadius = corner,
    )
    drawRoundRect(
      color = Color(0xFFDACBA6),
      size = Size(w, w),
      cornerRadius = corner,
      style = Stroke(width = 1.dp.toPx()),
    )

    // ② 墨卷：外缘 / 卷面 / 卷层线 / 釉光，共用同一圆心。
    drawCircle(Color(0xFF1F1A12), radius = r(310f), center = center)
    drawCircle(
      brush =
        Brush.linearGradient(
          colors = listOf(Color(0xFF514A3C), Color(0xFF37312A), Color(0xFF241F16)),
          start = Offset(0f, 0f),
          end = Offset(w * 0.8f, w),
        ),
      radius = r(300f),
      center = center,
    )
    drawCircle(
      color = Color(0xFFF0E8D2).copy(alpha = 0.09f),
      radius = r(236f),
      center = center,
      style = Stroke(r(3.5f)),
    )
    drawArc(
      color = Color(0xFFFFF4E0).copy(alpha = 0.15f),
      startAngle = 190f,
      sweepAngle = 90f,
      useCenter = false,
      topLeft = Offset(center.x - r(270f), center.y - r(270f)),
      size = Size(r(540f), r(540f)),
      style = Stroke(width = r(15f), cap = StrokeCap.Round),
    )

    // ③ 纸芯：暗环 + 微凹的米色卷芯 + 上缘一道凹影。
    drawCircle(Color(0xFF16110B), radius = r(210f), center = center)
    drawCircle(
      brush =
        Brush.radialGradient(
          colors = listOf(Color(0xFFF8F1DF), Color(0xFFEFE4C6), Color(0xFFDDCCA4)),
          center = Offset(center.x, center.y - r(204f) * 0.36f),
          radius = r(204f) * 1.2f,
        ),
      radius = r(204f),
      center = center,
    )
    drawCircle(
      color = Color(0xFF7E643C).copy(alpha = 0.28f),
      radius = r(204f),
      center = center,
      style = Stroke(r(2.5f)),
    )
    drawArc(
      color = Color(0xFF604623).copy(alpha = 0.20f),
      startAngle = 204.4f,
      sweepAngle = 131.2f,
      useCenter = false,
      topLeft = Offset(center.x - r(192f), center.y - r(192f)),
      size = Size(r(384f), r(384f)),
      style = Stroke(width = r(14f), cap = StrokeCap.Round),
    )

    // ④ 朱砂键：与 App 内播放键同形，圆心落轴、指向右侧。
    fun keyX(px: Float) = (540.5f + 16f * (px - 14.2f)) * s
    fun keyY(py: Float) = (512f + 16f * (py - 12f)) * s
    val key =
      roundedTriangle(
        a = Offset(keyX(8f), keyY(5.1f)),
        b = Offset(keyX(20.3f), keyY(12f)),
        c = Offset(keyX(8f), keyY(18.9f)),
        radius = 19.2f * s,
      )
    drawPath(
      path = key,
      brush =
        Brush.radialGradient(
          colors =
            listOf(
              Color(0xFFE26B52),
              Color(0xFFDC5A45),
              Color(0xFFB23129),
              Color(0xFF9A2A20),
              Color(0xFF86241D),
            ),
          center = Offset(keyX(11.9f), keyY(7.9f)),
          radius = 320f * s,
        ),
    )
    drawPath(key, Color(0xFF7E211B), style = Stroke(width = 6.4f * s))
  }
}

/** Rounded triangle for the play key — the V6 mark's corners, small enough to stay crisp. */
private fun roundedTriangle(
  a: Offset,
  b: Offset,
  c: Offset,
  radius: Float,
): Path {
  val vertices = listOf(a, b, c)
  fun towards(from: Offset, to: Offset): Offset {
    val dx = to.x - from.x
    val dy = to.y - from.y
    val length = kotlin.math.hypot(dx, dy).coerceAtLeast(0.001f)
    val t = (radius / length).coerceAtMost(0.5f)
    return Offset(from.x + dx * t, from.y + dy * t)
  }
  return Path().apply {
    val startPoint = towards(a, b)
    moveTo(startPoint.x, startPoint.y)
    vertices.indices.forEach { index ->
      val vertex = vertices[index]
      val next = vertices[(index + 1) % vertices.size]
      quadraticTo(vertex.x, vertex.y, towards(next, vertex).x, towards(next, vertex).y)
    }
    close()
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
