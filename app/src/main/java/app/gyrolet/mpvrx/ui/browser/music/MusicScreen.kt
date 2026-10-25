/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.music

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.presentation.Screen
import app.gyrolet.mpvrx.preferences.MediaServerPreferences
import app.gyrolet.mpvrx.preferences.MusicSourceProvider
import app.gyrolet.mpvrx.preferences.preference.collectAsState
import app.gyrolet.mpvrx.ui.browser.jellyfin.JellyfinContent
import app.gyrolet.mpvrx.ui.browser.jellyfin.JellyfinViewModel
import app.gyrolet.mpvrx.ui.browser.navidrome.NavidromeContent
import app.gyrolet.mpvrx.ui.browser.navidrome.NavidromeViewModel
import app.gyrolet.mpvrx.ui.utils.LocalBackStack
import app.gyrolet.mpvrx.ui.utils.navigateTo
import app.gyrolet.mpvrx.ui.preferences.MediaServersPreferencesScreen
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

/**
 * Standalone music library page. This used to be the bottom-nav MUSIC tab; with the
 * paper dock it is reached from the Profile hub shortcuts instead.
 */
@Serializable
object MusicScreen : Screen {
  @Composable
  override fun Content() {
    val context = LocalContext.current
    val mediaServerPreferences = koinInject<MediaServerPreferences>()
    val musicSourceProvider by mediaServerPreferences.musicSourceProvider.collectAsState()
    val musicLibraryViewModel: MusicLibraryViewModel = viewModel()
    val localMusicTabs by musicLibraryViewModel.visibleTabs.collectAsStateWithLifecycle()
    val jellyfinViewModel: JellyfinViewModel =
      viewModel(factory = JellyfinViewModel.factory(context.applicationContext as Application))
    val navidromeViewModel: NavidromeViewModel =
      viewModel(factory = NavidromeViewModel.factory(context.applicationContext as Application))
    val backStack = LocalBackStack.current

    LaunchedEffect(Unit) {
      musicLibraryViewModel.setTab(localMusicTabs.firstOrNull() ?: MusicTab.SONGS)
    }

  if (musicSourceProvider == MusicSourceProvider.JELLYFIN) {
    val jellyfinUiState by jellyfinViewModel.uiState.collectAsStateWithLifecycle()
    if (jellyfinUiState.activeServer == null) {
      Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
      ) {
        androidx.compose.material3.Card(
          shape = MaterialTheme.shapes.extraLarge,
          colors =
            androidx.compose.material3.CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            androidx.compose.material3.Icon(
              painter = painterResource(R.drawable.ic_jellyfin),
              contentDescription = null,
              modifier = Modifier.size(56.dp),
              tint = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = stringResource(R.string.music_source_jellyfin),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = stringResource(R.string.pref_jellyfin_no_server),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            androidx.compose.material3.FilledTonalButton(
              onClick = {
                backStack.navigateTo(app.gyrolet.mpvrx.ui.preferences.MediaServersPreferencesScreen)
              },
            ) {
              Text(stringResource(R.string.generic_configure))
            }
            androidx.compose.material3.TextButton(
              onClick = {
                mediaServerPreferences.musicSourceProvider.set(MusicSourceProvider.LOCAL)
              },
            ) {
              Text(stringResource(R.string.music_source_local))
            }
          }
        }
      }
    } else if (!jellyfinUiState.isLoading && !jellyfinUiState.hasMusicLibrary && jellyfinUiState.libraries.isNotEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
      ) {
        androidx.compose.material3.Card(
          shape = MaterialTheme.shapes.extraLarge,
          colors =
            androidx.compose.material3.CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            androidx.compose.material3.Icon(
              painter = painterResource(R.drawable.ic_jellyfin),
              contentDescription = null,
              modifier = Modifier.size(56.dp),
              tint = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = stringResource(R.string.music_source_jellyfin),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = stringResource(R.string.jellyfin_no_music_library),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            androidx.compose.material3.FilledTonalButton(
              onClick = {
                mediaServerPreferences.musicSourceProvider.set(MusicSourceProvider.LOCAL)
              },
            ) {
              Text(stringResource(R.string.music_source_local))
            }
            androidx.compose.material3.TextButton(
              onClick = {
                backStack.navigateTo(app.gyrolet.mpvrx.ui.preferences.MediaServersPreferencesScreen)
              },
            ) {
              Text(stringResource(R.string.generic_configure))
            }
          }
        }
      }
    } else {
      LaunchedEffect(jellyfinUiState.libraries) {
        jellyfinViewModel.ensureMusicDataLoaded()
      }
      app.gyrolet.mpvrx.ui.browser.jellyfin.JellyfinContent(
        viewModel = jellyfinViewModel,
        isMusicOnlyMode = true,
      )
    }
  } else if (musicSourceProvider == MusicSourceProvider.NAVIDROME) {
    val navidromeUiState by navidromeViewModel.uiState.collectAsStateWithLifecycle()
    if (navidromeUiState.activeServer == null) {
      Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
      ) {
        androidx.compose.material3.Card(
          shape = MaterialTheme.shapes.extraLarge,
          colors =
            androidx.compose.material3.CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            androidx.compose.material3.Icon(
              painter = painterResource(R.drawable.ic_navidrome),
              contentDescription = null,
              modifier = Modifier.size(56.dp),
              tint = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = stringResource(R.string.music_source_navidrome),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = stringResource(R.string.pref_navidrome_no_server),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            androidx.compose.material3.FilledTonalButton(
              onClick = {
                backStack.navigateTo(app.gyrolet.mpvrx.ui.preferences.MediaServersPreferencesScreen)
              },
            ) {
              Text(stringResource(R.string.generic_configure))
            }
            androidx.compose.material3.TextButton(
              onClick = {
                mediaServerPreferences.musicSourceProvider.set(MusicSourceProvider.LOCAL)
              },
            ) {
              Text(stringResource(R.string.music_source_local))
            }
          }
        }
      }
    } else {
      app.gyrolet.mpvrx.ui.browser.navidrome.NavidromeContent(
        viewModel = navidromeViewModel,
        isMusicOnlyMode = true,
      )
    }
  } else if (musicSourceProvider == MusicSourceProvider.AUDIOBOOKS) {
    app.gyrolet.mpvrx.ui.browser.audiobooks.AudiobookLibraryContent(
      isMusicTabMode = true,
    )
  } else {
    MusicLibraryContent(
      musicViewModel = musicLibraryViewModel,
      jellyfinViewModel = jellyfinViewModel,
      navidromeViewModel = navidromeViewModel,
    )
  }  }
}
