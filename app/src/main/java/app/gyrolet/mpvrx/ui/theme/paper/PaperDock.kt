/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.theme.paper

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gyrolet.mpvrx.ui.browser.MainScreen
import app.gyrolet.mpvrx.ui.browser.MainTabIcon
import app.gyrolet.mpvrx.ui.browser.mainNavigationLabel

/**
 * 悬浮纸坞 — floating paper dock with four keys.
 *
 * Mockup rules (`.tabbar` / `.tab`): non-active keys show an icon only; the
 * active key gets an inset groove, a vermilion icon and an expanding label
 * (flex-grow 1 → 1.85), so switching never changes the dock height — only the
 * active key grows while its neighbours shrink.
 */
@Composable
internal fun PaperDock(
  visibleTabs: List<MainScreen.MainTab>,
  selectedTab: MainScreen.MainTab,
  onTabSelected: (MainScreen.MainTab) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (visibleTabs.isEmpty()) return
  val colors = LocalPaperColors.current
  val density = LocalDensity.current
  val dockShape = RoundedCornerShape(18.dp)

  Box(
    modifier
      .width(DockWidth)
      .height(58.dp)
      .onGloballyPositioned { coordinates ->
        NavigationBarMetricsPublisher.publish(coordinates, density)
      }
      .shadow(
        elevation = 10.dp,
        shape = dockShape,
        ambientColor = Color(0xFF544224).copy(alpha = 0.20f),
        spotColor = Color(0xFF544224).copy(alpha = 0.16f),
      )
      .clip(dockShape)
      .background(colors.card)
      .border(1.dp, colors.cardLine, dockShape)
      .padding(6.dp),
  ) {
    Row {
      visibleTabs.forEach { tab ->
        val active = tab == selectedTab
        val weight by animateFloatAsState(
          targetValue = if (active) 1.85f else 1f,
          animationSpec = tween(320, easing = CubicBezierEasing(0.45f, 1.25f, 0.4f, 1f)),
          label = "dock_key_weight_${tab.name}",
        )
        PaperDockKey(
          tab = tab,
          active = active,
          onClick = { onTabSelected(tab) },
          modifier = Modifier.weight(weight),
        )
      }
    }
  }
}

@Composable
private fun PaperDockKey(
  tab: MainScreen.MainTab,
  active: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalPaperColors.current
  val interaction = remember { MutableInteractionSource() }
  val pressed by interaction.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (pressed) 0.93f else 1f,
    animationSpec = tween(120),
    label = "dock_key_scale",
  )
  val keyShape = RoundedCornerShape(14.dp)
  val tint = if (active) colors.red else colors.inkTertiary
  // The label slot hugs the measured text so icon↔label spacing stays 5dp in every locale.
  val label = mainNavigationLabel(tab)
  val textMeasurer = rememberTextMeasurer()
  val density = LocalDensity.current
  val labelSlotWidth =
    remember(label, density) {
      val layout =
        textMeasurer.measure(
          label,
          androidx.compose.ui.text.TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
          ),
        )
      with(density) { layout.size.width.toDp() }
    }
  val labelWidth by animateDpAsState(
    targetValue = if (active) labelSlotWidth else 0.dp,
    animationSpec = tween(320, easing = CubicBezierEasing(0.45f, 1.25f, 0.4f, 1f)),
    label = "dock_key_label_width",
  )

  Box(
    modifier
      .height(46.dp)
      .then(if (active) Modifier.paperInset(keyShape, colors) else Modifier)
      .clickable(
        interactionSource = interaction,
        indication = null,
        role = Role.Tab,
        onClick = onClick,
      )
      .semantics { selected = active },
    contentAlignment = Alignment.Center,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
    ) {
      Box(Modifier.padding(end = if (labelWidth > 0.dp) 5.dp else 0.dp)) {
        MainTabIcon(tab, tint, null, 21.dp)
      }
      Box(
        Modifier
          .width(labelWidth)
          .widthIn(max = labelSlotWidth),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = label,
          color = tint,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          letterSpacing = 1.sp,
          maxLines = 1,
          overflow = androidx.compose.ui.text.style.TextOverflow.Clip,
        )
      }
    }
  }
}

/** Mockup `.tabbar`: 286dp-wide compact dock. */
private val DockWidth = 286.dp

/** Publishes dock metrics for the MiniPlayer beside-navigation layout. */
private object NavigationBarMetricsPublisher {
  fun publish(
    coordinates: androidx.compose.ui.layout.LayoutCoordinates,
    density: androidx.compose.ui.unit.Density,
  ) {
    app.gyrolet.mpvrx.ui.browser.NavigationBarState.navbarWidth =
      with(density) { coordinates.size.width.toDp() }
    app.gyrolet.mpvrx.ui.browser.NavigationBarState.navigationPillHeight =
      with(density) { coordinates.size.height.toDp() }
    app.gyrolet.mpvrx.ui.browser.NavigationBarState.navbarLeftOffset =
      with(density) { coordinates.positionInRoot().x.toDp() }
  }
}
