/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.ui.icons.AppIcon
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors

/**
 * 二级页 · 卷宗页头 per mockup `.subhead`: a card-stock band carrying the vermilion enamelled
 * disc, the page title and its tag line, closed by a dashed tear edge. Used by pages pushed
 * beyond the main tabs (播放列表 / 快照 / 最近播放).
 */
@Composable
fun PaperSubhead(
  title: String,
  icon: AppIcon,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  onBackClick: (() -> Unit)? = null,
  /** Mockup `.sh-disc.sm` / `.appbar .tt`: 34dp disc and a 17.5sp title for content pages. */
  compact: Boolean = false,
  onTitleDoubleTap: (() -> Unit)? = null,
  onTitleLongPress: (() -> Unit)? = null,
  actions: @Composable RowScope.() -> Unit = {},
) {
  val paperColors = LocalPaperColors.current
  val discSize = if (compact) 34.dp else 44.dp
  val discShape = RoundedCornerShape(if (compact) 10.dp else 13.dp)
  val discIconSize = if (compact) 16.dp else 21.dp
  val discBorder = if (paperColors.isDark) paperColors.cardLine else Color(0xFFD6C49A)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(paperColors.paper)
      .statusBarsPadding(),
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .drawBehind {
            // Mockup `linear-gradient(180deg, card, paper 92%)` — vertical, measured height.
            drawRect(
              Brush.verticalGradient(
                colors = listOf(paperColors.card, paperColors.paper),
                startY = 0f,
                endY = size.height * 0.92f,
              ),
            )
            // Mockup `border-bottom:1.5px dashed`.
            val dash = 4.dp.toPx()
            val gap = 3.dp.toPx()
            val stroke = 1.5.dp.toPx()
            val y = size.height - stroke / 2f
            var x = 0f
            while (x < size.width) {
              drawLine(
                color = paperColors.cardLine,
                start = Offset(x, y),
                end = Offset((x + dash).coerceAtMost(size.width), y),
                strokeWidth = stroke,
              )
              x += dash + gap
            }
          }
          .padding(start = 12.dp, end = 14.dp, top = 9.dp, bottom = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
      if (onBackClick != null) {
        Box(
          modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onBackClick),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            Icons.RoundedFilled.ArrowBack,
            contentDescription = stringResource(R.string.back),
            tint = paperColors.inkSecondary,
            modifier = Modifier.size(21.dp),
          )
        }
      }

      Box(
        modifier =
          Modifier
            .size(discSize)
            .clip(discShape)
            .drawBehind {
              // Radial enamel: the light sits at the upper left (mockup 32% 24%).
              drawRect(
                Brush.radialGradient(
                  colors = listOf(Color(0xFFFCF4E2), Color(0xFFF0E4C8), Color(0xFFDCCBA2)),
                  center = Offset(size.width * 0.32f, size.height * 0.24f),
                  radius = size.width * 1.2f,
                ),
              )
            }
            .border(1.dp, discBorder, discShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(icon, contentDescription = null, tint = paperColors.red, modifier = Modifier.size(discIconSize))
      }

      Column(
        modifier =
          Modifier.weight(1f).then(
            if (onTitleDoubleTap != null || onTitleLongPress != null) {
              Modifier.pointerInput(onTitleDoubleTap, onTitleLongPress) {
                detectTapGestures(
                  onDoubleTap = onTitleDoubleTap?.let { action -> { action() } },
                  onLongPress = onTitleLongPress?.let { action -> { action() } },
                )
              }
            } else {
              Modifier
            },
          ),
      ) {
        Text(
          text = title,
          fontSize = if (compact) 17.5.sp else 18.sp,
          lineHeight = if (compact) 20.sp else 21.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 0.5.sp,
          color = paperColors.ink,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        if (!subtitle.isNullOrBlank()) {
          Text(
            text = subtitle,
            fontSize = 11.5.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.5.sp,
            color = paperColors.inkSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.5.dp),
          )
        }
      }

      // Actions sit shoulder to shoulder like the mockup's `.iconbtn` row; the header's own
      // 11dp gap applies once, before the group.
      Row(verticalAlignment = Alignment.CenterVertically) { actions() }
    }
  }
}
