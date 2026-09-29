/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.gyrolet.mpvrx.ui.theme.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gyrolet.mpvrx.ui.theme.ButtonTextStyle
import app.gyrolet.mpvrx.ui.theme.CardPaper
import app.gyrolet.mpvrx.ui.theme.Cinnabar
import app.gyrolet.mpvrx.ui.theme.CinnabarDk
import app.gyrolet.mpvrx.ui.theme.CinnabarHi
import app.gyrolet.mpvrx.ui.theme.Ink
import app.gyrolet.mpvrx.ui.theme.Ink2
import app.gyrolet.mpvrx.ui.theme.Ink3
import app.gyrolet.mpvrx.ui.theme.Paper
import app.gyrolet.mpvrx.ui.theme.PressOverlayTint
import app.gyrolet.mpvrx.ui.theme.ShadowTint

/** ① Cinnabar solid key — primary rectangular action button */
@Composable
fun CinnabarButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  content: @Composable RowScope.() -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val pressOffset by animateDpAsState(if (isPressed) 2.dp else 0.dp, label = "press")
  val stepHeight by animateDpAsState(if (isPressed) 1.dp else 3.dp, label = "step")

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(13.dp))
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick,
      )
      .drawBehind {
        // Step shadow
        drawRect(
          color = CinnabarDk,
          topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - pressOffset.toPx()),
          size = androidx.compose.ui.geometry.Size(size.width, stepHeight.toPx()),
        )
      }
      .offset(y = -pressOffset)
      .defaultMinSize(minHeight = 50.dp),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      modifier = Modifier
        .matchParentSize()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(CinnabarHi, Cinnabar, Color(0xFF9C2A22)),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY,
          ),
          shape = RoundedCornerShape(13.dp),
        )
        .padding(horizontal = 24.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
      content = {
        androidx.compose.runtime.CompositionLocalProvider(
          LocalContentColor provides Paper.copy(alpha = if (enabled) 1f else 0.5f),
        ) {
          content()
        }
      },
    )
  }
}

/** ② Paper button — secondary card-paper button */
@Composable
fun PaperButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  content: @Composable RowScope.() -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val pressOffset by animateDpAsState(if (isPressed) 1.dp else 0.dp, label = "press")

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick,
      )
      .shadow(elevation = if (isPressed) 2.dp else 6.dp, shape = RoundedCornerShape(12.dp))
      .offset(y = -pressOffset)
      .defaultMinSize(minHeight = 48.dp),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      modifier = Modifier
        .matchParentSize()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(CardPaper, Color(0xFFEDE4CE)),
          ),
          shape = RoundedCornerShape(12.dp),
        )
        .padding(horizontal = 20.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
      content = content,
    )
  }
}

/** ③ Cinnabar FAB — circular floating action button */
@Composable
fun CinnabarFab(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val pressOffset by animateDpAsState(if (isPressed) 2.dp else 0.dp, label = "press")

  Box(
    modifier = modifier
      .size(56.dp)
      .offset(y = -pressOffset)
      .shadow(elevation = 8.dp, shape = CircleShape, spotColor = ShadowTint.copy(0.12f))
      .shadow(elevation = 3.dp, shape = CircleShape, spotColor = ShadowTint.copy(0.08f))
      .clip(CircleShape)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
      )
      .background(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFFE06A50), Cinnabar, Color(0xFF8E261F)),
          center = androidx.compose.ui.geometry.Offset(0.32f, 0.24f),
          radius = 1.3f,
        ),
        shape = CircleShape,
      ),
    contentAlignment = Alignment.Center,
  ) {
    content()
  }
}

/** ④ Icon button — 40dp rounded square */
@Composable
fun IconButtonReelPaper(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  painter: Painter? = null,
  contentDescription: String? = null,
  isActive: Boolean = false,
  tint: Color = if (isActive) Cinnabar else Ink2,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  Box(
    modifier = modifier
      .size(40.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
      )
      .drawBehind {
        if (isPressed) {
          drawRect(color = PressOverlayTint.copy(alpha = 0.07f))
        }
      },
    contentAlignment = Alignment.Center,
  ) {
    painter?.let {
      Icon(
        painter = it,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(21.dp),
      )
    }
  }
}

/** Convenience: CinnabarButton with text */
@Composable
fun CinnabarTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  CinnabarButton(onClick = onClick, modifier = modifier, enabled = enabled) {
    Text(
      text = text,
      style = ButtonTextStyle,
      textAlign = TextAlign.Center,
    )
  }
}

/** Convenience: PaperButton with text */
@Composable
fun PaperTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  PaperButton(onClick = onClick, modifier = modifier, enabled = enabled) {
    Text(
      text = text,
      color = Ink,
      fontSize = 14.sp,
      textAlign = TextAlign.Center,
    )
  }
}
