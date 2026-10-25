/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.domain.media.model.VideoFolder
import app.gyrolet.mpvrx.preferences.VideoSwipeAction
import app.gyrolet.mpvrx.ui.icons.AppIcon
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.ui.player.controls.components.tvFocusHighlight
import app.gyrolet.mpvrx.ui.theme.AppShapeScale
import app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors

/**
 * reelPaper 宽舌纸夹文件夹卡 (mockup `reelPaper-主页-V10.4.html`), shared by the home folder
 * list, the tree browser and network shares.
 *
 * The icon is a paper folder — a wide tab behind a cream pocket with a crease line — and an
 * unread folder hangs a vermilion bookmark off the pocket's top edge. List rows keep the flat
 * row anatomy (icon, name, one-line meta, chevron, hairline divider from the icon's right
 * edge); grid cells center a 150dp icon over a two-line name.
 */

private val StackListWidth = 58.8.dp
private val StackListHeight = 49.5.dp
private val StackGridMaxWidth = 150.dp

private val ListNameFontSize = 15.sp
private val GridNameFontSize = 14.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PaperFolderCard(
  name: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  stampCount: Int = 0,
  newCount: Int = 0,
  customIcon: AppIcon? = null,
  meta: (@Composable () -> Unit)? = null,
  listTrailingMeta: String? = null,
  isSelected: Boolean = false,
  isGridMode: Boolean = false,
  isActive: Boolean = false,
  isPinned: Boolean = false,
  onThumbClick: () -> Unit = onClick,
  onLongClick: (() -> Unit)? = null,
  swipeIdentity: String = name,
  swipeLeft: VideoSwipeAction = VideoSwipeAction.None,
  swipeRight: VideoSwipeAction = VideoSwipeAction.None,
  swipeEnabled: Boolean = false,
  onSwipeAction: ((VideoSwipeAction) -> Unit)? = null,
  selectionNeighbors: MediaSelectionNeighbors = MediaSelectionNeighbors(),
) {
  val cardShape = rememberMediaSelectionShape(isSelected, isGridMode, selectionNeighbors)
  val selectionContainerColor = animatedSelectionColor(isSelected)
  val showSelectionBadge = isSelected || selectionContainerColor.alpha > 0.001f

  // Root box is NOT clipped: the red count sticker rides across the folder's top corner.
  Box(
    modifier
      .fillMaxWidth()
      .tvFocusHighlight(cardShape, focusedScale = 1.03f, clipToShape = false)
      .semantics { selected = isSelected },
  ) {
    Box(
      Modifier
        .matchParentSize()
        .clip(cardShape)
        .background(selectionContainerColor),
    )

    VideoSwipeSurface(
      identity = swipeIdentity,
      leftAction = swipeLeft,
      rightAction = swipeRight,
      isWatched = null,
      enabled = swipeEnabled && !isGridMode && !isSelected,
      onAction = onSwipeAction,
      modifier = Modifier.fillMaxWidth().clip(cardShape).mediaCardClickable(onClick = onClick, onLongClick = onLongClick),
      shape = cardShape,
      colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
      if (isGridMode) {
        GridBody(
          name = name,
          customIcon = customIcon,
          stampCount = stampCount,
          newCount = newCount,
          isSelected = isSelected,
          showSelectionBadge = showSelectionBadge,
          isPinned = isPinned,
          isActive = isActive,
          meta = meta,
          onThumbClick = onThumbClick,
          onLongClick = onLongClick,
        )
      } else {
        ListBody(
          name = name,
          customIcon = customIcon,
          stampCount = stampCount,
          newCount = newCount,
          isSelected = isSelected,
          showSelectionBadge = showSelectionBadge,
          isPinned = isPinned,
          isActive = isActive,
          meta = meta,
          trailingMeta = listTrailingMeta,
          onThumbClick = onThumbClick,
          onLongClick = onLongClick,
        )
      }
    }

    if (isGridMode && newCount > 0 && !showSelectionBadge) {
      // Rides above the card clip so the sticker can overhang the folder's corner (mockup
      // `top:-6px;right:-10px`); the box mirrors the grid icon's geometry.
      Box(
        Modifier
          .align(Alignment.TopCenter)
          .widthIn(max = StackGridMaxWidth)
          .fillMaxWidth()
          .aspectRatio(95f / 80f),
      ) {
        CountSticker(
          count = newCount,
          compact = false,
          modifier = Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-6).dp),
        )
      }
    }
  }
}

