/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.theme.paper

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import app.gyrolet.mpvrx.ui.icons.AppIcon
import app.gyrolet.mpvrx.ui.icons.Icon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 海拔规则 from the mockup:
 *  - `--raise` 卡纸浮起: top 1px inner highlight + warm drop shadows → cards / pills
 *  - `--inset` 纸槽内凹: top inner shadow + bottom 1px rebound light → input slots / tracks / active tabs
 *  - `0 3px 0` 实体键台阶: only for rectangular pressable buttons
 *  - circles (FAB / play) never take a step; they use a soft double drop shadow instead
 */

/** 卡纸浮起: warm elevation + inner top highlight. Pair with a card-line border for full cards. */
fun Modifier.paperRaise(
  shape: Shape,
  colors: PaperColors,
  elevation: Dp = 3.dp,
): Modifier =
  this
    .shadow(
      elevation = elevation,
      shape = shape,
      clip = false,
      ambientColor = colors.shadowTint.copy(alpha = if (colors.isDark) 0.55f else 0.34f),
      spotColor = colors.shadowTint.copy(alpha = if (colors.isDark) 0.5f else 0.30f),
    )
    .background(colors.card, shape)
    .drawWithCache {
      // inset 0 1px 0 rgba(255,255,255,.8) — the paper card's top highlight. Clipped to the
      // shape: an unclipped rect would bleed past the rounded corners onto whatever sits
      // behind the card (visible as a light bar over the dialog's dark scrim).
      val outlinePath = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
      onDrawWithContent {
        drawContent()
        clipPath(outlinePath) {
          drawRect(
            brush =
              // Mockup `inset 0 1px 0`: a hairline, not a band — a taller gradient gets cut
              // diagonally by the rounded corners and reads as a ragged edge.
              Brush.verticalGradient(
                colors = listOf(colors.highlight, Color.Transparent),
                startY = 0f,
                endY = 1.dp.toPx(),
              ),
          )
        }
      }
    }

/** 纸槽内凹: recessed groove drawn beneath the caller's content. */
fun Modifier.paperInset(
  shape: Shape,
  colors: PaperColors,
  color: Color = colors.paperInset,
): Modifier =
  this
    .clip(shape)
    .drawWithCache {
      // Mockup `--inset`: light = `inset 0 1px 3px rgba(96,76,36,.16)` — a whisper of
      // warm shadow hugging the top edge; dark = `inset 0 2px 4px rgba(0,0,0,.55)`.
      val topColor =
        if (colors.isDark) Color.Black.copy(alpha = 0.55f)
        else Color(0xFF604C24).copy(alpha = 0.16f)
      val topShadow =
        Brush.verticalGradient(
          colors = listOf(topColor, Color.Transparent),
          startY = 0f,
          endY = (if (colors.isDark) 4.dp else 3.dp).toPx(),
        )
      val bottomLight =
        Brush.verticalGradient(
          colors = listOf(Color.Transparent, colors.highlight.copy(alpha = 0.55f)),
          startY = size.height - 1.5.dp.toPx(),
          endY = size.height,
        )
      onDrawWithContent {
        drawRect(color)
        drawRect(topShadow)
        drawRect(bottomLight)
        drawContent()
      }
    }

/** Dashed tear-off edge (mockup dashed borders on tickets / footers). */
fun Modifier.dashedTopEdge(
  color: Color,
  strokeWidth: Dp = 1.5.dp,
): Modifier =
  drawBehind {
    val dash = 4.dp.toPx()
    val gap = 3.dp.toPx()
    val stroke = strokeWidth.toPx()
    var x = 0f
    while (x < drawContext.size.width) {
      drawLine(
        color = color,
        start = Offset(x, 0f),
        end = Offset(x + dash, 0f),
        strokeWidth = stroke,
      )
      x += dash + gap
    }
  }

/** Raised paper card container. */
@Composable
fun PaperCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(16.dp),
  bordered: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colors = LocalPaperColors.current
  Box(
    modifier
      .paperRaise(shape, colors)
      .then(if (bordered) Modifier.border(1.dp, colors.cardLine, shape) else Modifier),
  ) {
    content()
  }
}

/**
 * 朱砂实体键 — rectangular pressable key with a `0 3px 0 red-dark` step,
 * sinks 2dp on press. Only for main rectangular actions.
 */
