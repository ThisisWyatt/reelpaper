/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.domain.media.model.VideoFolder
import app.gyrolet.mpvrx.domain.thumbnail.ThumbnailRepository
import app.gyrolet.mpvrx.preferences.AppearancePreferences
import app.gyrolet.mpvrx.preferences.BrowserPageViewPreferences
import app.gyrolet.mpvrx.preferences.BrowserPreferences
import app.gyrolet.mpvrx.preferences.VideoSwipeAction
import app.gyrolet.mpvrx.preferences.preference.collectAsState
import app.gyrolet.mpvrx.ui.icons.AppIcon
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.ui.player.controls.components.tvFocusHighlight
import app.gyrolet.mpvrx.ui.player.controls.components.tvContextMenu
import app.gyrolet.mpvrx.ui.theme.AppShapeScale
import app.gyrolet.mpvrx.ui.theme.rememberConnectedShape
import app.gyrolet.mpvrx.ui.theme.ConnectedLayout
import app.gyrolet.mpvrx.ui.theme.onWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import kotlin.math.pow

@androidx.compose.runtime.Immutable
data class FolderCardUiConfig(
  val unlimitedNameLines: Boolean = false,
  val showTotalVideosChip: Boolean = true,
  val showTotalDurationChip: Boolean = true,
  val showTotalSizeChip: Boolean = true,
  val showDateChip: Boolean = false,
  val showFolderPath: Boolean = false,
  val centerGridTitles: Boolean = false,
  val showFolderThumbnails: Boolean = false,
  val thumbnailQuality: app.gyrolet.mpvrx.preferences.ThumbnailQuality = app.gyrolet.mpvrx.preferences.ThumbnailQuality.High,
  val includeAudio: Boolean = false,
  val swipeLeft: VideoSwipeAction = VideoSwipeAction.AddToPlaylist,
  val swipeRight: VideoSwipeAction = VideoSwipeAction.ToggleWatched,
  val thumbnailGenerator: app.gyrolet.mpvrx.preferences.ThumbnailGenerator = app.gyrolet.mpvrx.preferences.ThumbnailGenerator.Auto,
)

/** Hoist this once per screen and pass the result to every card rather than collecting per item. */
@Composable
fun rememberFolderCardUiConfig(viewPreferences: BrowserPageViewPreferences? = null): FolderCardUiConfig {
  val appearancePreferences = koinInject<AppearancePreferences>()
  val browserPreferences = koinInject<BrowserPreferences>()

  val unlimitedNameLines by (viewPreferences?.unlimitedNameLines ?: appearancePreferences.unlimitedNameLines).collectAsState()
  val showTotalVideosChip by (viewPreferences?.showItemCount ?: browserPreferences.showTotalVideosChip).collectAsState()
  val showTotalDurationChip by browserPreferences.showTotalDurationChip.collectAsState()
  val showTotalSizeChip by browserPreferences.showTotalSizeChip.collectAsState()
  val showDateChip by (viewPreferences?.showDateChip ?: browserPreferences.showDateChip).collectAsState()
  val showFolderPath by browserPreferences.showFolderPath.collectAsState()
  val centerGridTitles by (viewPreferences?.centerGridTitles ?: browserPreferences.centerGridTitles).collectAsState()
  val showFolderThumbnails by (viewPreferences?.showThumbnails ?: browserPreferences.showFolderThumbnails).collectAsState()
  val thumbnailQuality by browserPreferences.thumbnailQuality.collectAsState()
  val thumbnailGenerator by browserPreferences.thumbnailGenerator.collectAsState()
  val includeAudio by browserPreferences.includeAudioBrowser.collectAsState()
  val swipeLeft by browserPreferences.videoSwipeLeft.collectAsState()
  val swipeRight by browserPreferences.videoSwipeRight.collectAsState()

  return remember(
    unlimitedNameLines,
    showTotalVideosChip,
    showTotalDurationChip,
    showTotalSizeChip,
    showDateChip,
    showFolderPath,
    centerGridTitles,
    showFolderThumbnails,
    thumbnailQuality,
    thumbnailGenerator,
    includeAudio,
    swipeLeft,
    swipeRight,
  ) {
    FolderCardUiConfig(
      unlimitedNameLines = unlimitedNameLines,
      showTotalVideosChip = showTotalVideosChip,
      showTotalDurationChip = showTotalDurationChip,
      showTotalSizeChip = showTotalSizeChip,
      showDateChip = showDateChip,
      showFolderPath = showFolderPath,
      centerGridTitles = centerGridTitles,
      showFolderThumbnails = showFolderThumbnails,
      thumbnailQuality = thumbnailQuality,
      thumbnailGenerator = thumbnailGenerator,
      includeAudio = includeAudio,
      swipeLeft = swipeLeft,
      swipeRight = swipeRight,
    )
  }
}