@Composable
private fun StackSlot(
  compact: Boolean,
  stampCount: Int,
  newCount: Int,
  customIcon: AppIcon?,
  isSelected: Boolean,
  showSelectionBadge: Boolean,
  isPinned: Boolean,
  onThumbClick: () -> Unit,
  onLongClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier
      .then(
        if (compact) {
          Modifier.size(width = StackListWidth, height = StackListHeight)
        } else {
          Modifier.widthIn(max = StackGridMaxWidth).fillMaxWidth().aspectRatio(95f / 80f)
        },
      )
      .tvFocusHighlight(AppShapeScale.medium, focusedScale = 1.03f, clipToShape = false)
      .mediaCardClickable(onClick = onThumbClick, onLongClick = onLongClick),
  ) {
    PaperFolderTabIcon(
      videoCount = stampCount,
      hasBookmark = newCount > 0,
      modifier = Modifier.matchParentSize(),
    )
    if (customIcon != null) {
      Icon(
        customIcon,
        contentDescription = null,
        tint = LocalPaperColors.current.inkSecondary,
        modifier = Modifier.align(Alignment.Center).size(if (compact) 22.dp else 56.dp),
      )
    }
    if (compact && newCount > 0 && !showSelectionBadge) {
      // Mockup `#flist:not(.grid) .fbadge`: top:-8px;right:-7px, shrunk and lifted so it
      // clears the vermilion bookmark on the folder's top edge.
      CountSticker(
        count = newCount,
        compact = true,
        modifier = Modifier.align(Alignment.TopEnd).offset(x = 7.dp, y = (-8).dp),
      )
    }
    SelectionIndicatorHost(isSelected, Modifier.align(Alignment.TopEnd).padding(4.dp))
    if (isPinned) {
      PinnedFolderBadge(Modifier.align(Alignment.TopStart).padding(4.dp))
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GridBody(
  name: String,
  customIcon: AppIcon?,
  stampCount: Int,
  newCount: Int,
  isSelected: Boolean,
  showSelectionBadge: Boolean,
  isPinned: Boolean,
  isActive: Boolean,
  meta: (@Composable () -> Unit)?,
  onThumbClick: () -> Unit,
  onLongClick: (() -> Unit)?,
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    StackSlot(
      compact = false,
      stampCount = stampCount,
      customIcon = customIcon,
      newCount = newCount,
      isSelected = isSelected,
      showSelectionBadge = showSelectionBadge,
      isPinned = isPinned,
      onThumbClick = onThumbClick,
      onLongClick = onLongClick,
    )
    Text(
      text = name,
      fontSize = GridNameFontSize,
      lineHeight = 19.sp,
      fontWeight = FontWeight.SemiBold,
      color = if (isActive) MaterialTheme.colorScheme.primary else LocalPaperColors.current.ink,
      maxLines = 2,
      minLines = 2,
      overflow = TextOverflow.Ellipsis,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center,
      modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
    )
    if (meta != null) {
      // Grid cells are narrow: keep the mockup's single centered line and drop the chips
      // that cannot fit rather than showing them half cut.
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        maxLines = 1,
      ) {
        meta()
      }
    }
  }
}

@Composable
private fun ListBody(
  name: String,
  customIcon: AppIcon?,
  stampCount: Int,
  newCount: Int,
  isSelected: Boolean,
  showSelectionBadge: Boolean,
  isPinned: Boolean,
  isActive: Boolean,
  meta: (@Composable () -> Unit)?,
  trailingMeta: String?,
  onThumbClick: () -> Unit,
  onLongClick: (() -> Unit)?,
) {
  val paperColors = LocalPaperColors.current
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .drawBehind {
          // Mockup `.frow::after`: the hairline starts right of the icon (20 + 56 + 14).
          val startX = (20.dp + StackListWidth + 14.dp).toPx()
          val endX = drawContext.size.width - 20.dp.toPx()
          if (endX > startX) {
            drawLine(
              color = paperColors.line,
              start = Offset(startX, drawContext.size.height),
              end = Offset(endX, drawContext.size.height),
              strokeWidth = 1.dp.toPx(),
            )
          }
        }
        .padding(horizontal = 20.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    StackSlot(
      compact = true,
      stampCount = stampCount,
      customIcon = customIcon,
      newCount = newCount,
      isSelected = isSelected,
      showSelectionBadge = showSelectionBadge,
      isPinned = isPinned,
      onThumbClick = onThumbClick,
      onLongClick = onLongClick,
    )
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = name,
        fontSize = ListNameFontSize,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (isActive) MaterialTheme.colorScheme.primary else paperColors.ink,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (meta != null || trailingMeta != null) {
        Spacer(modifier = Modifier.height(3.5.dp))
        MetaLine(
          meta = meta,
          trailingMeta = trailingMeta,
        )
      }
    }
    Icon(
      Icons.RoundedFilled.ChevronRight,
      contentDescription = null,
      tint = paperColors.inkTertiary,
      modifier = Modifier.size(19.dp),
    )
  }
}

