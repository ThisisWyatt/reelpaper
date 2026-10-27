/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.profile

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import app.gyrolet.mpvrx.presentation.components.AppAlertDialog as AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import app.gyrolet.mpvrx.presentation.components.AppDialogTextButton as TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.database.entities.PlaylistEntity
import app.gyrolet.mpvrx.database.repository.PlaylistRepository
import app.gyrolet.mpvrx.domain.framecapture.FrameCapture
import app.gyrolet.mpvrx.domain.media.model.Video
import app.gyrolet.mpvrx.domain.thumbnail.ThumbnailRepository
import app.gyrolet.mpvrx.preferences.AdvancedPreferences
import app.gyrolet.mpvrx.preferences.AppearancePreferences
import app.gyrolet.mpvrx.preferences.BrowserPreferences
import app.gyrolet.mpvrx.preferences.preference.collectAsState
import app.gyrolet.mpvrx.presentation.Screen
import app.gyrolet.mpvrx.presentation.components.pullrefresh.PullRefreshBox
import app.gyrolet.mpvrx.ui.browser.LocalNavigationBarHeight
import app.gyrolet.mpvrx.ui.browser.components.BrowserTopBar
import app.gyrolet.mpvrx.ui.browser.components.rememberSwipePlaybackInfo
import app.gyrolet.mpvrx.ui.browser.networkstreaming.NetworkStreamingScreen
import app.gyrolet.mpvrx.ui.browser.playlist.PlaylistDetailScreen
import app.gyrolet.mpvrx.ui.browser.playlist.PlaylistScreen
import app.gyrolet.mpvrx.ui.browser.playlist.PlaylistViewModel
import app.gyrolet.mpvrx.ui.browser.recentlyplayed.RecentlyPlayedItem
import app.gyrolet.mpvrx.ui.browser.recentlyplayed.RecentlyPlayedScreen
import app.gyrolet.mpvrx.ui.browser.recentlyplayed.RecentlyPlayedViewModel
import app.gyrolet.mpvrx.ui.framecapture.SnapshotDetailScreen
import app.gyrolet.mpvrx.ui.framecapture.SnapshotLibraryViewModel
import app.gyrolet.mpvrx.ui.framecapture.SnapshotScreen
import app.gyrolet.mpvrx.ui.framecapture.toDetailItem
import app.gyrolet.mpvrx.ui.icons.AppIcon
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.ui.icons.PaperIcons
import app.gyrolet.mpvrx.ui.player.controls.components.tvFocusHighlight
import app.gyrolet.mpvrx.ui.preferences.PreferencesScreen
import app.gyrolet.mpvrx.ui.preferences.ProfileWatchStatistics
import app.gyrolet.mpvrx.ui.theme.AppMotion
import app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors
import app.gyrolet.mpvrx.ui.theme.paper.paperInset
import app.gyrolet.mpvrx.ui.theme.paper.paperRaise
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.border
import androidx.compose.runtime.produceState
import app.gyrolet.mpvrx.ui.theme.AppShapeScale
import app.gyrolet.mpvrx.ui.theme.AppConnectedShapeTokens
import app.gyrolet.mpvrx.ui.theme.ConnectedLayout
import app.gyrolet.mpvrx.ui.theme.rememberConnectedShape
import app.gyrolet.mpvrx.ui.theme.onWallpaper
import app.gyrolet.mpvrx.ui.theme.wallpaperAwareBackgroundColor
import app.gyrolet.mpvrx.ui.utils.LocalBackStack
import app.gyrolet.mpvrx.ui.utils.navigateTo
import app.gyrolet.mpvrx.ui.utils.rememberAppHaptics
import app.gyrolet.mpvrx.utils.media.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import java.io.File

private const val SHELF_LIMIT = 15
private const val AVATAR_FILE_PREFIX = "profile_avatar_"
private const val AVATAR_SIZE_PX = 512
private val SHELF_VIDEO_CARD_WIDTH = 214.dp

