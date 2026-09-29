/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package app.gyrolet.mpvrx.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gyrolet.mpvrx.ui.theme.CardLine
import app.gyrolet.mpvrx.ui.theme.CardPaper
import app.gyrolet.mpvrx.ui.theme.Ink
import app.gyrolet.mpvrx.ui.theme.Ink2
import app.gyrolet.mpvrx.ui.theme.Ink3
import app.gyrolet.mpvrx.ui.theme.Line
import app.gyrolet.mpvrx.ui.theme.PaperInset
import app.gyrolet.mpvrx.ui.theme.ShadowTint

@Composable
fun ReelPaperListCard(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = ShadowTint.copy(0.08f))
      .background(CardPaper, RoundedCornerShape(14.dp))
      .padding(vertical = 8.dp),
    content = content,
  )
}

@Composable
fun ReelPaperListRow(
  icon: Painter? = null,
  title: String,
  subtitle: String? = null,
  trailing: @Composable (() -> Unit)? = null,
  onClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
  showDivider: Boolean = true,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (icon != null) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .background(PaperInset, RoundedCornerShape(11.dp))
          .padding(8.dp),
        contentAlignment = Alignment.Center,
      ) {
        Icon(painter = icon, contentDescription = null, tint = Ink2, modifier = Modifier.size(21.dp))
      }
      Spacer(modifier = Modifier.width(12.dp))
    }
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, color = Ink, fontSize = 14.sp)
      subtitle?.let {
        Text(text = it, color = Ink2, fontSize = 12.sp)
      }
    }
    trailing?.invoke()
  }
  if (showDivider) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = if (icon != null) 66.dp else 16.dp, end = 16.dp)
        .height(1.dp)
        .background(Line),
    )
  }
}

@Composable
fun StackedPaperCard(
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
  content: @Composable RowScope.() -> Unit,
) {
  Box(modifier = modifier.fillMaxWidth()) {
    // Layer 2 (bottom)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 8.dp, top = 8.dp)
        .graphicsLayer { alpha = 0.75f; rotationZ = -0.6f }
        .shadow(4.dp, RoundedCornerShape(14.dp))
        .background(CardPaper, RoundedCornerShape(14.dp))
        .height(72.dp),
    )
    // Layer 1 (middle)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 4.dp, top = 4.dp)
        .graphicsLayer { rotationZ = 0.7f }
        .shadow(5.dp, RoundedCornerShape(14.dp))
        .background(CardPaper, RoundedCornerShape(14.dp))
        .height(72.dp),
    )
    // Main card
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = ShadowTint.copy(0.10f))
        .background(CardPaper, RoundedCornerShape(14.dp))
        .clickable(onClick = onClick)
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      content = content,
    )
  }
}