/**
 * Mockup `.fmeta`: one tight line. The theme's 24sp body line box would space the chips out,
 * and wrapping the path onto a second line stretched every row.
 */
@Composable
private fun MetaLine(
  meta: (@Composable () -> Unit)?,
  trailingMeta: String?,
) {
  val paperColors = LocalPaperColors.current
  val fontSize = 12.sp
  val lineHeight = fontSize * 1.3f
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (meta != null) meta()
    if (!trailingMeta.isNullOrBlank()) {
      Text(
        text = trailingMeta,
        fontSize = fontSize,
        lineHeight = lineHeight,
        color = paperColors.inkSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f, fill = false),
      )
    }
  }
}

/** Mockup `.fmeta`: "<n> 个视频" then the unread count in vermilion. */
@Composable
internal fun PaperFolderMetaChips(
  folder: VideoFolder,
  uiConfig: FolderCardUiConfig,
  countsOnly: Boolean = false,
) {
  val paperColors = LocalPaperColors.current
  val fontSize = 12.sp
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // The mockup's grid cell carries the counts only; detail fields live in the list row.
    if (countsOnly) return@Row
    if (uiConfig.showTotalSizeChip && folder.totalSize > 0) {
      Text(paperFileSize(folder.totalSize), fontSize = fontSize, color = paperColors.inkSecondary, maxLines = 1)
    }
    if (uiConfig.showTotalDurationChip && folder.totalDuration > 0) {
      Text(paperDuration(folder.totalDuration), fontSize = fontSize, color = paperColors.inkSecondary, maxLines = 1)
    }
    if (uiConfig.showDateChip && folder.lastModified > 0) {
      Text(paperDate(folder.lastModified), fontSize = fontSize, color = paperColors.inkSecondary, maxLines = 1)
    }
  }
}

/** Mockup `.fbadge`: red round sticker on the folder's corner. */
@Composable
private fun CountSticker(
  count: Int,
  compact: Boolean,
  modifier: Modifier = Modifier,
) {
  val colors = LocalPaperColors.current
  val shape = RoundedCornerShape(50)
  val height = if (compact) 16.5.dp else 19.dp
  Box(
    modifier
      .defaultMinSize(minWidth = height)
      .height(height)
      .clip(shape)
      .background(Brush.verticalGradient(listOf(colors.redHighlight, colors.red)))
      .border(1.5.dp, colors.paper, shape)
      .padding(horizontal = if (compact) 4.dp else 5.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = count.toString(),
      color = colors.onRed,
      fontSize = if (compact) 9.5.sp else 10.5.sp,
      fontWeight = FontWeight.Bold,
      lineHeight = 12.sp,
      maxLines = 1,
    )
  }
}

@Composable
private fun SelectionIndicatorHost(selected: Boolean, modifier: Modifier = Modifier) {
  SelectionIndicator(selected = selected, modifier = modifier)
}

@Composable
private fun PinnedFolderBadge(modifier: Modifier = Modifier) {
  Surface(
    shape = AppShapeScale.full,
    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.94f),
    contentColor = MaterialTheme.colorScheme.onPrimary,
    shadowElevation = 3.dp,
    modifier = modifier,
  ) {
    Icon(
      imageVector = Icons.RoundedFilled.PushPin,
      contentDescription = stringResource(R.string.ui_pinned_folder),
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp).size(12.dp),
    )
  }
}

/**
 * V10.4 宽舌纸夹 (viewBox 100×70): a wide tab behind a cream pocket; the pocket carries a
 * white highlight along its top edge and an ink crease line. An unread folder hangs a
 * vermilion bookmark off the pocket's top-right edge. Strokes stay hairline at both sizes
 * (mockup `vector-effect: non-scaling-stroke`).
 */