/** Personal hub that folds Recents, Playlists and Snapshots into one bottom-navigation tab. */
@Serializable
object ProfileScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val backStack = LocalBackStack.current
    val scope = rememberCoroutineScope()
    val haptics = rememberAppHaptics()
    val appearancePreferences = koinInject<AppearancePreferences>()
    val advancedPreferences = koinInject<AdvancedPreferences>()
    val browserPreferences = koinInject<BrowserPreferences>()
    val enableRecentlyPlayed by advancedPreferences.enableRecentlyPlayed.collectAsState()
    val profileName by appearancePreferences.profileName.collectAsState()
    val profileImagePath by appearancePreferences.profileImagePath.collectAsState()
    val showRecentThumbnails by browserPreferences.recentView.showThumbnails.collectAsState()
    val configuration = LocalConfiguration.current
    val navigationBarHeight = LocalNavigationBarHeight.current

    val recentsViewModel: RecentlyPlayedViewModel = viewModel(factory = RecentlyPlayedViewModel.factory(application))
    val playlistViewModel: PlaylistViewModel = viewModel(factory = PlaylistViewModel.factory(application))
    val snapshotViewModel: SnapshotLibraryViewModel = viewModel(factory = SnapshotLibraryViewModel.factory(application))

    LifecycleResumeEffect(playlistViewModel) {
      playlistViewModel.refresh()
      onPauseOrDispose { }
    }

    val recentItems by recentsViewModel.recentItems.collectAsState()
    val playlists by playlistViewModel.playlistsWithCount.collectAsState()
    val snapshotLibrary by snapshotViewModel.library.collectAsState()
    val snapshotThumbnails by snapshotViewModel.thumbnailCache.collectAsState()

    val recentShelf = remember(recentItems) { recentItems.take(SHELF_LIMIT) }
    val snapshots = remember(snapshotLibrary.allCaptures) { snapshotLibrary.allCaptures.sortedByDescending { it.capturedAt } }

    var activeVideoItem by remember { mutableStateOf<RecentlyPlayedItem.VideoItem?>(null) }
    var showProfileDialog by rememberSaveable { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    val isRefreshing = remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val missingFileMessage = stringResource(R.string.ui_recent_file_no_longer_exists)

    fun playRecent(video: Video) {
      scope.launch {
        val playable = recentsViewModel.resolvePlayableRecentVideo(video)
        if (playable != null) {
          MediaUtils.playFile(playable, context, "recently_played")
        } else {
          Toast.makeText(context, missingFileMessage, Toast.LENGTH_SHORT).show()
        }
      }
    }

    Scaffold(
      containerColor = wallpaperAwareBackgroundColor(),
      topBar = {
        BrowserTopBar(
          title = stringResource(R.string.ui_profile),
          brandLogo = true,
          brandTag = stringResource(R.string.profile_media_tag),
          isInSelectionMode = false,
          selectedCount = 0,
          totalCount = 0,
          onCancelSelection = { },
          onSettingsClick = { backStack.navigateTo(PreferencesScreen) },
          additionalActions = {
            // 设计稿 .iconbtn ⓘ：数据管理入口 —— 打开隐私说明（观看数据仅存本机）。
            app.gyrolet.mpvrx.ui.theme.paper.PaperIconButton(
              icon = Icons.RoundedFilled.Info,
              contentDescription = stringResource(R.string.profile_privacy_note),
              onClick = { showPrivacyDialog = true },
              modifier = Modifier.padding(horizontal = 4.dp),
            )
          },
        )
      },
    ) { paddingValues ->
      PullRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
          recentsViewModel.refresh()
          playlistViewModel.refresh().join()
        },
        listState = listState,
        modifier = Modifier.fillMaxSize().padding(paddingValues),
      ) {
        val isTablet = configuration.smallestScreenWidthDp >= 600
        val horizontalShelfPadding = if (isTablet) 24.dp else 16.dp

        LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(top = 0.dp, bottom = navigationBarHeight + 16.dp),
          verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
          item(key = "profile_overview") {
            ProfileArchiveCard(
              name = profileName,
              avatarPath = profileImagePath,
              onEditProfile = {
                haptics.tick()
                showProfileDialog = true
              },
            )
          }

          if (playlists.isNotEmpty()) {
            item(key = "playlists_header") {
              SectionHeader(
                title = stringResource(R.string.ui_playlists),
                countText = stringResource(R.string.profile_count_items, playlists.size),
                onViewAll = { backStack.navigateTo(PlaylistScreen) },
              )
            }
            item(key = "playlists_shelf") {
              // Mockup `.plcards`: a shelf of clip-stacked paper sheets, one per playlist.
              LazyRow(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                items(playlists.take(SHELF_LIMIT), key = { it.playlist.id }) { entry ->
                  ProfilePlaylistClipCard(
                    playlist = entry.playlist,
                    onClick = { backStack.navigateTo(PlaylistDetailScreen(entry.playlist.id)) },
                  )
                }
              }
            }
          }

          item(key = "recents_header") {
            SectionHeader(
              title = stringResource(R.string.pref_advanced_enable_recently_played_title),
              countText =
                if (enableRecentlyPlayed && recentItems.isNotEmpty()) {
                  stringResource(R.string.profile_count_items, recentItems.size)
                } else {
                  null
                },
              onViewAll = { backStack.navigateTo(RecentlyPlayedScreen) },
            )
          }
          item(key = "recents_shelf") {
            when {
              !enableRecentlyPlayed ->
                ShelfEmptyCard(
                  icon = Icons.RoundedFilled.History,
                  title = stringResource(R.string.ui_recently_played_disabled),
                  message = stringResource(R.string.ui_recently_played_disabled_message),
                )

              recentShelf.isEmpty() ->
                ShelfEmptyCard(
                  icon = Icons.RoundedFilled.History,
                  title = stringResource(R.string.ui_no_recently_played_videos),
                  message = stringResource(R.string.profile_recents_empty_message),
                )

              else -> {
                // Mockup `.tstubs`: a fixed 3-column grid of film frames — one dark strip,
                // the rest developed positives. Playlists ride in their own shelf above.
                Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  recentShelf
                    .filterIsInstance<RecentlyPlayedItem.VideoItem>()
                    .take(3)
                    .forEachIndexed { index, item ->
                      ProfileFilmFrameCard(
                        video = item.video,
                        showThumbnail = showRecentThumbnails,
                        dark = index == 0,
                        modifier = Modifier.weight(1f),
                        onClick = { playRecent(item.video) },
                        onLongClick = {
                          haptics.pickup()
                          activeVideoItem = item
                        },
                      )
                    }
                }
              }
            }
          }

          item(key = "snapshots_header") {
            SectionHeader(
              title = stringResource(R.string.ui_snapshots),
              countText = stringResource(R.string.profile_count_shots, snapshots.size).takeIf { snapshots.isNotEmpty() },
              onViewAll = { backStack.navigateTo(SnapshotScreen) },
            )
          }
          item(key = "snapshots_shelf") {
            if (snapshots.isEmpty()) {
              ShelfEmptyCard(
                icon = Icons.RoundedFilled.Image,
                title = stringResource(R.string.snapshot_empty_title),
                message = stringResource(R.string.snapshot_empty_message),
              )
            } else {
              // Mockup `.profpolars`: small taped polaroids riding one horizontal shelf.
              LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
              ) {
                itemsIndexed(snapshots.take(SHELF_LIMIT), key = { _, capture -> capture.id }) { index, capture ->
                  LaunchedEffect(capture.id) { snapshotViewModel.loadThumbnail(capture) }
                  ProfilePolaroidCard(
                    capture = capture,
                    thumbnail = snapshotThumbnails[capture.id],
                    onClick = {
                      backStack.navigateTo(
                        SnapshotDetailScreen(
                          captures = snapshots.map { it.toDetailItem() },
                          initialIndex = index,
                        ),
                      )
                    },
                  )
                }
              }
            }
          }
          item(key = "watch_statistics") {
            ProfileWatchStatistics()
          }
          item(key = "profile_motto") {
            Text(
              text = stringResource(R.string.profile_motto),
              fontSize = 11.sp,
              letterSpacing = 2.sp,
              color = LocalPaperColors.current.inkTertiary,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
              modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp),
            )
          }
        }
      }
    }

    activeVideoItem?.let { item ->
      RecentVideoActionsSheet(
        video = item.video,
        onDismiss = { activeVideoItem = null },
        onPlay = {
          activeVideoItem = null
          playRecent(item.video)
        },
        onShare = {
          activeVideoItem = null
          MediaUtils.shareVideos(context, listOf(item.video))
        },
        onRemove = {
          activeVideoItem = null
          scope.launch { recentsViewModel.deleteVideosFromHistory(listOf(item.video)) }
        },
      )
    }

    if (showProfileDialog) {
      ProfileEditDialog(
        initialName = profileName,
        hasPhoto = profileImagePath.isNotBlank(),
        onDismiss = { showProfileDialog = false },
        onSave = { name, newPhoto, removePhoto ->
          showProfileDialog = false
          appearancePreferences.profileName.set(name.trim())
          if (newPhoto != null || removePhoto) {
            scope.launch {
              val path = withContext(Dispatchers.IO) { storeProfileAvatar(context, newPhoto) }
              appearancePreferences.profileImagePath.set(path)
            }
          }
        },
      )
    }

    if (showPrivacyDialog) {
      AlertDialog(
        onDismissRequest = { showPrivacyDialog = false },
        title = { Text(stringResource(R.string.profile_privacy_note)) },
        text = { Text(stringResource(R.string.profile_privacy_explanation)) },
        confirmButton = {
          TextButton(onClick = { showPrivacyDialog = false }) {
            Text(stringResource(R.string.generic_close))
          }
        },
      )
    }
  }
}

