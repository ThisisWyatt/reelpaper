/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package app.gyrolet.mpvrx.ui.theme.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.gyrolet.mpvrx.ui.theme.Cinnabar
import app.gyrolet.mpvrx.ui.theme.CinnabarHi
import app.gyrolet.mpvrx.ui.theme.Paper
import app.gyrolet.mpvrx.ui.theme.PaperInset
import app.gyrolet.mpvrx.ui.theme.ShadowTint

@Composable
fun ReelPaperSwitch(
  checked: Boolean,
  onCheckedChange: ((Boolean) -> Unit)?,
  modifier: Modifier = Modifier,
  mini: Boolean = false,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val trackW = if (mini) 40.dp else 48.dp
  val trackH = if (mini) 24.dp else 29.dp
  val thumbSize = if (mini) 19.dp else 24.dp
  val thumbOffset by animateDpAsState(
    targetValue = if (checked) (trackW - thumbSize - 4.dp) else 2.dp,
    label = "thumb",
  )

  Box(
    modifier = modifier
      .size(width = trackW, height = trackH)
      .clip(RoundedCornerShape(trackH / 2))
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = onCheckedChange != null,
        onClick = { onCheckedChange?.invoke(!checked) },
      )
      .background(
        brush = if (checked) Brush.horizontalGradient(listOf(CinnabarHi, Cinnabar))
        else Brush.verticalGradient(listOf(PaperInset, Color(0xFFD8CDB5))),
        shape = RoundedCornerShape(trackH / 2),
      ),
    contentAlignment = Alignment.CenterStart,
  ) {
    Box(
      modifier = Modifier
        .padding(start = thumbOffset)
        .size(thumbSize)
        .shadow(2.dp, CircleShape, spotColor = ShadowTint.copy(0.10f))
        .clip(CircleShape)
        .background(
          brush = Brush.verticalGradient(listOf(Color(0xFFFFFDF6), Color(0xFFEDE4CE))),
          shape = CircleShape,
        ),
    )
  }
}
