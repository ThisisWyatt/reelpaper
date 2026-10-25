/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * reelPaper 细线描边图标 — vector translations of the mockup's stroke glyphs
 * (`#i-search` / `#i-sort` / `#i-tune` / `#i-home` / `#i-bookmark` / `#i-globe` /
 * `#i-user` in reelPaper-UI-V8.html). The bundled Material Symbols set is filled,
 * which reads too heavy on paper; these are drawn as ink strokes like the mockup.
 * Tinting happens through [Icon], so the paths paint with an opaque black stand-in.
 */
object PaperIcons {
  val Search: AppIcon by lazy(LazyThreadSafetyMode.NONE) { AppIcon(SearchVector) }
  val Sort: AppIcon by lazy(LazyThreadSafetyMode.NONE) { AppIcon(SortVector) }
  val Tune: AppIcon by lazy(LazyThreadSafetyMode.NONE) { AppIcon(TuneVector) }
  val Home: AppIcon by lazy(LazyThreadSafetyMode.NONE) { AppIcon(HomeVector) }
  val Bookmark: AppIcon by lazy(LazyThreadSafetyMode.NONE) { AppIcon(BookmarkVector) }
  val Globe: AppIcon by lazy(LazyThreadSafetyMode.NONE) { AppIcon(GlobeVector) }
  val NetPlus: AppIcon by lazy(LazyThreadSafetyMode.NONE) { AppIcon(NetPlusVector) }
  val Playlist: AppIcon by lazy(LazyThreadSafetyMode.NONE) { AppIcon(PlaylistVector) }
  val PlaylistAdd: AppIcon by lazy(LazyThreadSafetyMode.NONE) { AppIcon(PlaylistAddVector) }
  val Photo: AppIcon by lazy(LazyThreadSafetyMode.NONE) { AppIcon(PhotoVector) }
  val FolderAdd: AppIcon by lazy(LazyThreadSafetyMode.NONE) { AppIcon(FolderAddVector) }
  val User: AppIcon by lazy(LazyThreadSafetyMode.NONE) { AppIcon(UserVector) }
}

private const val VIEWPORT = 24f

private fun paperVector(name: String, block: androidx.compose.ui.graphics.vector.ImageVector.Builder.() -> Unit): ImageVector =
  ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = VIEWPORT,
    viewportHeight = VIEWPORT,
  ).apply(block).build()

private val IconInk = SolidColor(Color(0xFF000000))

private fun androidx.compose.ui.graphics.vector.ImageVector.Builder.inkStroke(
  width: Float,
  cap: StrokeCap = StrokeCap.Round,
  join: StrokeJoin = StrokeJoin.Round,
  block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit,
) = path(
  stroke = IconInk,
  strokeLineWidth = width,
  strokeLineCap = cap,
  strokeLineJoin = join,
  fill = null,
  pathBuilder = block,
)

private fun androidx.compose.ui.graphics.vector.ImageVector.Builder.inkCircle(
  cx: Float,
  cy: Float,
  radius: Float,
  width: Float,
) = path(
  stroke = IconInk,
  strokeLineWidth = width,
  fill = null,
  pathBuilder = {
    // Full circle as two arcs; PathBuilder has no direct addCircle on all targets.
    moveTo(cx + radius, cy)
    arcTo(
      horizontalEllipseRadius = radius,
      verticalEllipseRadius = radius,
      theta = 0f,
      isMoreThanHalf = true,
      isPositiveArc = true,
      x1 = cx - radius,
      y1 = cy,
    )
    arcTo(
      horizontalEllipseRadius = radius,
      verticalEllipseRadius = radius,
      theta = 0f,
      isMoreThanHalf = true,
      isPositiveArc = true,
      x1 = cx + radius,
      y1 = cy,
    )
    close()
  },
)

// #i-search: circle r6.8 @ (11,11) + 45° handle.
private val SearchVector = paperVector("PaperSearch") {
  inkCircle(cx = 11f, cy = 11f, radius = 6.8f, width = 1.9f)
  inkStroke(1.9f) {
    moveTo(16f, 16f)
    lineTo(20.5f, 20.5f)
  }
}

