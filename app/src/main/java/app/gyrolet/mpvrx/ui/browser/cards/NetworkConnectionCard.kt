/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.cards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.domain.network.NetworkConnection
import app.gyrolet.mpvrx.presentation.components.AppDropdownMenu
import app.gyrolet.mpvrx.presentation.components.AppDropdownMenuItem
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.ui.theme.AppMotion
import app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors
import app.gyrolet.mpvrx.ui.theme.paper.dashedTopEdge
import app.gyrolet.mpvrx.ui.theme.paper.paperInset
import app.gyrolet.mpvrx.ui.theme.paper.paperRaise

/**
 * 网络名片 per mockup `.netcard`: a 172dp cream card riding in the horizontal card wallet —
 * protocol corner chip + status dot, name/host, a slanted rubber stamp, and the dashed
 * tear-off footer carrying the 自动连接 mini toggle. Tap opens (or connects) the share;
 * long-press holds the edit/delete/disconnect actions so the card face stays as clean as the mock.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NetworkConnectionCard(
  connection: NetworkConnection,
  onConnect: (NetworkConnection) -> Unit,
  onDisconnect: (NetworkConnection) -> Unit,
  onEdit: (NetworkConnection) -> Unit,
  onDelete: (NetworkConnection) -> Unit,
  onBrowse: (NetworkConnection) -> Unit,
  onAutoConnectChange: (NetworkConnection, Boolean) -> Unit,
  modifier: Modifier = Modifier,
  isConnected: Boolean = false,
  isConnecting: Boolean = false,
  error: String? = null,
) {
  val colors = LocalPaperColors.current
  var isPressed by remember { mutableStateOf(false) }
  var menuOpen by remember { mutableStateOf(false) }
  val targetScale = if (isPressed) 0.97f else 1.0f
  val scale by animateFloatAsState(
    targetValue = targetScale,
    animationSpec = if (AppMotion.shouldReduceMotion()) snap() else AppMotion.Spatial.Expressive,
    label = "NetworkConnectionCardScale",
  )
  // 每张名片微旋一点，叠纸隐喻 (mockup rotates each card a touch).
  val tilt = remember(connection.id) { (((connection.id % 5) - 2) * 0.4f).toFloat() }

  // Mockup stamp states: ok (green) / err (red) / off (grey); connecting borrows the red.
  val stampRes =
    when {
      isConnecting -> R.string.ui_connecting
      error != null -> R.string.network_stamp_failed
      isConnected -> R.string.network_stamp_connected
      else -> R.string.network_stamp_offline
    }
  val stampColor =
    when {
      isConnecting -> colors.redHighlight
      error != null -> colors.red
      isConnected -> colors.green
      else -> colors.inkSecondary
    }
  val dotColor =
    when {
      isConnecting -> colors.redHighlight
      error != null -> colors.red
      isConnected -> colors.green
      else -> colors.inkTertiary
    }
  val dotHalo =
    when {
      isConnected -> colors.green.copy(alpha = 0.18f)
      error != null -> colors.red.copy(alpha = 0.15f)
      else -> Color.Transparent
    }

  val shape = RoundedCornerShape(10.dp)
  val cardBorder = if (colors.isDark) colors.cardLine else Color(0xFFE0D5B8)
  val dashColor = if (colors.isDark) colors.cardLine else Color(0xFFD3C49E)

  Box(
    modifier
      .width(172.dp)
      .height(150.dp)
      .graphicsLayer(scaleX = scale, scaleY = scale, rotationZ = tilt)
      .pointerInput(Unit) {
        awaitPointerEventScope {
          while (true) {
            val event = awaitPointerEvent()
            isPressed = event.changes.any { it.pressed }
          }
        }
      }
      .paperRaise(shape, colors, elevation = 5.dp)
      .border(1.dp, cardBorder, shape)
      .then(
        if (colors.isDark) {
          Modifier
        } else {
          // Mockup card face: vertical cream gradient slightly warmer than the flat card token.
          Modifier.clip(shape).drawBehind {
            drawRect(Brush.verticalGradient(listOf(Color(0xFFFDFBF3), Color(0xFFF3EDDC))))
          }
        },
      )
      .combinedClickable(
        onClick = { if (isConnected) onBrowse(connection) else onConnect(connection) },
        onLongClick = { menuOpen = true },
      ),
  ) {
    Column(Modifier.fillMaxSize()) {
      // .nc-top: protocol chip + status dot.
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = connection.protocol.displayName,
          fontSize = 9.sp,
          letterSpacing = 1.5.sp,
          fontWeight = FontWeight.Bold,
          color = colors.inkSecondary,
          modifier =
            Modifier
              .border(1.dp, if (colors.isDark) colors.cardLine else Color(0xFFD8CBA6), RoundedCornerShape(4.dp))
              .background(
                if (colors.isDark) colors.paperInset else Color(0xFFD8CBA6).copy(alpha = 0.25f),
                RoundedCornerShape(4.dp),
              )
              .padding(horizontal = 6.dp, vertical = 2.dp),
        )
        Box(
          Modifier
            .size(7.dp)
            .drawBehind {
              if (dotHalo != Color.Transparent) {
                drawCircle(dotHalo, radius = size.minDimension / 2f + 3.dp.toPx())
              }
              drawCircle(dotColor)
            },
        )
      }

      Text(
        text = connection.name,
        fontSize = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        color = colors.ink,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier =
          Modifier
            .padding(start = 12.dp, end = 12.dp, top = 8.dp)
            .fillMaxWidth(),
      )
      Text(
        text = "${connection.host}:${connection.port}",
        fontSize = 9.5.sp,
        color = colors.inkSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier =
          Modifier
            .padding(start = 12.dp, end = 12.dp, top = 4.dp)
            .fillMaxWidth(),
      )

      Spacer(Modifier.weight(1f))

      // .nc-foot: dashed tear-off edge spanning the full card, toggle + label inside.
      Column(Modifier.fillMaxWidth().dashedTopEdge(dashColor)) {
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 9.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier =
              Modifier
                .clip(RoundedCornerShape(8.dp))
                .toggleable(
                  value = connection.autoConnect,
                  role = Role.Switch,
                  onValueChange = { onAutoConnectChange(connection, it) },
                )
                .padding(horizontal = 2.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            PaperMiniSwitch(checked = connection.autoConnect)
            Text(
              text = stringResource(R.string.ui_auto_connect),
              fontSize = 9.5.sp,
              letterSpacing = 1.sp,
              color = colors.inkSecondary,
            )
          }
        }
      }
    }

    // 斜盖印章 (rubber stamp), overlapping the host line like the mock. The mock's 0.72
    // opacity goes onto the text/border colors — putting it on a rotated graphicsLayer
    // instead would route the layer through an offscreen texture and soften the strokes.
    Text(
      text = stringResource(stampRes),
      fontSize = 9.sp,
      fontWeight = FontWeight.ExtraBold,
      letterSpacing = 3.sp,
      color = stampColor.copy(alpha = 0.72f),
      modifier =
        Modifier
          .align(Alignment.TopEnd)
          .padding(top = 56.dp, end = 11.dp)
          .graphicsLayer { rotationZ = -9f }
          .border(1.5.dp, stampColor.copy(alpha = 0.72f), RoundedCornerShape(5.dp))
          .padding(horizontal = 8.dp, vertical = 3.dp),
    )

    AppDropdownMenu(
      expanded = menuOpen,
      onDismissRequest = { menuOpen = false },
    ) {
      val itemCount = if (isConnected) 4 else 3
      if (isConnected) {
        AppDropdownMenuItem(
          text = { Text(stringResource(R.string.ui_browse)) },
          leadingIcon = { Icon(Icons.RoundedFilled.FolderOpen, contentDescription = null) },
          index = 0,
          itemCount = itemCount,
          onClick = {
            menuOpen = false
            onBrowse(connection)
          },
        )
        AppDropdownMenuItem(
          text = { Text(stringResource(R.string.ui_disconnect)) },
          leadingIcon = { Icon(Icons.RoundedFilled.LinkOff, contentDescription = null) },
          index = 1,
          itemCount = itemCount,
          onClick = {
            menuOpen = false
            onDisconnect(connection)
          },
        )
      }
      AppDropdownMenuItem(
        text = { Text(stringResource(R.string.ui_edit_connection)) },
        leadingIcon = { Icon(Icons.RoundedFilled.Edit, contentDescription = null) },
        index = if (isConnected) 2 else 0,
        itemCount = itemCount,
        onClick = {
          menuOpen = false
          onEdit(connection)
        },
      )
      AppDropdownMenuItem(
        text = { Text(stringResource(R.string.delete)) },
        leadingIcon = { Icon(Icons.RoundedFilled.Delete, contentDescription = null) },
        index = if (isConnected) 3 else 1,
        itemCount = itemCount,
        onClick = {
          menuOpen = false
          onDelete(connection)
        },
      )
    }
  }
}

/** 迷你拨杆 (mockup `.sw.mini`): 40×24 recessed groove, 19px paper knob, vermilion when on. */
@Composable
private fun PaperMiniSwitch(checked: Boolean) {
  val colors = LocalPaperColors.current
  val knobShift by animateDpAsState(
    targetValue = if (checked) 18.5.dp else 2.5.dp,
    animationSpec = spring(stiffness = 1200f),
    label = "mini_switch_knob",
  )
  Box(
    modifier =
      Modifier
        .size(width = 40.dp, height = 24.dp)
        .then(
          if (checked) {
            Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Brush.verticalGradient(listOf(colors.redHighlight, colors.red)))
          } else {
            Modifier.paperInset(RoundedCornerShape(12.dp), colors)
          },
        ),
    contentAlignment = Alignment.CenterStart,
  ) {
    Box(
      modifier =
        Modifier
          .padding(start = knobShift)
          .size(19.dp)
          .shadow(elevation = 2.dp, shape = CircleShape)
          .then(
            if (colors.isDark) {
              Modifier.background(colors.card, CircleShape)
            } else {
              Modifier.background(
                Brush.verticalGradient(listOf(Color(0xFFFFFDF6), Color(0xFFEDE4CE))),
                CircleShape,
              )
            },
          ),
    )
  }
}
