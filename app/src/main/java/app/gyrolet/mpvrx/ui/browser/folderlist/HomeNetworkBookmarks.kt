/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.folderlist

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.preferences.NetworkBookmarkPreferences
import app.gyrolet.mpvrx.preferences.preference.collectAsState
import app.gyrolet.mpvrx.ui.browser.cards.PaperFolderCard
import app.gyrolet.mpvrx.ui.browser.networkstreaming.NetworkBrowserScreen
import app.gyrolet.mpvrx.ui.browser.networkstreaming.NetworkBookmarksScreen
import app.gyrolet.mpvrx.ui.browser.networkstreaming.NetworkStreamingViewModel
import app.gyrolet.mpvrx.ui.browser.networkstreaming.ResolvedNetworkFolderBookmark
import app.gyrolet.mpvrx.ui.browser.networkstreaming.resolveNetworkFolderBookmarks
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.PaperIcons
import app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors
import app.gyrolet.mpvrx.ui.utils.LocalBackStack
import app.gyrolet.mpvrx.ui.utils.navigateTo
import org.koin.compose.koinInject

private const val NETWORK_BOOKMARK_COLUMNS = 3

/**
 * 网络收藏文件夹 per mockup home: the folders favourited inside network shares, surfaced on the
 * home page above the local folders. The card keeps the home folder artwork; the vermilion globe
 * sticker on its lower-right corner is what marks it as living on a remote share.
 */
@Composable
internal fun HomeNetworkBookmarksSection(inGrid: Boolean = false) {
  val context = LocalContext.current
  val backstack = LocalBackStack.current
  val bookmarkPreferences = koinInject<NetworkBookmarkPreferences>()
  val bookmarks by bookmarkPreferences.bookmarks.collectAsState()
  val networkViewModel: NetworkStreamingViewModel =
    viewModel(factory = NetworkStreamingViewModel.factory(context.applicationContext as Application))
  val connections by networkViewModel.connections.collectAsState()
  val resolved =
    remember(bookmarks, connections) {
      resolveNetworkFolderBookmarks(bookmarks, connections)
    }
  if (resolved.isEmpty()) return

  val colors = LocalPaperColors.current
  val horizontalPadding = if (inGrid) 2.dp else 22.dp
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(start = horizontalPadding, end = horizontalPadding, top = 14.dp, bottom = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom,
    ) {
      Text(
        text = stringResource(R.string.network_bookmarks_home_title),
        fontSize = 12.5.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 3.sp,
        color = colors.inkTertiary,
        maxLines = 1,
      )
      Text(
        text =
          stringResource(R.string.profile_count_items, resolved.size) +
            " · " +
            stringResource(R.string.network_bookmarks_manage_short),
        fontSize = 11.5.sp,
        letterSpacing = 1.sp,
        color = colors.inkSecondary,
        maxLines = 1,
        modifier =
          Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { backstack.navigateTo(NetworkBookmarksScreen) }
            .padding(horizontal = 2.dp, vertical = 2.dp),
      )
    }

    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(start = if (inGrid) 0.dp else 20.dp, end = if (inGrid) 0.dp else 20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      resolved.chunked(NETWORK_BOOKMARK_COLUMNS).forEach { row ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          row.forEach { bookmark ->
            HomeNetworkBookmarkCard(
              bookmark = bookmark,
              modifier = Modifier.weight(1f),
              onClick = {
                backstack.navigateTo(
                  NetworkBrowserScreen(
                    connectionId = bookmark.connection.id,
                    connectionName = bookmark.connection.name,
                    currentPath = bookmark.bookmark.path,
                  ),
                )
              },
            )
          }
          repeat(NETWORK_BOOKMARK_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
        }
      }
    }
  }
}

@Composable
private fun HomeNetworkBookmarkCard(
  bookmark: ResolvedNetworkFolderBookmark,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  val colors = LocalPaperColors.current
  BoxWithConstraints(modifier = modifier) {
    PaperFolderCard(
      name = bookmark.bookmark.folderName,
      onClick = onClick,
      isGridMode = true,
    )
    // The folder art is 48:41 inside the card; the globe sticker sits on its lower-right corner.
    Box(
      modifier =
        Modifier
          .align(Alignment.TopEnd)
          .offset(x = 3.dp, y = maxWidth * 41f / 48f - 15.dp)
          .size(18.dp)
          .background(colors.red, CircleShape)
          .border(1.5.dp, colors.card, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        PaperIcons.Globe,
        contentDescription = null,
        tint = colors.onRed,
        modifier = Modifier.size(10.dp),
      )
    }
  }
}