// #i-sort: three shrinking bars + down arrow on the right.
private val SortVector = paperVector("PaperSort") {
  inkStroke(1.8f) {
    moveTo(4f, 6f)
    horizontalLineToRelative(11f)
    moveTo(4f, 11f)
    horizontalLineToRelative(8f)
    moveTo(4f, 16f)
    horizontalLineToRelative(5f)
    moveTo(17.5f, 8f)
    verticalLineToRelative(9f)
    moveTo(15f, 14.5f)
    lineToRelative(2.5f, 2.5f)
    lineToRelative(2.5f, -2.5f)
  }
}

// #i-tune: two rows of horizontal sliders with round knobs.
private val TuneVector = paperVector("PaperTune") {
  inkStroke(1.8f) {
    moveTo(4f, 7.5f)
    horizontalLineToRelative(8.6f)
    moveTo(17f, 7.5f)
    horizontalLineToRelative(3f)
    moveTo(4f, 16.5f)
    horizontalLineToRelative(3.6f)
    moveTo(12f, 16.5f)
    horizontalLineToRelative(8f)
  }
  inkCircle(cx = 14.8f, cy = 7.5f, radius = 2.4f, width = 1.8f)
  inkCircle(cx = 9.8f, cy = 16.5f, radius = 2.4f, width = 1.8f)
}

// #i-home: outline house with separate roof and walls.
private val HomeVector = paperVector("PaperHome") {
  inkStroke(1.9f) {
    moveTo(4f, 11.2f)
    lineTo(12f, 4.4f)
    lineTo(20f, 11.2f)
    moveTo(6.3f, 10f)
    verticalLineToRelative(9.6f)
    horizontalLineToRelative(11.4f)
    verticalLineTo(10f)
  }
}

// #i-bookmark: outline bookmark with rounded shoulders.
private val BookmarkVector = paperVector("PaperBookmark") {
  inkStroke(1.8f) {
    moveTo(7.2f, 3.6f)
    horizontalLineToRelative(9.6f)
    curveToRelative(0.9f, 0f, 1.6f, 0.7f, 1.6f, 1.6f)
    verticalLineToRelative(15.2f)
    lineTo(12f, 16.1f)
    lineToRelative(-6.4f, 4.3f)
    verticalLineTo(5.2f)
    curveToRelative(0f, -0.9f, 0.7f, -1.6f, 1.6f, -1.6f)
    close()
  }
}

// #i-globe: meridian circle.
private val GlobeVector = paperVector("PaperGlobe") {
  inkCircle(cx = 12f, cy = 12f, radius = 8.4f, width = 1.8f)
  inkStroke(1.7f) {
    moveTo(3.6f, 12f)
    horizontalLineToRelative(16.8f)
    moveTo(12f, 3.6f)
    curveToRelative(2.7f, 2.5f, 4.1f, 5.4f, 4.1f, 8.4f)
    // SVG `s` shorthand expanded: first control mirrors the previous curve's last one.
    curveToRelative(0f, 3f, -1.4f, 5.9f, -4.1f, 8.4f)
    curveToRelative(-2.7f, -2.5f, -4.1f, -5.4f, -4.1f, -8.4f)
    curveToRelative(0f, -3f, 1.4f, -5.9f, 4.1f, -8.4f)
    close()
  }
}

// #i-netplus: meridian globe + plus at the lower right (network page FAB).
private val NetPlusVector = paperVector("PaperNetPlus") {
  inkCircle(cx = 10.5f, cy = 10.5f, radius = 7f, width = 1.7f)
  inkStroke(1.5f) {
    moveTo(3.5f, 10.5f)
    horizontalLineToRelative(14f)
    moveTo(10.5f, 3.5f)
    curveToRelative(2.4f, 2.1f, 3.6f, 4.5f, 3.6f, 7f)
    // SVG `s` shorthand expanded: first control mirrors the previous curve's last one.
    curveToRelative(0f, 2.5f, -1.2f, 4.9f, -3.6f, 7f)
    curveToRelative(-2.4f, -2.1f, -3.6f, -4.5f, -3.6f, -7f)
    curveToRelative(0f, -2.5f, 1.2f, -4.9f, 3.6f, -7f)
    close()
    moveTo(18.5f, 15.5f)
    verticalLineToRelative(6f)
    moveTo(15.5f, 18.5f)
    horizontalLineToRelative(6f)
  }
}