/** The user's avatar, or the account glyph when none is set. Shared with the navigation pill and header. */
@Composable
internal fun ProfileAvatar(
  size: Dp,
  tint: Color,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  containerColor: Color? = null,
) {
  val path by koinInject<AppearancePreferences>().profileImagePath.collectAsState()
  val avatar = rememberProfileAvatar(path)
  if (avatar != null) {
    Image(
      bitmap = avatar,
      contentDescription = contentDescription,
      contentScale = ContentScale.Crop,
      modifier = modifier.size(size).clip(CircleShape),
    )
  } else if (containerColor != null) {
    Surface(
      shape = CircleShape,
      color = containerColor,
      modifier = modifier.size(size),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          Icons.RoundedFilled.Person,
          contentDescription = contentDescription,
          tint = tint,
          modifier = Modifier.size(size * 0.54f),
        )
      }
    }
  } else {
    Icon(
      PaperIcons.User,
      contentDescription = contentDescription,
      tint = tint,
      modifier = modifier.size(size),
    )
  }
}

@Composable
private fun rememberProfileAvatar(path: String): ImageBitmap? {
  val avatar by produceState<ImageBitmap?>(initialValue = null, path) {
    value =
      if (path.isBlank()) {
        null
      } else {
        withContext(Dispatchers.IO) {
          runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull()
        }
      }
  }
  return avatar
}

/**
 * 档案卡 per mockup `.profcard`: a bookplate — cream reel-emblem badge carrying the edit key,
 * the serif archive title with the privacy chip, and the vermilion 私藏 seal stamped at the
 * upper right.
 */
@Composable
private fun ProfileArchiveCard(
  name: String,
  avatarPath: String,
  onEditProfile: () -> Unit,
) {
  val paperColors = LocalPaperColors.current
  val configuration = LocalConfiguration.current
  val isTablet = configuration.smallestScreenWidthDp >= 600
  val horizontalPadding = if (isTablet) 24.dp else 20.dp
  val cardShape = RoundedCornerShape(14.dp)
  // Mockup `.pav`: the user's avatar image when one is set, the reel emblem otherwise.
  val avatar = rememberProfileAvatar(avatarPath)

  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(start = horizontalPadding, end = horizontalPadding, top = 6.dp, bottom = 4.dp)
        .paperRaise(cardShape, paperColors)
        .border(1.dp, paperColors.cardLine, cardShape)
        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 15.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // .pav 藏书票徽章 + 编辑键 (22dp key on a 30dp touch target, overhanging like the mock).
    Box(contentAlignment = Alignment.BottomEnd) {
      Box(
        modifier =
          Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .drawBehind {
              drawRect(
                Brush.radialGradient(
                  listOf(Color(0xFFFCF4E2), Color(0xFFF0E4C8), Color(0xFFDCCBA2)),
                  center = Offset(size.width * 0.32f, size.height * 0.24f),
                  radius = size.width * 1.2f,
                ),
              )
            }
            .border(1.dp, if (paperColors.isDark) paperColors.cardLine else Color(0xFFD6C49A), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
      ) {
        val bitmap = avatar
        if (bitmap != null) {
          Image(
            bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
          )
        } else {
          BookplateEmblem(modifier = Modifier.size(34.dp))
        }
      }
      Box(
        modifier =
          Modifier
            .size(30.dp)
            .offset(x = 4.dp, y = 4.dp)
            .clickable(onClick = onEditProfile),
        contentAlignment = Alignment.Center,
      ) {
        Box(
          modifier =
            Modifier
              .size(22.dp)
              .paperRaise(CircleShape, paperColors, elevation = 2.dp)
              .border(1.5.dp, paperColors.cardLine, CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            Icons.RoundedFilled.Edit,
            contentDescription = stringResource(R.string.profile_edit),
            tint = paperColors.inkSecondary,
            modifier = Modifier.size(11.dp),
          )
        }
      }
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = name.ifBlank { stringResource(R.string.profile_archive_title) },
        modifier = Modifier.semantics { heading() },
        fontFamily = FontFamily.Serif,
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        color = paperColors.ink,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      Box(
        modifier =
          Modifier
            .padding(top = 7.dp)
            .paperInset(RoundedCornerShape(999.dp), paperColors),
      ) {
        Text(
          text = stringResource(R.string.profile_privacy_note),
          fontSize = 10.sp,
          letterSpacing = 0.5.sp,
          color = paperColors.inkSecondary,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
        )
      }
    }

    // .seal 朱砂「私藏」印章: 7° tilt, double vermilion ring, serif characters.
    Box(
      modifier =
        Modifier
          .size(46.dp)
          .graphicsLayer { rotationZ = 7f }
          .border(2.dp, paperColors.red.copy(alpha = 0.72f), RoundedCornerShape(9.dp))
          .padding(2.dp)
          .border(1.5.dp, paperColors.red.copy(alpha = 0.26f), RoundedCornerShape(7.dp))
          .background(paperColors.red.copy(alpha = 0.05f), RoundedCornerShape(7.dp)),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = stringResource(R.string.profile_seal),
        fontFamily = FontFamily.Serif,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 16.sp,
        letterSpacing = 1.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        color = paperColors.red.copy(alpha = 0.88f),
      )
    }
  }
}

