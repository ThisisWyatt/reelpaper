/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.library

import android.app.Application
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.presentation.Screen
import app.gyrolet.mpvrx.ui.browser.playlist.PlaylistScreen
import app.gyrolet.mpvrx.ui.browser.playlist.PlaylistViewModel
import app.gyrolet.mpvrx.ui.framecapture.SnapshotLibraryViewModel
import app.gyrolet.mpvrx.ui.framecapture.SnapshotScreen
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.ui.icons.PaperIcons
import app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors
import app.gyrolet.mpvrx.ui.theme.paper.PaperIconButton
import app.gyrolet.mpvrx.ui.theme.paper.paperInset
import app.gyrolet.mpvrx.ui.theme.paper.paperRaise
import app.gyrolet.mpvrx.ui.utils.LocalBackStack
import app.gyrolet.mpvrx.ui.utils.navigateTo
import kotlinx.serialization.Serializable
import app.gyrolet.mpvrx.ui.preferences.PreferencesScreen

/**
 * 「收藏」 hub — playlists and snapshots merged behind one paper-dock key.
 *
 * Mockup `#page-library`: one header (title + live count subtitle + search/sort/settings),
 * archive index tabs (active floats on card paper with a vermilion count pill, inactive
 * sits in an inset slot), and two panes that slide in directionally. The panes reuse the
 * existing playlist/snapshot screens via [LocalLibraryPaneHost].
 */
@Serializable
object LibraryScreen : Screen {
  private enum class LibraryPane {
    PLAYLISTS,
    SNAPSHOTS,
  }

  @Composable
  override fun Content() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val backStack = LocalBackStack.current
    val colors = LocalPaperColors.current
    var pane by rememberSaveable { mutableStateOf(LibraryPane.PLAYLISTS) }
    val controller = remember { LibraryPaneController() }

    val playlistViewModel: PlaylistViewModel =
      viewModel(factory = PlaylistViewModel.factory(application))
    val snapshotViewModel: SnapshotLibraryViewModel =
      viewModel(factory = SnapshotLibraryViewModel.factory(application))
    LifecycleResumeEffect(playlistViewModel) {
      playlistViewModel.refresh()
      onPauseOrDispose { }
    }
    val allPlaylists by playlistViewModel.playlistsWithCount.collectAsState()
    // The protected Favorites entries stay out of the browse lists; the heart toggle is their UI.
    val playlists =
      remember(allPlaylists, playlistViewModel) {
        allPlaylists.filterNot { playlistViewModel.isProtectedPlaylist(it.playlist) }
      }
    val snapshotLibrary by snapshotViewModel.library.collectAsState()
    val playlistCount = playlists.size
    val playlistVideoCount = playlists.sumOf { it.itemCount }
    val snapshotCount = snapshotLibrary.allCaptures.size
    val snapshotFolderCount = snapshotLibrary.folders.size

