/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.cards

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import app.gyrolet.mpvrx.presentation.components.AppDropdownMenu as DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.database.entities.PlaylistEntity
import app.gyrolet.mpvrx.database.repository.PlaylistRepository
import app.gyrolet.mpvrx.domain.media.model.Video
import app.gyrolet.mpvrx.domain.media.model.VideoFolder
import app.gyrolet.mpvrx.domain.network.NetworkProtocol
import app.gyrolet.mpvrx.domain.thumbnail.EmbeddedArtworkResolver
import app.gyrolet.mpvrx.domain.thumbnail.ThumbnailRepository
import app.gyrolet.mpvrx.domain.thumbnail.getArtwork
import app.gyrolet.mpvrx.preferences.AppearancePreferences
import app.gyrolet.mpvrx.preferences.BrowserPreferences
import app.gyrolet.mpvrx.preferences.preference.collectAsState
import app.gyrolet.mpvrx.ui.icons.AppIcon
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.ui.player.controls.components.tvContextMenu
import app.gyrolet.mpvrx.ui.player.controls.components.tvFocusHighlight
import app.gyrolet.mpvrx.ui.player.ytdlp.YtdlpManager
import app.gyrolet.mpvrx.ui.theme.paper.paperInset
import app.gyrolet.mpvrx.ui.theme.paper.paperRaise
import app.gyrolet.mpvrx.ui.theme.AppShapeScale
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

/**
 * Card for displaying a playlist item.
 *
 * Grid mode intentionally has its own YouTube-style presentation instead of reusing FolderCard:
 * 16:9 artwork, a stacked-playlist hint, an item-count badge, title/metadata below, and an
 * overflow menu. List mode keeps the existing compact folder-card presentation.
 */