/**
 * The reel emblem redrawn in vermilion on the cream bookplate (mockup `.pav`): same symbol
 * geometry as [ReelPaperLogoBadge], but stroked/filled with the paper red instead of enamel.
 */
@Composable
private fun BookplateEmblem(modifier: Modifier = Modifier) {
  val colors = LocalPaperColors.current
  androidx.compose.foundation.Canvas(modifier = modifier) {
    val s = size.width / 30f
    fun sx(v: Float) = (v - 9f) * s
    val red = colors.red
    // Disc outline + engraved inner ring.
    drawCircle(red, radius = 12.3f * s, center = Offset(sx(24f), sx(24f)), style = Stroke(width = 1.2f * s))
    drawCircle(
      red.copy(alpha = 0.3f),
      radius = 11f * s,
      center = Offset(sx(24f), sx(24f)),
      style = Stroke(width = 0.6f * s),
    )
    listOf(
      Offset(sx(24f), sx(15.2f)),
      Offset(sx(16.38f), sx(28.4f)),
      Offset(sx(31.62f), sx(28.4f)),
    ).forEach { c ->
      drawCircle(red, radius = 2.9f * s, center = c)
    }
    val wing =
      Path().apply {
        moveTo(sx(20.66f), sx(19.36f))
        lineTo(sx(20.66f), sx(28.64f))
        lineTo(sx(29.07f), sx(24f))
        close()
      }
    drawPath(wing, red)
  }
}

@Composable
private fun SectionHeader(
  title: String,
  countText: String? = null,
  onViewAll: () -> Unit,
) {
  val configuration = LocalConfiguration.current
  val isTablet = configuration.smallestScreenWidthDp >= 600
  val paperColors = LocalPaperColors.current
  // Mockup `.sec`: letter-spaced tertiary title, with the count and 查看全部 fused into
  // one secondary-ink `.more` action on the right.
  Row(
    modifier = Modifier.fillMaxWidth().padding(start = if (isTablet) 24.dp else 22.dp, end = if (isTablet) 24.dp else 22.dp, top = 20.dp, bottom = 10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = title,
      modifier = Modifier.alignByBaseline().semantics { heading() },
      fontSize = 12.5.sp,
      letterSpacing = 3.sp,
      fontWeight = FontWeight.ExtraBold,
      color = paperColors.inkTertiary,
    )
    Row(
      modifier =
        Modifier
          .alignByBaseline()
          .clip(RoundedCornerShape(8.dp))
          .clickable(onClick = onViewAll)
          .padding(horizontal = 2.dp, vertical = 2.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = buildString {
          if (countText != null) {
            append(countText)
            append(" · ")
          }
          append(stringResource(R.string.profile_view_all))
          append(" ›")
        },
        fontSize = 11.5.sp,
        letterSpacing = 1.sp,
        color = paperColors.inkSecondary,
      )
    }
  }
}

@Composable
private fun ShelfEmptyCard(
  icon: AppIcon,
  title: String,
  message: String,
) {
  val configuration = LocalConfiguration.current
  val isTablet = configuration.smallestScreenWidthDp >= 600
  Surface(
    shape = AppShapeScale.large,
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
    modifier = Modifier.fillMaxWidth().padding(horizontal = if (isTablet) 24.dp else 16.dp, vertical = 2.dp),
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.size(40.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
      }
      Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.weight(1f),
      ) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}

/**
 * 播放列表剧照夹 per mockup `.plcard`: a clip-stacked paper listing — two sheets fanned out
 * beneath a titled top sheet held by a vermilion binder clip — with the video count and the
 * source line centred underneath.
 */
@Composable
private fun ProfilePlaylistClipCard(
  playlist: PlaylistEntity,
  onClick: () -> Unit,
) {
  val paperColors = LocalPaperColors.current
  val isFavorites = playlist.name.equals(PlaylistRepository.FAVORITES_PLAYLIST_NAME, ignoreCase = true)
  val title =
    when {
      !isFavorites -> playlist.name
      playlist.isAudio -> stringResource(R.string.playlist_favorite_songs)
      else -> stringResource(R.string.playlist_favorite_videos)
    }
  val sheetBorder = if (paperColors.isDark) paperColors.cardLine else Color(0xFFE0D5B8)
  val sheetShape = RoundedCornerShape(4.dp)
  val topSheetGradient =
    if (paperColors.isDark) {
      Brush.verticalGradient(listOf(paperColors.card, paperColors.paperInset))
    } else {
      Brush.verticalGradient(listOf(Color(0xFFFDFBF3), Color(0xFFF3EDDC)))
    }
  val underSheetGradient =
    if (paperColors.isDark) {
      Brush.verticalGradient(listOf(paperColors.paperInset, paperColors.card))
    } else {
      Brush.verticalGradient(listOf(Color(0xFFF5EDDA), Color(0xFFEDE2C9)))
    }

  Column(modifier = Modifier.width(106.dp).clickable(onClick = onClick)) {
    // .cst: the 76dp stack area — sheets peek out below and behind the top card.
    Box(modifier = Modifier.fillMaxWidth().height(76.dp)) {
      ClipStackSheet(
        gradient = underSheetGradient,
        border = sheetBorder,
        shape = sheetShape,
        modifier = Modifier.offset(x = 0.dp, y = 18.dp).graphicsLayer { rotationZ = 1.2f },
      )
      ClipStackSheet(
        gradient = underSheetGradient,
        border = sheetBorder,
        shape = sheetShape,
        modifier = Modifier.offset(x = 2.dp, y = 9.dp).graphicsLayer { rotationZ = -1.4f },
      )
      Box(
        modifier =
          Modifier
            .offset(x = 5.dp)
            .size(width = 96.dp, height = 54.dp)
            .paperRaise(sheetShape, paperColors, elevation = 4.dp)
            .background(topSheetGradient, sheetShape)
            .border(1.dp, sheetBorder, sheetShape),
      ) {
        // .cst-top::before: the ruled lines of a written listing.
        Canvas(modifier = Modifier.fillMaxSize()) {
          val lineColor =
            if (paperColors.isDark) {
              paperColors.ink.copy(alpha = 0.10f)
            } else {
              Color(0xFF544224).copy(alpha = 0.10f)
            }
          val bottom = size.height - 5.dp.toPx()
          var y = 30.dp.toPx()
          while (y <= bottom) {
            drawLine(
              color = lineColor,
              start = Offset(7.dp.toPx(), y),
              end = Offset(size.width - 7.dp.toPx(), y),
              strokeWidth = 1.dp.toPx(),
            )
            y += 7.dp.toPx()
          }
        }
        Text(
          text = title,
          fontFamily = FontFamily.Serif,
          fontStyle = FontStyle.Italic,
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp,
          // Explicit tight line boxes: the ambient style's 24sp leading would push the caption
          // off the design's 11dp inset.
          lineHeight = 12.sp,
          letterSpacing = 0.4.sp,
          color = paperColors.ink,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center,
          modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 11.dp),
        )
      }
      BinderClip(modifier = Modifier.offset(x = 10.dp, y = (-7).dp))
    }
  }
}

