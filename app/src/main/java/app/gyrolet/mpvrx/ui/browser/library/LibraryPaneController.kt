/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Relays the 收藏 hub header's search/sort requests into whichever pane is
 * currently hosted. Requests are counters so a pane recomposing mid-session
 * never replays old requests twice.
 */
class LibraryPaneController {
  var searchRequests by mutableIntStateOf(0)
    private set

  var sortRequests by mutableIntStateOf(0)
    private set

  fun requestSearch() {
    searchRequests++
  }

  fun requestSort() {
    sortRequests++
  }
}

/**
 * Non-null while a browser screen is hosted inside the 收藏 hub: the pane hides its
 * own normal-mode top bar, drops Scaffold insets, and follows the hub's actions.
 */
val LocalLibraryPaneHost = staticCompositionLocalOf<LibraryPaneController?> { null }