// #i-plist: three playlist lines + a filled play wing (library index tab / playlist card).
private val PlaylistVector = paperVector("PaperPlaylist") {
  inkStroke(1.8f) {
    moveTo(4f, 6.5f)
    horizontalLineToRelative(11f)
    moveTo(4f, 11.5f)
    horizontalLineToRelative(7.5f)
    moveTo(4f, 16.5f)
    horizontalLineToRelative(9.5f)
  }
  path(fill = IconInk, pathBuilder = {
    moveTo(15.5f, 10.2f)
    lineTo(20.7f, 13.2f)
    lineTo(15.5f, 16.2f)
    close()
  })
}

// #i-plplus: three short playlist lines + a plus (create-playlist FAB).
private val PlaylistAddVector = paperVector("PaperPlaylistAdd") {
  inkStroke(1.8f) {
    moveTo(4f, 6.5f)
    horizontalLineToRelative(8f)
    moveTo(4f, 11.5f)
    horizontalLineToRelative(8f)
    moveTo(4f, 16.5f)
    horizontalLineToRelative(8f)
    moveTo(18f, 7.5f)
    verticalLineToRelative(8f)
    moveTo(14f, 11.5f)
    horizontalLineToRelative(8f)
  }
}

// #i-photo: rounded photo frame + sun + mountain line (snapshot index tab).
private val PhotoVector = paperVector("PaperPhoto") {
  inkStroke(1.8f) {
    // Rounded 18×14.4 frame at (3,4.8), r2.6 — drawn as one continuous outline.
    moveTo(5.6f, 4.8f)
    horizontalLineToRelative(12.8f)
    arcToRelative(2.6f, 2.6f, 0f, false, true, 2.6f, 2.6f)
    verticalLineToRelative(9.2f)
    arcToRelative(2.6f, 2.6f, 0f, false, true, -2.6f, 2.6f)
    horizontalLineToRelative(-12.8f)
    arcToRelative(2.6f, 2.6f, 0f, false, true, -2.6f, -2.6f)
    verticalLineToRelative(-9.2f)
    arcToRelative(2.6f, 2.6f, 0f, false, true, 2.6f, -2.6f)
    close()
  }
  path(fill = IconInk, pathBuilder = {
    moveTo(8.7f, 7.8f)
    arcToRelative(1.6f, 1.6f, 0f, true, true, -0.01f, 3.2f)
    arcToRelative(1.6f, 1.6f, 0f, true, true, 0.01f, -3.2f)
    close()
  })
  inkStroke(1.8f) {
    moveTo(4.2f, 17.4f)
    lineToRelative(4.5f, -4.7f)
    lineToRelative(3.4f, 3.6f)
    lineToRelative(3f, -3.1f)
    lineToRelative(4.7f, 4.8f)
  }
}

// #i-newfold: folder outline with a plus (new snapshot folder FAB).
private val FolderAddVector = paperVector("PaperFolderAdd") {
  inkStroke(1.8f) {
    moveTo(3.5f, 7.4f)
    curveToRelative(0f, -1.9f, 1.5f, -3.4f, 3.4f, -3.4f)
    horizontalLineToRelative(3.4f)
    lineToRelative(2f, 2.4f)
    horizontalLineToRelative(5.2f)
    curveToRelative(1.9f, 0f, 3.4f, 1.5f, 3.4f, 3.4f)
    verticalLineToRelative(8.2f)
    curveToRelative(0f, 1.9f, -1.5f, 3.4f, -3.4f, 3.4f)
    horizontalLineToRelative(-10.6f)
    curveToRelative(-1.9f, 0f, -3.4f, -1.5f, -3.4f, -3.4f)
    verticalLineToRelative(-8.2f)
    close()
    moveTo(12f, 10.6f)
    verticalLineToRelative(5.8f)
    moveTo(9.1f, 13.5f)
    horizontalLineToRelative(5.8f)
  }
}

// #i-user: head circle + open shoulders arc.
private val UserVector = paperVector("PaperUser") {
  inkCircle(cx = 12f, cy = 7.8f, radius = 3.5f, width = 1.8f)
  inkStroke(1.8f) {
    moveTo(5f, 20f)
    curveToRelative(0.9f, -4f, 3.7f, -6.1f, 7f, -6.1f)
    curveToRelative(3.3f, 0f, 6.1f, 2.1f, 7f, 6.1f)
  }
}