/** Mockup `.cst i`: one of the listing sheets fanned beneath the titled top sheet. */
@Composable
private fun ClipStackSheet(
  gradient: Brush,
  border: Color,
  shape: Shape,
  modifier: Modifier = Modifier,
) {
  val paperColors = LocalPaperColors.current
  Box(
    modifier =
      modifier
        .size(width = 96.dp, height = 54.dp)
        .paperRaise(shape, paperColors, elevation = 1.5.dp)
        .background(gradient, shape)
        .border(1.dp, border, shape),
  )
}

/** Mockup `.clip`: the vermilion long-tail clip pinning a sheet stack. */
@Composable
private fun BinderClip(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier.size(width = 20.dp, height = 14.dp)) {
    val s = size.width / 20f
    // The wire handle arcs above the clip's body and is clipped away at the bottom.
    val handle =
      Path().apply {
        val left = 4.5f * s
        val right = 15.5f * s
        val top = 0.5f * s
        val bottom = 7f * s
        val radius = 5.5f * s
        moveTo(left, bottom)
        lineTo(left, top + radius)
        quadraticTo(left, top, left + radius, top)
        lineTo(right - radius, top)
        quadraticTo(right, top, right, top + radius)
        lineTo(right, bottom)
      }
    drawPath(handle, Color(0xFFC05046), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    drawRoundRect(
      brush = Brush.verticalGradient(listOf(Color(0xFFDE7A5E), Color(0xFFB23129), Color(0xFF9C2A22))),
      topLeft = Offset(0f, 6f * s),
      size = Size(20f * s, 5.5f * s),
      cornerRadius = CornerRadius(2.8f * s),
    )
  }
}

/** Fallback hues (mockup `--g1/--g2`) for frames whose thumbnail is unavailable. */
private val FILM_FRAME_GRADIENTS =
  listOf(
    Color(0xFFDCE6EE) to Color(0xFF7A93A8),
    Color(0xFFE8C590) to Color(0xFF8A6B3F),
    Color(0xFFB9A0D0) to Color(0xFF5A3F78),
    Color(0xFFF2A24C) to Color(0xFFB23C22),
    Color(0xFF7FB1A6) to Color(0xFF3F6B66),
    Color(0xFF55617A) to Color(0xFF232A35),
    Color(0xFFD98F8F) to Color(0xFF6B2A20),
    Color(0xFFC9A0C0) to Color(0xFF6B3F66),
    Color(0xFFEFC07A) to Color(0xFFB96A3A),
    Color(0xFFD8E2E8) to Color(0xFF8FA3B0),
  )