@Composable
fun PaperSolidButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = RoundedCornerShape(13.dp),
  height: Dp = 50.dp,
  fillWidth: Boolean = true,
  icon: AppIcon? = null,
) {
  val colors = LocalPaperColors.current
  val interaction = remember { MutableInteractionSource() }
  val pressed by interaction.collectIsPressedAsState()
  val step by animateDpAsState(
    targetValue = if (pressed) 1.dp else 3.dp,
    animationSpec = spring(stiffness = 1400f),
    label = "paper_button_step",
  )
  val sink by animateDpAsState(
    targetValue = if (pressed) 2.dp else 0.dp,
    animationSpec = spring(stiffness = 1400f),
    label = "paper_button_sink",
  )
  Box(
    modifier
      .graphicsLayer {
        translationY = sink.toPx()
        if (!enabled) alpha = 0.45f
      }
      .drawWithCache {
        // The step edge: the button's own rounded outline shifted down, so the slab peeks
        // out below the body the way CSS `box-shadow: 0 3px 0` follows the border-radius —
        // a full-width rect would poke its square corners past the rounded bottom corners.
        val outline = shape.createOutline(size, layoutDirection, this)
        onDrawBehind {
          translate(top = step.toPx()) {
            drawOutline(outline, color = colors.redDark)
          }
        }
      }
      .height(height)
      .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
      .clip(shape)
      .background(
        Brush.verticalGradient(listOf(colors.redHighlight, colors.red, Color(0xFF9C2A22))),
      )
      .clickable(
        interactionSource = interaction,
        indication = null,
        enabled = enabled,
        role = Role.Button,
        onClick = onClick,
      ),
    contentAlignment = Alignment.Center,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (icon != null) {
        Icon(icon, contentDescription = null, tint = colors.onRed, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
      }
      Text(
        text = text,
        color = colors.onRed,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

/**
 * 纸色实体键 — mockup `.btn-paper`: raised card-paper key (48dp, card→paper-in gradient,
 * card-line border), sinks into an inset groove on press. The neutral counterpart
 * to [PaperSolidButton].
 */
@Composable
fun PaperRaisedButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = RoundedCornerShape(12.dp),
  height: Dp = 48.dp,
  fillWidth: Boolean = true,
) {
  val colors = LocalPaperColors.current
  val interaction = remember { MutableInteractionSource() }
  val pressed by interaction.collectIsPressedAsState()
  Box(
    modifier
      .height(height)
      .graphicsLayer {
        translationY = if (pressed) 1.dp.toPx() else 0f
        if (!enabled) alpha = 0.45f
      }
      .then(
        if (pressed) {
          Modifier.paperInset(shape, colors)
        } else {
          Modifier
            .paperRaise(shape, colors, elevation = 2.dp)
            .background(Brush.verticalGradient(listOf(colors.card, colors.paperInset)), shape)
        },
      )
      .then(if (pressed) Modifier else Modifier.border(1.dp, colors.cardLine, shape))
      .clickable(
        interactionSource = interaction,
        indication = null,
        enabled = enabled,
        role = Role.Button,
        onClick = onClick,
      ),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text,
      color = colors.ink,
      fontWeight = FontWeight.SemiBold,
    )
  }
}

/** 朱砂圆键 FAB — soft double shadow, no step (circles may not take steps). */
@Composable
fun PaperFab(
  icon: AppIcon,
  contentDescription: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  fabSize: Dp = 56.dp,
) {
  val colors = LocalPaperColors.current
  Box(
    modifier
      .size(fabSize)
      .shadow(
        elevation = 8.dp,
        shape = CircleShape,
        ambientColor = Color(0xFF8E261F).copy(alpha = 0.42f),
        spotColor = Color(0xFF8E261F).copy(alpha = 0.38f),
      )
      .clip(CircleShape)
      .drawWithCache {
        val gradient =
          Brush.radialGradient(
            colors = listOf(Color(0xFFE06A50), colors.red, Color(0xFF8E261F)),
            center = Offset(size.width * 0.32f, size.height * 0.24f),
            radius = size.width * 1.3f,
          )
        onDrawBehind { drawRect(gradient) }
      }
      .border(1.dp, colors.redDark, CircleShape)
      .clickable(role = Role.Button, onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      icon,
      contentDescription = contentDescription,
      tint = colors.onRed,
      modifier = Modifier.size(24.dp),
    )
  }
}

/** 圆角图标键 — transparent rest state, inset groove when pressed. */
@Composable
fun PaperIconButton(
  icon: AppIcon,
  contentDescription: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tint: Color? = null,
  enabled: Boolean = true,
  buttonSize: Dp = 40.dp,
  iconSize: Dp = 21.dp,
) {
  val colors = LocalPaperColors.current
  val interaction = remember { MutableInteractionSource() }
  val pressed by interaction.collectIsPressedAsState()
  val contentTint = tint ?: colors.inkSecondary
  // Corner keeps the mockup's 40/12 ratio at any size.
  val shape = RoundedCornerShape((buttonSize.value * 0.3f).dp)
  Box(
    modifier
      .size(buttonSize)
      .then(
        if (pressed) {
          Modifier.paperInset(shape, colors)
        } else {
          Modifier
        },
      )
      .clickable(
        interactionSource = interaction,
        indication = null,
        enabled = enabled,
        role = Role.Button,
        onClick = onClick,
      ),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      icon,
      contentDescription = contentDescription,
      tint = contentTint,
      modifier = Modifier.size(iconSize),
    )
  }
}

/**
 * 搜索槽 — mockup `.search`: recessed 42dp paper groove with a magnifier and
 * placeholder text; tapping hands off to the screen's search flow.
 */
@Composable
fun PaperSearchSlot(
  placeholder: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalPaperColors.current
  val interaction = remember { MutableInteractionSource() }
  val pressed by interaction.collectIsPressedAsState()
  Row(
    modifier
      .height(42.dp)
      .paperInset(RoundedCornerShape(12.dp), colors)
      .then(if (pressed) Modifier.border(1.dp, colors.cardLine, RoundedCornerShape(12.dp)) else Modifier)
      .clickable(
        interactionSource = interaction,
        indication = null,
        role = Role.Button,
        onClick = onClick,
      )
      .padding(horizontal = 13.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      app.gyrolet.mpvrx.ui.icons.PaperIcons.Search,
      contentDescription = null,
      tint = colors.inkTertiary,
      modifier = Modifier.size(17.dp),
    )
    Spacer(Modifier.width(9.dp))
    Text(
      text = placeholder,
      color = colors.inkTertiary,
      fontSize = 13.5.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