@Composable
fun PlaylistCard(
  playlist: PlaylistEntity,
  itemCount: Int,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onThumbClick: () -> Unit,
  modifier: Modifier = Modifier,
  isSelected: Boolean = false,
  selectionNeighbors: MediaSelectionNeighbors = MediaSelectionNeighbors(),
  isGridMode: Boolean = false,
  thumbnail: Bitmap? = null,
  /** Source kinds present in this playlist; null means a local file. Empty for M3U playlists. */
  sources: List<NetworkProtocol?> = emptyList(),
  onRenameClick: (() -> Unit)? = null,
  onDeleteClick: (() -> Unit)? = null,
) {
  val context = LocalContext.current
  val repository = koinInject<PlaylistRepository>()
  val thumbnailRepository = koinInject<ThumbnailRepository>()
  val preferences = koinInject<BrowserPreferences>()
  val appearancePreferences = koinInject<AppearancePreferences>()
  val viewPreferences = preferences.playlistView
  val thumbnailQuality by preferences.thumbnailQuality.collectAsState()
  val thumbnailMode by preferences.thumbnailMode.collectAsState()
  val thumbnailGenerator by preferences.thumbnailGenerator.collectAsState()
  val thumbnailCacheRevision by thumbnailRepository.cacheRevision.collectAsState()
  val thumbnailFramePosition by preferences.thumbnailFramePosition.collectAsState()
  val showLocation by viewPreferences.showLocation.collectAsState()
  val sourceLocation =
    remember(playlist.m3uSourceUrl, playlist.xtreamServerUrl) {
      app.gyrolet.mpvrx.ui.browser.playlist.playlistSourceLocation(
        playlist.m3uSourceUrl ?: playlist.xtreamServerUrl,
      )
    }
  val showNetworkThumbnails by appearancePreferences.showNetworkThumbnails.collectAsState()
  val previewItems by
    remember(repository, playlist.id) {
      repository.observePlaylistPreviewItems(playlist.id, 2)
    }.collectAsState(initial = emptyList())
  val firstItem = previewItems.firstOrNull()
  val secondItem = previewItems.getOrNull(1)
  val density = LocalDensity.current
  val thumbnailWidthPx =
    with(density) {
      (if (isGridMode) 480.dp else 160.dp).roundToPx()
    }
  val isAudio = playlist.isAudio
  val thumbnailHeightPx =
    if (isGridMode) {
      (thumbnailWidthPx * 9 / 16).coerceAtLeast(1)
    } else {
      thumbnailWidthPx
    }
  // Playlist artwork is intrinsic to the playlist card, like YouTube's playlist cover, so it
  // does not follow the generic folder-thumbnail visibility toggle. The DAO gives position 0.
  // Audio prefers embedded/MediaStore cover art. Local video prefers a freshly generated frame
  // at the card's real 16:9 target so a tiny supplied artwork is never stretched across the tile.
  val resolvedThumbnail by
    produceState<Bitmap?>(
      initialValue = thumbnail,
      playlist.id,
      thumbnail,
      firstItem?.filePath,
      firstItem?.tvgLogo,
      firstItem?.addedAt,
      firstItem?.licenseType,
      thumbnailWidthPx,
      thumbnailHeightPx,
      thumbnailQuality,
      thumbnailMode,
      thumbnailGenerator,
      thumbnailCacheRevision,
      thumbnailFramePosition,
      showNetworkThumbnails,
    ) {
      value = thumbnail
      if (thumbnail != null) return@produceState
      val item = firstItem ?: return@produceState
      value =
        resolvePlaylistItemArtwork(
          context = context,
          thumbnailRepository = thumbnailRepository,
          item = item,
          isAudio = isAudio,
          thumbnailWidthPx = thumbnailWidthPx,
          thumbnailHeightPx = thumbnailHeightPx,
        )
    }

  val resolvedSecondThumbnail by
    produceState<Bitmap?>(
      initialValue = null,
      playlist.id,
      secondItem?.filePath,
      secondItem?.tvgLogo,
      secondItem?.addedAt,
      secondItem?.licenseType,
      thumbnailWidthPx,
      thumbnailHeightPx,
      thumbnailQuality,
      thumbnailMode,
      thumbnailGenerator,
      thumbnailCacheRevision,
      thumbnailFramePosition,
      showNetworkThumbnails,
    ) {
      val item = secondItem
      if (item == null) {
        value = null
        return@produceState
      }
      value =
        resolvePlaylistItemArtwork(
          context = context,
          thumbnailRepository = thumbnailRepository,
          item = item,
          isAudio = isAudio,
          thumbnailWidthPx = thumbnailWidthPx,
          thumbnailHeightPx = thumbnailHeightPx,
        )
    }

  val isFavorites =
    playlist.name.equals(PlaylistRepository.FAVORITES_PLAYLIST_NAME, ignoreCase = true)
  val displayName =
    when {
      !isFavorites -> playlist.name
      playlist.isAudio -> stringResource(R.string.playlist_favorite_songs)
      else -> stringResource(R.string.playlist_favorite_videos)
    }
  val isOnlinePlaylist =
    playlist.m3uSourceUrl?.let { source ->
      YtdlpManager.isPotentialPlaylistUrl(source) && YtdlpManager.requiresYtdlp(source)
    } == true
  val typeBadge =
    when {
      playlist.isZipPlaylist -> stringResource(R.string.playlist_zip_read_only)
      playlist.isXtreamPlaylist -> stringResource(R.string.playlist_xtream_badge)
      isOnlinePlaylist -> stringResource(R.string.playlist_online_badge)
      playlist.isM3uPlaylist -> stringResource(R.string.playlist_m3u_badge)
      else -> null
    }
  val localSourceLabel = stringResource(R.string.playlist_source_local)
  val sourceSummary =
    when {
      typeBadge != null -> typeBadge
      sources.isNotEmpty() ->
        sources
          .map { it?.displayName ?: localSourceLabel }
          .distinct()
          .joinToString(" + ")
      else -> localSourceLabel
    }
  val playlistLabel = stringResource(R.string.ui_playlist)
  val metadata = "$sourceSummary · $playlistLabel"
  val thumbnailBitmap = remember(resolvedThumbnail) { resolvedThumbnail?.asImageBitmap() }
  val secondThumbnailBitmap = remember(resolvedSecondThumbnail) { resolvedSecondThumbnail?.asImageBitmap() }
  val placeholderIcon =
    when {
      isFavorites && playlist.isAudio -> Icons.RoundedFilled.Favorite
      isFavorites -> Icons.RoundedFilled.Bookmarks
      playlist.isZipPlaylist -> Icons.RoundedFilled.FolderZip
      playlist.isXtreamPlaylist -> Icons.RoundedFilled.Tv
      playlist.isAudio && !playlist.isM3uPlaylist -> Icons.RoundedFilled.QueueMusic
      else -> Icons.RoundedFilled.PlaylistPlay
    }

  if (isGridMode) {
    YouTubePlaylistGridCard(
      displayName = displayName,
      metadata = metadata,
      itemCount = itemCount,
      thumbnail = thumbnailBitmap,
      secondThumbnail = secondThumbnailBitmap,
      placeholderIcon = placeholderIcon,
      isSelected = isSelected,
      selectionNeighbors = selectionNeighbors,
      onClick = onClick,
      onLongClick = onLongClick,
      onRenameClick = if (isFavorites) null else onRenameClick,
      onDeleteClick = if (isFavorites) null else onDeleteClick,
      modifier = modifier,
    )
    return
  }

  // Mockup `.pcard`: 叠纸卡片 — two offset paper sheets behind, an inset icon slot, the
  // count line with source chips, and a circular play key on the right.
  val paperColors = app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors.current
  val cardShape = RoundedCornerShape(14.dp)
  val countLine = androidx.compose.ui.res.pluralStringResource(R.plurals.folder_video_count, itemCount, itemCount)

  Box(modifier = modifier.padding(start = 20.dp, end = 28.dp)) {
    // 错位的两张纸 (mock ::before/::after) — deliberate stack-paper metaphor.
    Box(
      modifier =
        Modifier
          .matchParentSize()
          .graphicsLayer {
            translationX = 8.dp.toPx()
            translationY = 8.dp.toPx()
            rotationZ = -0.6f
            alpha = 0.75f
          }
          .clip(cardShape)
          .background(paperColors.card)
          .border(1.dp, paperColors.cardLine, cardShape),
    )
    Box(
      modifier =
        Modifier
          .matchParentSize()
          .graphicsLayer {
            translationX = 4.dp.toPx()
            translationY = 4.dp.toPx()
            rotationZ = 0.7f
          }
          .clip(cardShape)
          .background(paperColors.card)
          .border(1.dp, paperColors.cardLine, cardShape),
    )

    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .paperRaise(cardShape, paperColors)
          .clip(cardShape)
          .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) paperColors.red else paperColors.cardLine, cardShape)
          .combinedClickable(onClick = onClick, onLongClick = onLongClick)
          .padding(horizontal = 16.dp, vertical = 15.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
      // .pico: inset icon slot; favorites carry the vermilion heart with its red ring.
      Box(
        modifier =
          Modifier
            .size(46.dp)
            .paperInset(RoundedCornerShape(12.dp), paperColors)
            .then(
              if (isFavorites) {
                Modifier.border(1.5.dp, paperColors.red.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
              } else {
                Modifier
              },
            ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          placeholderIcon,
          contentDescription = null,
          tint = if (isFavorites) paperColors.red else paperColors.inkSecondary,
          modifier = Modifier.size(21.dp),
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = displayName,
          fontSize = 15.5.sp,
          fontWeight = FontWeight.Bold,
          color = paperColors.ink,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = countLine,
          fontSize = 11.5.sp,
          color = paperColors.inkSecondary,
          modifier = Modifier.padding(top = 3.dp),
        )
        // Source chips as inset paper pills (mock `.chip`); favorites show none.
        if (!isFavorites) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            val chipLabels =
              when {
                playlist.isZipPlaylist -> listOf(stringResource(R.string.playlist_zip_read_only), localSourceLabel)
                typeBadge != null -> listOf(typeBadge)
                else -> sources.map { it?.displayName ?: localSourceLabel }.distinct()
              }
            chipLabels.forEach { label ->
              Text(
                text = label,
                fontSize = 10.sp,
                color = paperColors.inkSecondary,
                modifier =
                  Modifier
                    .paperInset(RoundedCornerShape(7.dp), paperColors)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
              )
            }
          }
        }
        if (!playlist.isZipPlaylist && showLocation && sourceLocation.isNotBlank()) {
          Text(
            text = sourceLocation,
            fontSize = 11.sp,
            color = paperColors.inkTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
          )
        }
      }

      // .pmini: raised paper play key (circles never take a step).
      Box(
        modifier =
          Modifier
            .size(38.dp)
            .paperRaise(CircleShape, paperColors, elevation = 2.dp)
            .border(1.dp, paperColors.cardLine, CircleShape)
            .clickable(onClick = onThumbClick),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          Icons.RoundedFilled.PlayArrow,
          contentDescription = stringResource(R.string.ui_play),
          tint = paperColors.red,
          modifier = Modifier.size(17.dp),
        )
      }
    }

    SelectionIndicator(
      selected = isSelected,
      modifier = Modifier.align(Alignment.TopEnd),
    )
  }
}

