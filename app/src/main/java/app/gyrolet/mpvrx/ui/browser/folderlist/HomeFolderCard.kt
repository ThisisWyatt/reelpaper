/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.folderlist

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.gyrolet.mpvrx.preferences.VideoSwipeAction
import app.gyrolet.mpvrx.domain.media.model.VideoFolder
import app.gyrolet.mpvrx.ui.browser.cards.FolderCardUiConfig
import app.gyrolet.mpvrx.ui.browser.cards.MediaSelectionNeighbors
import app.gyrolet.mpvrx.ui.browser.cards.PaperFolderCard
import app.gyrolet.mpvrx.ui.browser.cards.PaperFolderMetaChips
import app.gyrolet.mpvrx.ui.browser.cards.rememberFolderCardUiConfig
import app.gyrolet.mpvrx.ui.icons.AppIcon

/**
 * 主页文件夹卡 per mockup `reelPaper-主页-V13.3-照片堆.html`, rendered through the shared
 * [PaperFolderCard] so the home list, the tree browser and network shares stay identical.
 *
 * The signature stays drop-in compatible with [app.gyrolet.mpvrx.ui.browser.cards.FolderCard]'s
 * home call sites so the swap is a rename; parameters this style has no use for are accepted
 * and ignored.
 */
@Composable
internal fun HomeFolderCard(
  folder: VideoFolder,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  @Suppress("UNUSED_PARAMETER") isRecentlyPlayed: Boolean = false,
  onLongClick: (() -> Unit)? = null,
  isSelected: Boolean = false,
  onThumbClick: () -> Unit = {},
  @Suppress("UNUSED_PARAMETER") showDateModified: Boolean = false,
  customIcon: AppIcon? = null,
  newVideoCount: Int = 0,
  customChipContent: @Composable (() -> Unit)? = null,
  isGridMode: Boolean = false,
  @Suppress("UNUSED_PARAMETER") gridRowMinHeight: Dp = 0.dp,
  @Suppress("UNUSED_PARAMETER") onGridContentHeightChanged: ((Int) -> Unit)? = null,
  isPinned: Boolean = false,
  onPinClick: (() -> Unit)? = null,
  @Suppress("UNUSED_PARAMETER") thumbnail: ImageBitmap? = null,
  @Suppress("UNUSED_PARAMETER") isDualPane: Boolean = false,
  isActive: Boolean = false,
  isAudioOnly: Boolean = false,
  onSwipeAction: ((VideoFolder, VideoSwipeAction) -> Unit)? = null,
  @Suppress("UNUSED_PARAMETER") placeholderIconSize: Dp? = null,
  @Suppress("UNUSED_PARAMETER") viewPreferences: app.gyrolet.mpvrx.preferences.BrowserPageViewPreferences? = null,
  uiConfig: FolderCardUiConfig? = null,
  @Suppress("UNUSED_PARAMETER") allowThumbnailGeneration: Boolean = true,
  selectionNeighbors: MediaSelectionNeighbors = MediaSelectionNeighbors(),
) {
  val resolvedUiConfig = uiConfig ?: rememberFolderCardUiConfig()

  // Scoped document-tree folders can be swiped, but Delete must not target a directory in
  // the Play Store flavor (mirrors FolderCard).
  val scopedFolder = app.gyrolet.mpvrx.BuildConfig.SCOPED_STORAGE_ONLY
  val swipeLeft = resolvedUiConfig.swipeLeft
  val swipeRight = resolvedUiConfig.swipeRight
  val swipeEnabled =
    (folder.path.startsWith('/') ||
      (scopedFolder && app.gyrolet.mpvrx.utils.media.ScopedHiddenFolderScanner.isScopedFolder(folder.bucketId)))

  // The corner badge already carries the new count, so the meta only shows detail fields
  // (list only — the grid cell has no room for them).
  val hasMeta =
    customChipContent != null ||
      (!isGridMode &&
        ((resolvedUiConfig.showTotalSizeChip && folder.totalSize > 0) ||
          (resolvedUiConfig.showTotalDurationChip && folder.totalDuration > 0) ||
          (resolvedUiConfig.showDateChip && folder.lastModified > 0)))

  PaperFolderCard(
    name = folder.name,
    onClick = onClick,
    modifier = modifier,
    stampCount = folder.videoCount,
    newCount = newVideoCount,
    customIcon = customIcon,
    meta =
      if (hasMeta) {
        {
          if (customChipContent != null) customChipContent()
          PaperFolderMetaChips(
            folder = folder,
            uiConfig = resolvedUiConfig,
            countsOnly = isGridMode,
          )
        }
      } else {
        null
      },
    listTrailingMeta =
      if (resolvedUiConfig.showFolderPath && folder.path.isNotBlank()) {
        folder.path.substringBeforeLast("/", folder.path)
      } else {
        null
      },
    isSelected = isSelected,
    isGridMode = isGridMode,
    isActive = isActive,
    isPinned = isPinned,
    onThumbClick = onThumbClick,
    onLongClick = onLongClick,
    swipeIdentity = folder.path,
    swipeLeft = if (scopedFolder && swipeLeft == VideoSwipeAction.Delete) VideoSwipeAction.None else swipeLeft,
    swipeRight = if (scopedFolder && swipeRight == VideoSwipeAction.Delete) VideoSwipeAction.None else swipeRight,
    swipeEnabled = swipeEnabled,
    onSwipeAction = onSwipeAction?.let { action -> { swipe -> action(folder, swipe) } },
    selectionNeighbors = selectionNeighbors,
  )
}