@Composable
internal fun PaperFolderTabIcon(
  videoCount: Int,
  hasBookmark: Boolean,
  modifier: Modifier = Modifier,
) {
  val textMeasurer = rememberTextMeasurer()
  Canvas(modifier) {
    val sx = size.width / 100f
    val sy = size.height / 70f
    val hairline = 1.dp.toPx()

    // Back tab.
    val tabTopLeft = Offset(8f * sx, 7f * sy)
    val tabSize = Size(60f * sx, 18f * sy)
    val tabRadius = CornerRadius(5f * sx, 5f * sy)
    drawRoundRect(
      brush = Brush.verticalGradient(listOf(Color(0xFFE9DEBF), Color(0xFFD9CBA3)), startY = 7f * sy, endY = 25f * sy),
      topLeft = tabTopLeft,
      size = tabSize,
      cornerRadius = tabRadius,
    )
    drawRoundRect(
      color = Color(0xFFC7B78C),
      topLeft = tabTopLeft,
      size = tabSize,
      cornerRadius = tabRadius,
      style = Stroke(hairline),
    )
    drawLine(
      color = Color(0x80FFFFFF),
      start = Offset(12.5f * sx, 10.8f * sy),
      end = Offset(63.5f * sx, 10.8f * sy),
      strokeWidth = hairline,
      cap = StrokeCap.Round,
    )

    // Front pocket.
    val bodyTopLeft = Offset(4.5f * sx, 21f * sy)
    val bodySize = Size(91f * sx, 45f * sy)
    val bodyRadius = CornerRadius(6.5f * sx, 6.5f * sy)
    drawRoundRect(
      brush = Brush.verticalGradient(listOf(Color(0xFFFDFAF0), Color(0xFFF0E7CE)), startY = 21f * sy, endY = 66f * sy),
      topLeft = bodyTopLeft,
      size = bodySize,
      cornerRadius = bodyRadius,
    )
    drawRoundRect(
      color = Color(0xFFDBCDA4),
      topLeft = bodyTopLeft,
      size = bodySize,
      cornerRadius = bodyRadius,
      style = Stroke(hairline),
    )
    drawLine(
      color = Color(0xBFFFFFFF),
      start = Offset(9.5f * sx, 24.2f * sy),
      end = Offset(90.5f * sx, 24.2f * sy),
      strokeWidth = hairline,
      cap = StrokeCap.Round,
    )
    drawLine(
      color = Color(0x1F655026),
      start = Offset(9.5f * sx, 30f * sy),
      end = Offset(90.5f * sx, 30f * sy),
      strokeWidth = hairline,
    )

    // ×N stamp inside the pocket, like the count printed on the other folder style.
    if (videoCount > 0) {
      val fontPx = 7.5f * sx
      val layout =
        textMeasurer.measure(
          AnnotatedString("×$videoCount"),
          style = TextStyle(fontSize = fontPx.toSp(), fontWeight = FontWeight.Bold, color = Color(0xFF7B7160)),
        )
      drawText(
        layout,
        topLeft = Offset(88f * sx - layout.size.width, 61f * sy - layout.size.height * 0.8f),
      )
    }

    // Vermilion bookmark, hanging off the pocket's top edge.
    if (hasBookmark) {
      val bookmark =
        Path().apply {
          moveTo(84f * sx, 21f * sy)
          lineTo(94.5f * sx, 21f * sy)
          lineTo(94.5f * sx, 35f * sy)
          lineTo(89.25f * sx, 30.3f * sy)
          lineTo(84f * sx, 35f * sy)
          close()
        }
      drawPath(
        bookmark,
        brush = Brush.verticalGradient(listOf(Color(0xFFD65444), Color(0xFFA62E25)), startY = 21f * sy, endY = 35f * sy),
      )
    }
  }
}

private fun paperDuration(durationMs: Long): String {
  val seconds = durationMs / 1000
  val hours = seconds / 3600
  val minutes = (seconds % 3600) / 60
  val secs = seconds % 60
  return when {
    hours > 0 -> "${hours}h ${minutes}m"
    minutes > 0 -> "${minutes}m"
    else -> "${secs}s"
  }
}

private fun paperFileSize(bytes: Long): String {
  val kb = bytes / 1024.0
  val mb = kb / 1024.0
  val gb = mb / 1024.0
  return when {
    gb >= 1 -> String.format(java.util.Locale.US, "%.1f GB", gb)
    mb >= 1 -> String.format(java.util.Locale.US, "%.1f MB", mb)
    kb >= 1 -> String.format(java.util.Locale.US, "%.1f KB", kb)
    else -> "$bytes B"
  }
}

private val PAPER_DATE_FORMATTER: java.time.format.DateTimeFormatter =
  java.time.format.DateTimeFormatter
    .ofPattern("MMM dd, yyyy")
    .withZone(java.time.ZoneId.systemDefault())

private fun paperDate(timestampSeconds: Long): String =
  PAPER_DATE_FORMATTER.format(java.time.Instant.ofEpochSecond(timestampSeconds))
