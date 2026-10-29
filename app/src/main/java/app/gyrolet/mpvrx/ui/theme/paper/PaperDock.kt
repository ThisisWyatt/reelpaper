/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.theme.paper

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.gyrolet.mpvrx.ui.browser.MainScreen
import app.gyrolet.mpvrx.ui.browser.MainTabIcon
import app.gyrolet.mpvrx.ui.browser.mainNavigationLabel
import kotlin.math.roundToInt

/**
 * 悬浮纸坞 — floating paper dock of equal-width, icon-only paper slips.
 *
 * Mockup rules (`.tabbar` / `.tab`): every visible tab is an equal-width slip, so switching
 * tabs never moves the layout; the active slip carries a 朱砂墨印纸贴 — the same sticker
 * material as the page-head disc badge — that keeps its warm paper colours in dark mode
 * because it is a physical sticker, not a re-tinted surface. The dock width follows the
 * number of visible tabs (the appearance page's tab switches).
 *
 * Motion: the stamp slides to the new slip with a spring overshoot and the icon pops as it
 * becomes current. The stamp never fades in or out — a cross-fade shows the near-white card
 * through the sticker and reads as a white box flashing on the incoming tab.
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
  val dockShape = RoundedCornerShape(16.dp)
  val stampShape = RoundedCornerShape(12.dp)
  val dockWidth =
    DockPadding * 2 + DockTabWidth * visibleTabs.size + DockTabGap * (visibleTabs.size - 1)
  val selectedIndex = visibleTabs.indexOf(selectedTab)
  val stampIndex by animateFloatAsState(
    targetValue = selectedIndex.coerceAtLeast(0).toFloat(),
    animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f),
    label = "dock_stamp_index",
  )
  val tabStepPx = with(density) { (DockTabWidth + DockTabGap).toPx() }

  Box(
    modifier
      .width(dockWidth)
      .height(DockPadding * 2 + DockTabHeight)
      .onGloballyPositioned { coordinates ->
        NavigationBarMetricsPublisher.publish(coordinates, density)
      }
      // Mockup `--raise` + `0 8px 22px`: one soft float and one ambient layer, no three-ply stack.
      .shadow(
        elevation = 8.dp,
        shape = dockShape,
        ambientColor = AmbientShadow.copy(alpha = if (colors.isDark) 0.34f else 0.12f),
        spotColor = AmbientShadow.copy(alpha = if (colors.isDark) 0.30f else 0.10f),
      )
      .paperRaise(dockShape, colors, elevation = 2.dp)
      .border(1.dp, colors.cardLine, dockShape)
      .padding(DockPadding),
  ) {
    if (selectedIndex >= 0) {
      Box(
        Modifier
          .offset { IntOffset((tabStepPx * stampIndex).roundToInt(), 0) }
          .width(DockTabWidth)
          .height(DockTabHeight)
          .shadow(
            elevation = 2.dp,
            shape = stampShape,
            ambientColor = AmbientShadow.copy(alpha = 0.22f),
            spotColor = AmbientShadow.copy(alpha = 0.22f),
          )
          .clip(stampShape)
          .drawWithCache {
            val stampBrush =
              Brush.radialGradient(
                colorStops = arrayOf(0f to StampHighlight, 0.6f to StampMid, 1f to StampEdge),
                center = Offset(size.width * 0.32f, size.height * 0.24f),
                // Mockup gradient is a 120%×120% ellipse; a circle at 0.95×width keeps both the
                // upper-left light and the lower-right falloff where the ellipse puts them.
                radius = size.width * 0.95f,
              )
            val highlightHeight = 1.dp.toPx()
            onDrawBehind {
              drawRect(stampBrush)
              // inset 0 1px 0 rgba(255,255,255,.8)
              drawRect(
                color = Color.White.copy(alpha = 0.8f),
                size = Size(size.width, highlightHeight),
              )
            }
          }
          .border(1.dp, StampBorder, stampShape),
      )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(DockTabGap)) {
      visibleTabs.forEach { tab ->
        PaperDockKey(
          tab = tab,
          active = tab == selectedTab,
          onClick = { onTabSelected(tab) },
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
  val keyShape = RoundedCornerShape(12.dp)
  // 0 → 1 as the key becomes current; the spring overshoots, so the icon pops rather than eases.
  val activation by animateFloatAsState(
    targetValue = if (active) 1f else 0f,
    animationSpec = spring(dampingRatio = 0.55f, stiffness = 520f),
    label = "dock_key_activation",
  )
  val tint by animateColorAsState(
    targetValue = if (active) colors.red else colors.inkTertiary,
    animationSpec = tween(220),
    label = "dock_key_tint",
  )
  val pressAlpha by animateFloatAsState(
    targetValue = if (pressed) 1f else 0f,
    animationSpec = tween(160),
    label = "dock_key_press",
  )
  val iconScale by animateFloatAsState(
    targetValue = if (pressed) 0.88f else 1f,
    animationSpec = tween(140),
    label = "dock_key_icon_scale",
  )
  val pressColor =
    if (colors.isDark) {
      Color.White.copy(alpha = 0.05f * pressAlpha)
    } else {
      PressTint.copy(alpha = 0.07f * pressAlpha)
    }

  Box(
    modifier
      .width(DockTabWidth)
      .height(DockTabHeight)
      .clip(keyShape)
      .drawWithCache {
        onDrawBehind { if (pressAlpha > 0f) drawRect(pressColor) }
      }
      .clickable(
        interactionSource = interaction,
        indication = null,
        role = Role.Tab,
        onClick = onClick,
      )
      .semantics { selected = active },
    contentAlignment = Alignment.Center,
  ) {
    Box(
      Modifier.graphicsLayer {
        val iconPop = 1f + 0.08f * activation
        scaleX = iconScale * iconPop
        scaleY = iconScale * iconPop
        translationY = -1.dp.toPx() * activation
      },
    ) {
      // The slip carries no visible label, so the tab name moves into the icon's description.
      MainTabIcon(tab, tint, mainNavigationLabel(tab), DockIconSize)
    }
  }
}

/** Mockup `.tab`: 5dp frame + 86×46dp slips with a 2dp seam (slips ~10% wider than V33). */
private val DockPadding = 5.dp
private val DockTabWidth = 86.dp
private val DockTabHeight = 46.dp
private val DockTabGap = 2.dp
private val DockIconSize = 26.dp

// 朱砂墨印纸贴 — fixed values on purpose: a physical sticker does not darken with the theme.
private val StampHighlight = Color(0xFFFCF4E2)
private val StampMid = Color(0xFFF0E4C8)
private val StampEdge = Color(0xFFDCCBA2)
private val StampBorder = Color(0xFFD6C49A)
private val PressTint = Color(0xFF786030)
private val AmbientShadow = Color(0xFF544224)

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