private suspend fun resolvePlaylistItemArtwork(
  context: android.content.Context,
  thumbnailRepository: ThumbnailRepository,
  item: app.gyrolet.mpvrx.database.entities.PlaylistItemEntity,
  isAudio: Boolean,
  thumbnailWidthPx: Int,
  thumbnailHeightPx: Int,
): Bitmap? {
  if (app.gyrolet.mpvrx.domain.archive.ZipArchiveMedia.isPlaybackUri(item.filePath)) return null
  return withContext(Dispatchers.IO) {
    try {
      val path = item.filePath.substringBefore('|')
      val uri =
        Uri.parse(path).let {
          if (it.scheme.isNullOrBlank()) Uri.fromFile(File(path)) else it
        }
      if (app.gyrolet.mpvrx.domain.thumbnail.isLocalArtworkSource(path)) {
        return@withContext thumbnailRepository.getArtwork(path, isAudio = isAudio, widthPx = thumbnailWidthPx, heightPx = thumbnailHeightPx)
      }
      val suppliedArtwork = EmbeddedArtworkResolver.decodeArtworkUri(context, item.tvgLogo)
      val media =
        Video(
          id = item.id.toLong(),
          title = item.fileName,
          displayName = item.fileName,
          path = path,
          uri = uri,
          duration = 0L,
          durationFormatted = "",
          size = item.fileSize ?: 0L,
          sizeFormatted = "",
          dateModified = item.addedAt / 1000L,
          dateAdded = item.addedAt / 1000L,
          mimeType = if (isAudio) "audio/*" else "video/*",
          bucketId = "",
          bucketDisplayName = "",
          width = 0,
          height = 0,
          fps = 0f,
          resolution = "",
          isAudio = isAudio,
        )

      when {
        // Album art is the canonical visual for audio and may be the only image available.
        isAudio ->
          suppliedArtwork
            ?: thumbnailRepository.getThumbnail(media, thumbnailWidthPx, thumbnailHeightPx)

        // For local videos, prefer a real frame generated at the playlist card's target size.
        item.licenseType.isNullOrBlank() &&
          !path.startsWith("http://", ignoreCase = true) &&
          !path.startsWith("https://", ignoreCase = true) ->
          thumbnailRepository.getThumbnail(media, thumbnailWidthPx, thumbnailHeightPx)
            ?: suppliedArtwork

        // Network/DRM entries commonly provide their intended poster through tvgLogo.
        else ->
          suppliedArtwork
            ?: thumbnailRepository.getThumbnail(media, thumbnailWidthPx, thumbnailHeightPx)
      }
    } catch (error: CancellationException) {
      throw error
    } catch (_: Exception) {
      null
    }
  }
}

