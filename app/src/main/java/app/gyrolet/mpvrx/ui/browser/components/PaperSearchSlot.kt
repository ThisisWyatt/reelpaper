/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.ui.icons.PaperIcons
import app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors
import app.gyrolet.mpvrx.ui.theme.paper.paperInset

/**
 * 搜索槽 per mockup `.search`: a recessed 42dp paper groove with the magnifier and the query.
 * Permanent on the playlists page — typing filters the list live.
 */
@Composable
fun PaperSearchSlot(
  query: String,
  onQueryChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier,
  focusRequester: FocusRequester? = null,
) {
  val colors = LocalPaperColors.current
  val shape = RoundedCornerShape(12.dp)
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(42.dp)
        .paperInset(shape, colors)
        .padding(horizontal = 13.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(9.dp),
  ) {
    Icon(PaperIcons.Search, contentDescription = null, tint = colors.inkTertiary, modifier = Modifier.size(17.dp))
    Box(modifier = Modifier.weight(1f)) {
      if (query.isEmpty()) {
        Text(
          text = placeholder,
          fontSize = 13.5.sp,
          lineHeight = 16.sp,
          color = colors.inkTertiary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(fontSize = 13.5.sp, color = colors.ink),
        cursorBrush = SolidColor(colors.red),
        modifier =
          Modifier
            .fillMaxWidth()
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
      )
    }
    if (query.isNotEmpty()) {
      Box(
        modifier = Modifier.size(24.dp).clip(CircleShape).clickable { onQueryChange("") },
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          Icons.RoundedFilled.Close,
          contentDescription = stringResource(R.string.generic_cancel),
          tint = colors.inkTertiary,
          modifier = Modifier.size(15.dp),
        )
      }
    }
  }
}