/** Mockup `.fbadge` — 19dp vermilion pill with a paper ring, overlapping the folder corner. */
@Composable
private fun PaperNewCountBadge(
  count: Int,
  modifier: Modifier = Modifier,
) {
  val colors = app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors.current
  val shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
  Box(
    modifier
      .shadow(
        elevation = 2.dp,
        shape = shape,
        clip = false,
        ambientColor = Color(0xFF8E261F).copy(alpha = 0.40f),
        spotColor = Color(0xFF8E261F).copy(alpha = 0.36f),
      )
      .defaultMinSize(minWidth = 19.dp)
      .height(19.dp)
      .clip(shape)
      .background(Brush.verticalGradient(listOf(colors.redHighlight, colors.red)))
      .border(1.5.dp, colors.paper, shape)
      .padding(horizontal = 5.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = count.toString(),
      color = colors.onRed,
      fontSize = 10.5.sp,
      fontWeight = FontWeight.Bold,
      lineHeight = 12.sp,
      maxLines = 1,
    )
  }
}

@Composable
fun FolderCard(
  folder: VideoFolder,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isRecentlyPlayed: Boolean = false,
  onLongClick: (() -> Unit)? = null,
  isSelected: Boolean = false,
  onThumbClick: () -> Unit = {},
  showDateModified: Boolean = false,
  customIcon: AppIcon? = null,
  newVideoCount: Int = 0,
  customChipContent: @Composable (() -> Unit)? = null,
  isGridMode: Boolean = false,
  gridRowMinHeight: Dp = 0.dp,
  onGridContentHeightChanged: ((Int) -> Unit)? = null,
  isPinned: Boolean = false,
  onPinClick: (() -> Unit)? = null,
  thumbnail: ImageBitmap? = null,
  isDualPane: Boolean = false,
  isActive: Boolean = false,
  isAudioOnly: Boolean = false,
  onSwipeAction: ((VideoFolder, VideoSwipeAction) -> Unit)? = null,
  placeholderIconSize: Dp? = null,
  viewPreferences: BrowserPageViewPreferences? = null,
  uiConfig: FolderCardUiConfig? = null,
  allowThumbnailGeneration: Boolean = true,
  selectionNeighbors: MediaSelectionNeighbors = MediaSelectionNeighbors(),
) {
  val resolvedUiConfig = uiConfig ?: rememberFolderCardUiConfig(viewPreferences)
  val unlimitedNameLines = resolvedUiConfig.unlimitedNameLines
  val showTotalVideosChip = resolvedUiConfig.showTotalVideosChip
  val showTotalDurationChip = resolvedUiConfig.showTotalDurationChip
  val showTotalSizeChip = resolvedUiConfig.showTotalSizeChip
  val showDateChip = resolvedUiConfig.showDateChip
  val showFolderPath = resolvedUiConfig.showFolderPath
  val centerGridTitles = resolvedUiConfig.centerGridTitles
  val showFolderThumbnails = resolvedUiConfig.showFolderThumbnails
  val thumbnailQuality = resolvedUiConfig.thumbnailQuality
  val thumbnailGenerator = resolvedUiConfig.thumbnailGenerator
  val includeAudio = resolvedUiConfig.includeAudio
  val swipeLeft = resolvedUiConfig.swipeLeft
  val swipeRight = resolvedUiConfig.swipeRight
  // reelPaper: folders are illustrated (mockup #i-folder) instead of photo collages,
  // so no preview frames are generated here. Explicit caller-provided thumbnails
  // (dual pane selection, callers passing `thumbnail`) are still honored.

  val maxLines = if (unlimitedNameLines) Int.MAX_VALUE else 2
  val selectionContainerColor = animatedSelectionColor(isSelected)
  val showSelectionBadge = isSelected || selectionContainerColor.alpha > 0.001f

  // Remove the redundant folder name from the path
  val parentPath = folder.path.substringBeforeLast("/", folder.path)

  @Composable
  fun PinnedFolderBadge(modifier: Modifier = Modifier) {
    Surface(
      shape = AppShapeScale.full,
      color = MaterialTheme.colorScheme.primary.copy(alpha = 0.94f),
      contentColor = MaterialTheme.colorScheme.onPrimary,
      shadowElevation = 3.dp,
      modifier = modifier.rotate(-18f),
    ) {
      Icon(
        imageVector = Icons.RoundedFilled.PushPin,
        contentDescription =
          androidx.compose.ui.res
            .stringResource(app.gyrolet.mpvrx.R.string.ui_pinned_folder),
        modifier =
          Modifier
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .size(12.dp),
      )
    }
  }

  val cardShape = rememberMediaSelectionShape(isSelected, isGridMode, selectionNeighbors)
  // Derive row height from actual measured labels and chips instead of a fixed footer.

  // Scoped document-tree folders can be swiped for playback, playlists and watched state,
  // but Delete must not target a directory in the Play Store flavor.
  val scopedFolder = app.gyrolet.mpvrx.BuildConfig.SCOPED_STORAGE_ONLY
  // Root box is intentionally NOT clipped: the mockup `.fbadge` rides across the
  // folder art's top-right corner (top -5px, right -8px), so it must sit outside
  // every card-shape clip. The selection wash and the swipe surface clip themselves;
  // tvFocusHighlight's default clipToShape would cut the overflowing badge.
  Box(
    modifier
      .fillMaxWidth()
      .then(if (isGridMode && gridRowMinHeight > 0.dp) Modifier.heightIn(min = gridRowMinHeight) else Modifier)
      .tvFocusHighlight(cardShape, focusedScale = 1.03f, clipToShape = false)
      .semantics { selected = isSelected }
      .tvContextMenu(onLongClick),
  ) {
    Box(
      Modifier
        .matchParentSize()
        .clip(cardShape)
        .background(selectionContainerColor),
    )

    VideoSwipeSurface(
      identity = folder.path,
      leftAction = if (scopedFolder && swipeLeft == VideoSwipeAction.Delete) VideoSwipeAction.None else swipeLeft,
      rightAction = if (scopedFolder && swipeRight == VideoSwipeAction.Delete) VideoSwipeAction.None else swipeRight,
      isWatched = null,
      enabled = !isGridMode && !isSelected &&
        (folder.path.startsWith('/') ||
          (app.gyrolet.mpvrx.BuildConfig.SCOPED_STORAGE_ONLY &&
            app.gyrolet.mpvrx.utils.media.ScopedHiddenFolderScanner.isScopedFolder(folder.bucketId))),
      onAction = onSwipeAction?.let { action -> { swipe -> action(folder, swipe) } },
      // Clickable (ripple) + shape clip live on the self-contained surface so presses
      // keep the rounded card silhouette while the overflowing badge above stays
      // unclipped (it is a sibling drawn on top of this subtree).
      modifier = Modifier.fillMaxWidth().clip(cardShape).mediaCardClickable(
        onClick = onClick,
        onLongClick = onLongClick,
      ),
      shape = cardShape,
      colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
    // Size the actual Card content, not just the swipe wrapper's outer box; otherwise
    // the selected tonal background stops above a taller neighbor in the same row.
    Box(modifier = if (isGridMode) Modifier.fillMaxWidth().heightIn(min = gridRowMinHeight) else Modifier.fillMaxWidth()) {

      if (isGridMode) {
        val horizontalAlignment = if (centerGridTitles) Alignment.CenterHorizontally else Alignment.Start

        // GRID LAYOUT - Vertical arrangement (mockup `.flist.grid .row`):
        // full-bleed folder art, 11px to the caption, 13px/600 single-line name.
        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .onSizeChanged { size -> onGridContentHeightChanged?.invoke(size.height) },
          horizontalAlignment = horizontalAlignment,
        ) {
          val aspect = 20f / 17f

          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .aspectRatio(aspect)
                .tvFocusHighlight(AppShapeScale.medium, focusedScale = 1.03f)
                .mediaCardClickable(
                  onClick = onThumbClick,
                  onLongClick = onLongClick,
                ),
            contentAlignment = Alignment.Center,
          ) {
            val folderContentDescription =
              androidx.compose.ui.res.stringResource(app.gyrolet.mpvrx.R.string.ui_folder)
            if (thumbnail != null) {
              androidx.compose.foundation.Image(
                bitmap = thumbnail,
                contentDescription = folderContentDescription,
                modifier = Modifier.matchParentSize().clip(AppShapeScale.medium),
                contentScale = ContentScale.Crop,
              )
            } else if (customIcon != null) {
              Icon(
                customIcon,
                contentDescription = folderContentDescription,
                modifier = placeholderIconSize?.let { Modifier.size(it) } ?: Modifier.fillMaxWidth().aspectRatio(aspect),
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
              )
            } else {
              app.gyrolet.mpvrx.ui.theme.paper.PaperFolderIcon(
                modifier = Modifier.matchParentSize(),
              )
            }

            SelectionIndicator(isSelected, Modifier.align(Alignment.TopEnd).padding(6.dp))

            if (isPinned) {
              PinnedFolderBadge(
                modifier =
                  Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp),
              )
            }
          }

          Spacer(modifier = Modifier.height(11.dp))

          Text(
            folder.name,
            style = MaterialTheme.typography.titleMedium,
            // Mockup grid `.nm` is 13px; scaled 1.2× per user preference, centered.
            fontSize = 15.6.sp,
            lineHeight = 21.6.sp,
            fontWeight = FontWeight.SemiBold,
            color =
              if (isActive) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.onSurface.onWallpaper(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign =
              if (centerGridTitles) {
                androidx.compose.ui.text.style.TextAlign.Center
              } else {
                androidx.compose.ui.text.style.TextAlign.Start
              },
            modifier = Modifier.fillMaxWidth(),
          )

          if (showTotalVideosChip && folder.videoCount > 0) {
            // Mockup `.st`: secondary ink, 3px below the name; 1.1× per user preference.
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              androidx.compose.ui.res.pluralStringResource(
                when {
                  isAudioOnly -> R.plurals.folder_song_count
                  includeAudio -> R.plurals.folder_media_item_count
                  else -> R.plurals.folder_video_count
                },
                folder.videoCount,
                folder.videoCount,
              ),
              style = MaterialTheme.typography.labelSmall,
              fontSize = 12.1.sp,
              lineHeight = 15.4.sp,
              color = app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors.current.inkSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              textAlign =
                if (centerGridTitles) {
                  androidx.compose.ui.text.style.TextAlign.Center
                } else {
                  androidx.compose.ui.text.style.TextAlign.Start
                },
              modifier = Modifier.fillMaxWidth(),
            )
          }
          if (customChipContent != null) {
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              customChipContent()
            }
          }
        }
      } else {
        // reelPaper flat row (mockup .flist .row): 20dp side padding, 12dp vertical,
        // 46x38 illustrated folder, 13dp gap, ink name + secondary line, chevron at the
        // end, and a hairline divider that starts right of the icon instead of at the edge.
        val paperColors = app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors.current
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .drawBehind {
                val startX = 81.dp.toPx()
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
          horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(13.dp),
        ) {
          Box(
            modifier =
              Modifier
                .size(width = 48.dp, height = 40.dp)
                // clipToShape=false: the `.fbadge` overlay rides above this box's
                // top edge and must not be cut by its rounded clip.
                .tvFocusHighlight(AppShapeScale.medium, focusedScale = 1.03f, clipToShape = false)
                .mediaCardClickable(
                  onClick = onThumbClick,
                  onLongClick = onLongClick,
                ),
            contentAlignment = Alignment.Center,
          ) {
            if (thumbnail != null) {
              androidx.compose.foundation.Image(
                bitmap = thumbnail,
                contentDescription = null,
                modifier = Modifier.matchParentSize().clip(AppShapeScale.medium),
                contentScale = ContentScale.Crop,
              )
            } else if (customIcon != null) {
              Icon(
                customIcon,
                contentDescription = null,
                modifier = placeholderIconSize?.let { Modifier.size(it) } ?: Modifier.matchParentSize(),
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
              )
            } else {
              app.gyrolet.mpvrx.ui.theme.paper.PaperFolderIcon(
                modifier = Modifier.size(width = 46.dp, height = 38.dp),
              )
            }

            // Show new video count badge if folder contains new videos
            if (newVideoCount > 0 && !showSelectionBadge) {
              PaperNewCountBadge(
                count = newVideoCount,
                modifier =
                  Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-5).dp),
              )
            }

            SelectionIndicator(isSelected, Modifier.align(Alignment.TopEnd).padding(4.dp))

            if (isPinned) {
              PinnedFolderBadge(
                modifier =
                  Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp),
              )
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(
            modifier = Modifier.weight(1f),
          ) {
            Text(
              folder.name,
              style = MaterialTheme.typography.titleMedium,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              color =
                if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.onWallpaper(),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            if (showFolderPath && parentPath.isNotEmpty()) {
              Text(
                parentPath,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.onWallpaper(),
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
              )
              Spacer(modifier = Modifier.height(2.dp))
            } else {
              Spacer(modifier = Modifier.height(2.dp))
            }
            FlowRow(
              horizontalArrangement =
                androidx.compose.foundation.layout.Arrangement
                  .spacedBy(4.dp),
              verticalArrangement =
                androidx.compose.foundation.layout.Arrangement
                  .spacedBy(4.dp),
            ) {
              // Render custom chip content first if provided
              var hasChip = false
              if (customChipContent != null) {
                customChipContent()
                hasChip = true
              }

              // Hide chips at storage root level (when videoCount is 0)
              if (showTotalVideosChip && folder.videoCount > 0) {
                Text(
                  androidx.compose.ui.res.pluralStringResource(
                    when {
                      isAudioOnly -> R.plurals.folder_song_count
                      includeAudio -> R.plurals.folder_media_item_count
                      else -> R.plurals.folder_video_count
                    },
                    folder.videoCount,
                    folder.videoCount,
                  ),
                  style = MaterialTheme.typography.labelSmall,
                  color = app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors.current.inkSecondary,
                )
                hasChip = true
              }

              if (showTotalSizeChip && folder.totalSize > 0) {
                Text(
                  formatFileSize(folder.totalSize),
                  style = MaterialTheme.typography.labelSmall,
                  color = app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors.current.inkSecondary,
                )
                hasChip = true
              }

              if (showTotalDurationChip && folder.totalDuration > 0) {
                Text(
                  formatDuration(folder.totalDuration),
                  style = MaterialTheme.typography.labelSmall,
                  color = app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors.current.inkSecondary,
                )
                hasChip = true
              }

              if (showDateChip && folder.lastModified > 0) {
                Text(
                  formatDate(folder.lastModified),
                  style = MaterialTheme.typography.labelSmall,
                  color = app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors.current.inkSecondary,
                )
              }
            }
          }

          Icon(
            Icons.RoundedFilled.ChevronRight,
            contentDescription = null,
            tint = app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors.current.inkTertiary,
            modifier = Modifier.size(19.dp),
          )
        }
      }
    }
    }

    // Mockup `.fbadge`: 19dp vermilion pill with a paper ring, overlapping the
    // folder art's top-right corner. Drawn above the card, outside its clips.
    // (List rows keep their badge inline — it stays inside the row bounds there.)
    if (isGridMode && newVideoCount > 0 && !showSelectionBadge) {
      PaperNewCountBadge(
        count = newVideoCount,
        modifier =
          Modifier
            .align(Alignment.TopEnd)
            .offset(x = 8.dp, y = (-5).dp),
      )
    }
  }
}

private fun formatDuration(durationMs: Long): String {
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

private fun formatFileSize(bytes: Long): String {
  if (bytes <= 0) return "0 B"
  val units = arrayOf("B", "KB", "MB", "GB", "TB")
  val digitGroups = (kotlin.math.log10(bytes.toDouble()) / kotlin.math.log10(1024.0)).toInt()
  val value = bytes / 1024.0.pow(digitGroups.toDouble())
  return String.format(java.util.Locale.getDefault(), "%.1f %s", value, units[digitGroups])
}

// Hoisted because a card formats a date on every recomposition and SimpleDateFormat construction
// parses the pattern and clones a Calendar each time.
private val FOLDER_DATE_FORMATTER: java.time.format.DateTimeFormatter =
  java.time.format.DateTimeFormatter
    .ofPattern("MMM dd, yyyy")
    .withZone(java.time.ZoneId.systemDefault())

private fun formatDate(timestampSeconds: Long): String =
  FOLDER_DATE_FORMATTER.format(java.time.Instant.ofEpochSecond(timestampSeconds))


// The collage uses at most four images and responds to the actual display cell width.
// Two frames divide the cover horizontally; three/four use connected 2x2 blocks.