    Column(modifier = Modifier.fillMaxSize()) {
      // Header: 收藏 + count summary + hub actions.
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Mockup `.brand.pg`: enamel logo badge + page title + live count tag (scaled 1.2×).
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
          app.gyrolet.mpvrx.ui.theme.paper.ReelPaperLogoBadge(size = 40.8.dp)
          Spacer(Modifier.width(11.dp))
          Column {
            Text(
              text = stringResource(R.string.ui_library),
              fontSize = 20.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 0.5.sp,
              color = colors.ink,
            )
            Text(
              text =
                when (pane) {
                  LibraryPane.PLAYLISTS ->
                    stringResource(R.string.library_summary_playlists, playlistCount, playlistVideoCount)
                  LibraryPane.SNAPSHOTS ->
                    stringResource(R.string.library_summary_snapshots, snapshotCount, snapshotFolderCount)
                },
              fontSize = 12.5.sp,
              letterSpacing = 0.6.sp,
              color = colors.inkSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.offset(y = (-1.5).dp),
            )
          }
        }
        PaperIconButton(
          icon = PaperIcons.Search,
          contentDescription = stringResource(R.string.settings_search_title),
          onClick = { controller.requestSearch() },
        )
        PaperIconButton(
          icon = PaperIcons.Sort,
          contentDescription = stringResource(R.string.sort),
          onClick = { controller.requestSort() },
        )
        PaperIconButton(
          icon = PaperIcons.Tune,
          contentDescription = stringResource(R.string.ui_settings),
          onClick = { backStack.navigateTo(PreferencesScreen) },
        )
      }

      // 档案索引标签: active key floats up on card paper, inactive sits in an inset slot.
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
      ) {
        LibraryIndexTab(
          icon = PaperIcons.Playlist,
          label = stringResource(R.string.ui_playlists),
          count = playlistCount,
          active = pane == LibraryPane.PLAYLISTS,
          onClick = { pane = LibraryPane.PLAYLISTS },
          modifier = Modifier.weight(1f),
        )
        LibraryIndexTab(
          icon = PaperIcons.Photo,
          label = stringResource(R.string.ui_snapshots),
          count = snapshotCount,
          active = pane == LibraryPane.SNAPSHOTS,
          onClick = { pane = LibraryPane.SNAPSHOTS },
          modifier = Modifier.weight(1f),
        )
      }

      // Panes: crossfade keeps exactly one pane composed — directional slide-ins kept an
      // exiting pane hit-testable on some devices, killing input over the content area.
      CompositionLocalProvider(LocalLibraryPaneHost provides controller) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
          androidx.compose.animation.Crossfade(
            targetState = pane,
            animationSpec = tween(220),
            label = "library_panes",
          ) { current ->
            when (current) {
              LibraryPane.PLAYLISTS -> PlaylistScreen.Content()
              LibraryPane.SNAPSHOTS -> SnapshotScreen.Content()
            }
          }
        }
      }
    }
  }
}

/** 档案柜纸质标签 per mockup `.idxtab`: pressed-in when idle, raised with vermilion accents when on. */
@Composable
private fun LibraryIndexTab(
  icon: app.gyrolet.mpvrx.ui.icons.AppIcon,
  label: String,
  count: Int,
  active: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalPaperColors.current
  val shape = RoundedCornerShape(13.dp)
  val lift by animateDpAsState(
    targetValue = if (active) 0.dp else 3.dp,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "library_tab_lift",
  )
  val nameColor by animateColorAsState(
    targetValue = if (active) colors.ink else colors.inkTertiary,
    animationSpec = tween(220),
    label = "library_tab_name",
  )
  Box(
    modifier
      .height(46.dp)
      .graphicsLayer { translationY = lift.toPx() }
      .then(if (active) Modifier.paperRaise(shape, colors, elevation = 2.dp) else Modifier.paperInset(shape, colors))
      .then(if (!active) Modifier.border(1.dp, colors.cardLine, shape) else Modifier)
      .clickable(role = Role.Tab, onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.padding(horizontal = 12.dp),
    ) {
      Icon(
        icon,
        contentDescription = null,
        tint = if (active) colors.red else colors.inkTertiary,
        modifier = Modifier.size(18.dp),
      )
      Text(
        text = label,
        color = nameColor,
        fontSize = 13.sp,
        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
        letterSpacing = 1.5.sp,
        maxLines = 1,
      )
      // Count pill: paper + line ring when idle, vermilion gradient when on.
      Box(
        modifier =
          Modifier
            .height(19.dp)
            .widthIn(min = 26.dp)
            .then(
              if (active) {
                Modifier
                  .background(
                    Brush.verticalGradient(listOf(colors.redHighlight, colors.red)),
                    RoundedCornerShape(10.dp),
                  )
              } else {
                Modifier
                  .background(colors.paper, RoundedCornerShape(10.dp))
                  .border(1.dp, colors.cardLine, RoundedCornerShape(10.dp))
              },
            )
            .padding(horizontal = 7.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = count.toString(),
          fontSize = 10.5.sp,
          fontWeight = FontWeight.Bold,
          color = if (active) colors.onRed else colors.inkSecondary,
        )
      }
    }
  }
}
