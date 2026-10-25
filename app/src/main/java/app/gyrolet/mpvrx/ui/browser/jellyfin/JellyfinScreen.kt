/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.jellyfin

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import app.gyrolet.mpvrx.presentation.Screen
import kotlinx.serialization.Serializable

/**
 * Standalone Jellyfin page. This used to be the bottom-nav JELLYFIN tab; with the
 * paper dock it is reached from the Profile hub shortcuts instead.
 */
@Serializable
object JellyfinScreen : Screen {
  @Composable
  override fun Content() {
    val context = LocalContext.current
    val viewModel: JellyfinViewModel =
      viewModel(factory = JellyfinViewModel.factory(context.applicationContext as Application))
    JellyfinContent(viewModel = viewModel)
  }
}