/**
 * 胶片帧 per mockup `.fframe`: a 16:10 frame with sprocket holes punched top and bottom, the
 * play glyph and duration chip on the still, and the serif-italic file name on the film edge.
 * The newest entry is developed as a dark strip; the rest are cream positives.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileFilmFrameCard(
  video: Video,
  showThumbnail: Boolean,
  dark: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val paperColors = LocalPaperColors.current
  val thumbnailRepository = koinInject<ThumbnailRepository>()
  val density = LocalDensity.current
  val widthPx = with(density) { 160.dp.roundToPx() }
  val heightPx = widthPx * 10 / 16

  var thumbnail by remember(video.path, showThumbnail) {
    mutableStateOf(if (showThumbnail) thumbnailRepository.peekThumbnailFromMemory(video, widthPx, heightPx) else null)
  }
  LaunchedEffect(video.path, showThumbnail) {
    if (showThumbnail && thumbnail == null) {
      thumbnail = thumbnailRepository.getThumbnail(video, widthPx, heightPx)
    }
  }

  val fallback =
    remember(video.displayName) {
      FILM_FRAME_GRADIENTS[Math.floorMod(video.displayName.hashCode(), FILM_FRAME_GRADIENTS.size)]
    }
  val duration = video.durationFormatted
  val shape = RoundedCornerShape(8.dp)
  val frameGradient =
    if (dark) {
      Brush.verticalGradient(listOf(Color(0xFF514A39), Color(0xFF322C22)))
    } else {
      Brush.verticalGradient(listOf(Color(0xFFFBF6E7), Color(0xFFEFE6CC)))
    }

  Column(
    modifier =
      modifier
        .shadow(
          elevation = 5.dp,
          shape = shape,
          clip = false,
          ambientColor = paperColors.shadowTint.copy(alpha = 0.26f),
          spotColor = paperColors.shadowTint.copy(alpha = 0.24f),
        )
        .clip(shape)
        .background(frameGradient)
        .border(1.dp, if (dark) Color(0xFF282118) else Color(0xFFDACDA8), shape)
        .combinedClickable(onClick = onClick, onLongClick = onLongClick)
        .padding(horizontal = 5.dp, vertical = 6.dp),
  ) {
    FilmPerfStrip(dark = dark)

    Box(
      modifier =
        Modifier
          .padding(horizontal = 2.dp, vertical = 5.dp)
          .fillMaxWidth()
          .aspectRatio(16f / 10f)
          .clip(RoundedCornerShape(3.dp))
          .background(Brush.linearGradient(listOf(fallback.first, fallback.second)))
          .border(
            1.dp,
            if (dark) Color.Black.copy(alpha = 0.55f) else Color(0xFF544224).copy(alpha = 0.32f),
            RoundedCornerShape(3.dp),
          ),
      contentAlignment = Alignment.Center,
    ) {
      val bitmap = thumbnail
      if (bitmap != null) {
        Image(
          bitmap.asImageBitmap(),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      }
      // .timg::before / ::after: the glossy highlight and the bottom vignette over the still.
      Canvas(modifier = Modifier.matchParentSize()) {
        drawRect(
          Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = if (dark) 0.18f else 0.30f), Color.Transparent),
            center = Offset(size.width * 0.28f, size.height * 0.18f),
            radius = size.width * 0.75f,
          ),
        )
        drawRect(
          Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0xFF160E08).copy(alpha = if (dark) 0.20f else 0.13f)),
            startY = size.height * 0.7f,
          ),
        )
      }
      // .pg drop-shadow: a dark under-copy keeps the glyph readable on bright stills.
      Icon(
        Icons.RoundedFilled.PlayArrow,
        contentDescription = null,
        tint = Color(0xFF160E08).copy(alpha = 0.55f),
        modifier = Modifier.offset(y = 1.dp).size(17.dp).graphicsLayer {
          scaleX = 1.3f
          scaleY = 1.3f
        },
      )
      Icon(
        Icons.RoundedFilled.PlayArrow,
        contentDescription = null,
        tint = Color(0xFFFFF8E6).copy(alpha = 0.94f),
        modifier = Modifier.size(17.dp),
      )
      if (duration.isNotBlank() && duration != "--:--") {
        Text(
          text = duration,
          fontSize = 10.sp,
          lineHeight = 12.sp,
          color = Color(0xFFFDF6E6),
          modifier =
            Modifier
              .align(Alignment.BottomEnd)
              .padding(6.dp)
              .background(Color(0xFF140F0A).copy(alpha = 0.55f), RoundedCornerShape(5.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp),
        )
      }
    }

    FilmPerfStrip(dark = dark)

    Text(
      text = video.displayName.substringBeforeLast('.'),
      fontFamily = FontFamily.Serif,
      fontStyle = FontStyle.Italic,
      fontWeight = FontWeight.Bold,
      fontSize = 10.5.sp,
      lineHeight = 12.sp,
      letterSpacing = 0.3.sp,
      color = if (dark) Color(0xFFCFC3A4) else Color(0xFF8F8471),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center,
      modifier = Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, top = 5.dp, bottom = 1.dp),
    )
  }
}

/** Mockup `.perf`: nine sprocket holes punched along a film edge. */
@Composable
private fun FilmPerfStrip(dark: Boolean) {
  Canvas(modifier = Modifier.fillMaxWidth().height(11.dp)) {
    val hole = 5.dp.toPx()
    val inset = 2.dp.toPx()
    val gap = ((size.width - inset * 2 - hole * 9) / 8f).coerceAtLeast(0f)
    val y = (size.height - hole) / 2f
    val holeGradient =
      if (dark) {
        Brush.verticalGradient(listOf(Color(0xFF8B7D60), Color(0xFFF3EDE0)), startY = y, endY = y + hole)
      } else {
        Brush.verticalGradient(listOf(Color(0xFFA99A77), Color(0xFFD5C7A2)), startY = y, endY = y + hole)
      }
    repeat(9) { index ->
      drawRoundRect(
        brush = holeGradient,
        topLeft = Offset(inset + index * (hole + gap), y),
        size = Size(hole, hole),
        cornerRadius = CornerRadius(1.5.dp.toPx()),
      )
    }
  }
}

