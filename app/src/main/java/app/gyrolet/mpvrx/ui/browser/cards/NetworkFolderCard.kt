/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.cards

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.gyrolet.mpvrx.domain.network.NetworkFile

/**
 * A directory on a network share, drawn with the same reelPaper polaroid stack as the home
 * folders. Shares are not scanned, so the print carries no ×N stamp.
 */
@Composable
fun NetworkFolderCard(
  file: NetworkFile,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  onLongClick: (() -> Unit)? = null,
  isSelected: Boolean = false,
  isGridMode: Boolean = false,
  selectionNeighbors: MediaSelectionNeighbors = MediaSelectionNeighbors(),
) {
  PaperFolderCard(
    name = file.name,
    onClick = onClick,
    modifier = modifier,
    isSelected = isSelected,
    isGridMode = isGridMode,
    onLongClick = onLongClick,
    selectionNeighbors = selectionNeighbors,
  )
}