@Composable
private fun YouTubePlaylistGridCard(
  displayName: String,
  metadata: String,
  itemCount: Int,
  thumbnail: ImageBitmap?,
  secondThumbnail: ImageBitmap? = null,
  placeholderIcon: AppIcon,
  isSelected: Boolean,
  selectionNeighbors: MediaSelectionNeighbors,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onRenameClick: (() -> Unit)?,
  onDeleteClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
) {
  var menuExpanded by remember { mutableStateOf(false) }
  val selectionColor = animatedSelectionColor(isSelected)
  val cardShape = rememberMediaSelectionShape(isSelected, grid = true, neighbors = selectionNeighbors)

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(cardShape)
        .background(selectionColor)
        .semantics { selected = isSelected }
        .tvContextMenu(onLongClick)
        .mediaCardClickable(
          onClick = onClick,
          onLongClick = onLongClick,
        )
        .padding(horizontal = 2.dp, vertical = 2.dp),
  ) {
    // YouTube's playlist tiles read as a small stack instead of a single flat thumbnail.
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(top = if (itemCount > 1) 4.dp else 0.dp),
      contentAlignment = Alignment.TopCenter,
    ) {
      if (itemCount > 1) {
        Box(
          modifier =
            Modifier
              .align(Alignment.TopCenter)
              .offset(y = (-4).dp)
              .fillMaxWidth(0.90f)
              .aspectRatio(16f / 9f)
              .clip(AppShapeScale.medium)
              .background(MaterialTheme.colorScheme.surfaceContainerHighest),
          contentAlignment = Alignment.Center,
        ) {
          if (secondThumbnail != null) {
            Image(
              bitmap = secondThumbnail,
              contentDescription = null,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
            )
            Box(
              modifier =
                Modifier
                  .fillMaxSize()
                  .background(Color.Black.copy(alpha = 0.25f)),
            )
          }
        }
      }

      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(AppShapeScale.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .tvFocusHighlight(AppShapeScale.medium, focusedScale = 1.025f),
        contentAlignment = Alignment.Center,
      ) {
        if (thumbnail != null) {
          Image(
            bitmap = thumbnail,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
          )
        } else {
          Icon(
            imageVector = placeholderIcon,
            contentDescription = null,
            modifier = Modifier.size(42.dp),
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f),
          )
        }

        Surface(
          shape = AppShapeScale.small,
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.94f),
          contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
          shadowElevation = 2.dp,
          modifier =
            Modifier
              .align(Alignment.BottomEnd)
              .padding(8.dp),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.RoundedFilled.PlaylistPlay,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = itemCount.toString(),
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }

        SelectionIndicator(
          selected = isSelected,
          modifier =
            Modifier
              .align(Alignment.TopEnd)
              .padding(8.dp),
        )
      }
    }

    Spacer(Modifier.height(4.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.Top,
    ) {
      Column(
        modifier =
          Modifier
            .weight(1f)
            .padding(start = 2.dp, top = 0.dp, bottom = 2.dp),
      ) {
        Text(
          text = displayName,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.SemiBold,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
          text = metadata,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Box {
        IconButton(
          onClick = { menuExpanded = true },
          modifier = Modifier.size(32.dp),
        ) {
          Icon(
            imageVector = Icons.RoundedFilled.MoreVert,
            contentDescription = stringResource(R.string.ui_playlist_options),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
          )
        }
        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false },
        ) {
          DropdownMenuItem(
            text = { Text(stringResource(R.string.playlist_open_action)) },
            onClick = {
              menuExpanded = false
              onClick()
            },
          )
          if (onRenameClick != null) {
            DropdownMenuItem(
              text = { Text(stringResource(R.string.rename)) },
              onClick = {
                menuExpanded = false
                onRenameClick()
              },
            )
          }
          if (onDeleteClick != null) {
            DropdownMenuItem(
              text = {
                Text(
                  text = stringResource(R.string.delete),
                  color = MaterialTheme.colorScheme.error,
                )
              },
              onClick = {
                menuExpanded = false
                onDeleteClick()
              },
            )
          }
        }
      }
    }
  }
}