/**
 * 拍立得 per mockup `.ppolaroid`: a 106dp cream print with a strip of tape on top, the 16:10
 * snapshot with a play/duration chip, and the serif-italic caption band.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ProfilePolaroidCard(
  capture: FrameCapture,
  thumbnail: Bitmap?,
  onClick: () -> Unit,
) {
  val paperColors = LocalPaperColors.current
  val tilt = ((capture.id % 3) - 1) * 1.8f
  val shape = RoundedCornerShape(5.dp)
  Box(modifier = Modifier.width(106.dp)) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .graphicsLayer { rotationZ = tilt }
          .paperRaise(shape, paperColors, elevation = 6.dp)
          .clip(shape)
          .combinedClickable(onClick = onClick)
          .border(1.dp, if (paperColors.isDark) paperColors.cardLine else Color(0xFFE0D5B8), shape)
          .padding(start = 7.dp, end = 7.dp, top = 7.dp),
    ) {
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(RoundedCornerShape(2.dp))
            .background(paperColors.paperInset),
        contentAlignment = Alignment.Center,
      ) {
        if (thumbnail != null) {
          Image(
            thumbnail.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
          )
        } else {
          Icon(
            Icons.RoundedFilled.Image,
            contentDescription = null,
            tint = paperColors.inkTertiary,
            modifier = Modifier.size(26.dp),
          )
        }
        // .polbadge: play glyph + position chip pinned to the photo's lower right.
        Row(
          modifier =
            Modifier
              .align(Alignment.BottomEnd)
              .padding(7.dp)
              .background(Color(0xFF140F0A).copy(alpha = 0.55f), RoundedCornerShape(6.dp))
              .padding(horizontal = 7.dp, vertical = 2.5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            Icons.RoundedFilled.PlayArrow,
            contentDescription = null,
            tint = Color(0xFFFDF6E6),
            modifier = Modifier.size(10.dp),
          )
          Text(
            text = capture.formattedPosition,
            fontSize = 10.5.sp,
            color = Color(0xFFFDF6E6),
          )
        }
      }
      Text(
        text = capture.videoTitle.ifBlank { capture.formattedPosition },
        fontFamily = FontFamily.Serif,
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        fontSize = 12.5.sp,
        color = paperColors.inkSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 13.dp, start = 4.dp, end = 4.dp),
      )
    }
    // 一条米色胶带斜贴在打印纸的顶缘。
    Box(
      modifier =
        Modifier
          .align(Alignment.TopCenter)
          .offset(y = (-10).dp)
          .size(width = 64.dp, height = 19.dp)
          .graphicsLayer { rotationZ = -2.5f }
          .background(
            Brush.verticalGradient(
              listOf(Color(0xFFE9DDBE).copy(alpha = 0.8f), Color(0xFFD5C49D).copy(alpha = 0.65f)),
            ),
            RoundedCornerShape(2.dp),
          ),
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentVideoActionsSheet(
  video: Video,
  onDismiss: () -> Unit,
  onPlay: () -> Unit,
  onShare: () -> Unit,
  onRemove: () -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState =
      rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
      ),
    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
      Text(
        text = video.displayName,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
      )
      HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
      SheetAction(Icons.RoundedFilled.PlayArrow, stringResource(R.string.ui_play), MaterialTheme.colorScheme.onSurface, onPlay)
      if (!video.path.contains("://")) {
        SheetAction(Icons.RoundedFilled.Share, stringResource(R.string.generic_share), MaterialTheme.colorScheme.onSurface, onShare)
      }
      SheetAction(Icons.RoundedFilled.Delete, stringResource(R.string.profile_remove_from_history), MaterialTheme.colorScheme.error, onRemove)
    }
  }
}

@Composable
private fun SheetAction(
  icon: AppIcon,
  label: String,
  tint: Color,
  onClick: () -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .clip(AppShapeScale.medium)
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
    Text(label, style = MaterialTheme.typography.bodyLarge, color = tint)
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileEditDialog(
  initialName: String,
  hasPhoto: Boolean,
  onDismiss: () -> Unit,
  onSave: (name: String, newPhoto: Bitmap?, removePhoto: Boolean) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val currentPath by koinInject<AppearancePreferences>().profileImagePath.collectAsState()
  val currentAvatar = rememberProfileAvatar(currentPath)
  var name by rememberSaveable { mutableStateOf(initialName) }
  var pendingPhoto by remember { mutableStateOf<Bitmap?>(null) }
  var rawCropBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var removePhoto by remember { mutableStateOf(false) }
  val pendingImage = remember(pendingPhoto) { pendingPhoto?.asImageBitmap() }
  val preview = pendingImage ?: currentAvatar.takeUnless { removePhoto }
  val showsPhoto = pendingPhoto != null || (hasPhoto && !removePhoto)

  val picker =
    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
      if (uri != null) {
        scope.launch {
          val decoded = withContext(Dispatchers.IO) { decodeRawProfileAvatar(context, uri) }
          if (decoded != null) {
            rawCropBitmap = decoded
          }
        }
      }
    }

  if (rawCropBitmap != null) {
    ProfileAvatarCropDialog(
      sourceBitmap = rawCropBitmap!!,
      onDismiss = { rawCropBitmap = null },
      onCropped = { cropped ->
        rawCropBitmap = null
        pendingPhoto = cropped
        removePhoto = false
      },
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.profile_edit)) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Surface(
          onClick = { picker.launch("image/*") },
          shape = CircleShape,
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.size(96.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            if (preview != null) {
              Image(preview, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
              Icon(
                Icons.RoundedFilled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(52.dp),
              )
            }
          }
        }
        // Three buttons don't fit one row in the dialog width once a photo exists; a plain Row
        // squeezed the last one into a thin vertical pill. FlowRow wraps it onto its own line.
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          OutlinedButton(onClick = { picker.launch("image/*") }) {
            Text(stringResource(R.string.profile_choose_photo), maxLines = 1)
          }
          if (showsPhoto) {
            OutlinedButton(
              onClick = {
                val toCrop = pendingPhoto ?: currentPath.takeIf { it.isNotBlank() }?.let {
                  BitmapFactory.decodeFile(it)
                }
                if (toCrop != null) {
                  rawCropBitmap = toCrop
                }
              },
            ) {
              Text(stringResource(R.string.clip_crop), maxLines = 1)
            }
            OutlinedButton(
              onClick = {
                pendingPhoto = null
                removePhoto = true
              },
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
              ),
              border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
              ),
            ) {
              Text(stringResource(R.string.profile_remove_photo), maxLines = 1)
            }
          }
        }
        OutlinedTextField(
          value = name,
          onValueChange = { name = it.take(40) },
          label = { Text(stringResource(R.string.profile_display_name)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      TextButton(onClick = { onSave(name, pendingPhoto, removePhoto) }) {
        Text(stringResource(R.string.ui_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.generic_cancel)) }
    },
  )
}

@Composable
private fun ProfileAvatarCropDialog(
  sourceBitmap: Bitmap,
  onDismiss: () -> Unit,
  onCropped: (Bitmap) -> Unit,
) {
  val configuration = LocalConfiguration.current
  val density = LocalDensity.current
  val isTablet = configuration.smallestScreenWidthDp >= 600

  val cropBoxDp = minOf(configuration.screenWidthDp.dp - 64.dp, if (isTablet) 360.dp else 280.dp)
  val cropBoxPx = with(density) { cropBoxDp.toPx() }

  var rotation by remember { mutableStateOf(0) }
  var userScale by remember { mutableStateOf(1f) }
  var userOffset by remember { mutableStateOf(Offset.Zero) }

  val rotatedBitmap = remember(sourceBitmap, rotation) {
    if (rotation % 360 != 0) {
      val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
      Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.width, sourceBitmap.height, matrix, true)
    } else {
      sourceBitmap
    }
  }

  val rotatedImageBitmap = remember(rotatedBitmap) { rotatedBitmap.asImageBitmap() }

  val baseScale = remember(rotatedImageBitmap, cropBoxPx) {
    maxOf(cropBoxPx / rotatedImageBitmap.width, cropBoxPx / rotatedImageBitmap.height)
  }

  // Calculate clamp boundaries so the image always completely covers the crop box
  val maxPanX = remember(userScale, baseScale, rotatedImageBitmap.width, cropBoxPx) {
    ((rotatedImageBitmap.width * baseScale * userScale - cropBoxPx) / 2f).coerceAtLeast(0f)
  }
  val maxPanY = remember(userScale, baseScale, rotatedImageBitmap.height, cropBoxPx) {
    ((rotatedImageBitmap.height * baseScale * userScale - cropBoxPx) / 2f).coerceAtLeast(0f)
  }

  // Keep offset clamped when zoom changes
  LaunchedEffect(maxPanX, maxPanY) {
    userOffset = Offset(
      userOffset.x.coerceIn(-maxPanX, maxPanX),
      userOffset.y.coerceIn(-maxPanY, maxPanY),
    )
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier =
        Modifier
          .fillMaxWidth(if (isTablet) 0.65f else 0.94f)
          .clip(AppShapeScale.large),
      shape = AppShapeScale.large,
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.clip_crop),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.RoundedFilled.Close, contentDescription = stringResource(R.string.generic_cancel))
          }
        }

        // Viewport
        Box(
          modifier =
            Modifier
              .size(cropBoxDp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.Black)
              .pointerInput(rotatedImageBitmap, cropBoxPx) {
                detectTransformGestures { _, pan, zoom, _ ->
                  userScale = (userScale * zoom).coerceIn(1f, 5f)
                  val currentMaxPanX = ((rotatedImageBitmap.width * baseScale * userScale - cropBoxPx) / 2f).coerceAtLeast(0f)
                  val currentMaxPanY = ((rotatedImageBitmap.height * baseScale * userScale - cropBoxPx) / 2f).coerceAtLeast(0f)
                  userOffset = Offset(
                    (userOffset.x + pan.x).coerceIn(-currentMaxPanX, currentMaxPanX),
                    (userOffset.y + pan.y).coerceIn(-currentMaxPanY, currentMaxPanY),
                  )
                }
              },
          contentAlignment = Alignment.Center,
        ) {
          Image(
            bitmap = rotatedImageBitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
              Modifier
                .size(
                  width = with(density) { (rotatedImageBitmap.width * baseScale).toDp() },
                  height = with(density) { (rotatedImageBitmap.height * baseScale).toDp() },
                )
                .graphicsLayer {
                  scaleX = userScale
                  scaleY = userScale
                  translationX = userOffset.x
                  translationY = userOffset.y
                },
          )

          // Circular crop cutout mask overlay
          Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val radius = minOf(width, height) / 2f

            val path = Path().apply {
              fillType = PathFillType.EvenOdd
              addRect(Rect(0f, 0f, width, height))
              addOval(Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius))
            }
            drawPath(path, color = Color.Black.copy(alpha = 0.60f))

            drawCircle(
              color = Color.White.copy(alpha = 0.85f),
              radius = radius - 1f,
              center = center,
              style = Stroke(width = 2.dp.toPx()),
            )
          }
        }

        // Toolbar: Rotate Left, Rotate Right & Reset
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          FilledTonalButton(
            onClick = {
              rotation = (rotation - 90 + 360) % 360
              userScale = 1f
              userOffset = Offset.Zero
            },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          ) {
            Icon(Icons.RoundedFilled.RotateLeft, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Left", style = MaterialTheme.typography.labelMedium)
          }

          FilledTonalButton(
            onClick = {
              rotation = (rotation + 90) % 360
              userScale = 1f
              userOffset = Offset.Zero
            },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          ) {
            Icon(Icons.RoundedFilled.RotateRight, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Right", style = MaterialTheme.typography.labelMedium)
          }

          FilledTonalButton(
            onClick = {
              rotation = 0
              userScale = 1f
              userOffset = Offset.Zero
            },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          ) {
            Icon(Icons.RoundedFilled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.generic_reset), style = MaterialTheme.typography.labelMedium)
          }
        }

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.generic_cancel))
          }
          Button(
            onClick = {
              val cropped = performCrop(
                bitmap = sourceBitmap,
                rotation = rotation,
                userScale = userScale,
                userOffset = userOffset,
                viewportSizePx = cropBoxPx,
              )
              onCropped(cropped)
            },
          ) {
            Text(stringResource(R.string.clip_crop))
          }
        }
      }
    }
  }
}

private fun performCrop(
  bitmap: Bitmap,
  rotation: Int,
  userScale: Float,
  userOffset: Offset,
  viewportSizePx: Float,
  targetSizePx: Int = AVATAR_SIZE_PX,
): Bitmap {
  val rotated = if (rotation % 360 != 0) {
    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
  } else {
    bitmap
  }

  val bw = rotated.width.toFloat()
  val bh = rotated.height.toFloat()

  val baseScale = maxOf(viewportSizePx / bw, viewportSizePx / bh)
  val totalScale = baseScale * userScale

  val cropSizeInImage = (viewportSizePx / totalScale).coerceAtMost(minOf(bw, bh))
  val cropCenterX = bw / 2f - userOffset.x / totalScale
  val cropCenterY = bh / 2f - userOffset.y / totalScale

  val srcX = (cropCenterX - cropSizeInImage / 2f).toInt().coerceIn(0, (bw - cropSizeInImage).toInt().coerceAtLeast(0))
  val srcY = (cropCenterY - cropSizeInImage / 2f).toInt().coerceIn(0, (bh - cropSizeInImage).toInt().coerceAtLeast(0))
  val srcW = cropSizeInImage.toInt().coerceAtMost(rotated.width - srcX)
  val srcH = cropSizeInImage.toInt().coerceAtMost(rotated.height - srcY)
  val cropSide = minOf(srcW, srcH).coerceAtLeast(1)

  val scaleMatrix = Matrix().apply {
    val scale = targetSizePx.toFloat() / cropSide
    postScale(scale, scale)
  }

  return Bitmap.createBitmap(rotated, srcX, srcY, cropSide, cropSide, scaleMatrix, true)
}

/** Decodes [uri] into an upright raw bitmap for cropping. */
private fun decodeRawProfileAvatar(
  context: Context,
  uri: Uri,
): Bitmap? =
  runCatching {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
    while (maxDim / (sampleSize * 2) >= 2048) sampleSize *= 2
    val decoded =
      resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sampleSize })
      } ?: return null

    val rotation =
      resolver.openInputStream(uri)?.use {
        when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
          ExifInterface.ORIENTATION_ROTATE_90 -> 90f
          ExifInterface.ORIENTATION_ROTATE_180 -> 180f
          ExifInterface.ORIENTATION_ROTATE_270 -> 270f
          else -> 0f
        }
      } ?: 0f

    if (rotation != 0f) {
      val matrix = Matrix().apply { postRotate(rotation) }
      Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    } else {
      decoded
    }
  }.getOrNull()

/** Replaces the stored avatar with [bitmap] (or clears it when null) and returns the new path. */
private fun storeProfileAvatar(
  context: Context,
  bitmap: Bitmap?,
): String {
  context.filesDir.listFiles { file -> file.name.startsWith(AVATAR_FILE_PREFIX) }?.forEach(File::delete)
  if (bitmap == null) return ""
  // A fresh name per save changes the preference value, so every avatar observer reloads.
  val file = File(context.filesDir, "$AVATAR_FILE_PREFIX${System.currentTimeMillis()}.png")
  file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
  return file.absolutePath
}
