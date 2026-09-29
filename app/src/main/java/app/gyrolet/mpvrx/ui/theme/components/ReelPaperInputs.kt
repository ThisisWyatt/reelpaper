/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package app.gyrolet.mpvrx.ui.theme.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.gyrolet.mpvrx.ui.theme.CardPaper
import app.gyrolet.mpvrx.ui.theme.Cinnabar
import app.gyrolet.mpvrx.ui.theme.CinnabarHi
import app.gyrolet.mpvrx.ui.theme.Ink
import app.gyrolet.mpvrx.ui.theme.Ink2
import app.gyrolet.mpvrx.ui.theme.Ink3
import app.gyrolet.mpvrx.ui.theme.Line
import app.gyrolet.mpvrx.ui.theme.Paper
import app.gyrolet.mpvrx.ui.theme.PaperInset

// ===== Slider =====
@Composable
fun ReelPaperSlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
  var dragOffset by remember { mutableFloatStateOf(0f) }
  Box(
    modifier = modifier
      .height(24.dp)
      .pointerInput(Unit) {
        detectHorizontalDragGestures(
          onDragStart = { dragOffset = it.x },
          onHorizontalDrag = { change, _ ->
            change.consume()
            val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
            onValueChange(valueRange.start + fraction * (valueRange.endInclusive - valueRange.start))
          },
        )
      },
    contentAlignment = Alignment.CenterStart,
  ) {
    // Track
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(PaperInset),
    )
    // Fill
    val fraction = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
    Box(
      modifier = Modifier
        .fillMaxWidth(fraction.coerceIn(0f, 1f))
        .height(6.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(Brush.horizontalGradient(listOf(Cinnabar, CinnabarHi))),
    )
    // Thumb
    Box(
      modifier = Modifier
        .offset(x = (fraction * (size.width - 16.dp.toPx())).coerceAtLeast(0f).dp)
        .size(16.dp)
        .shadow(2.dp, CircleShape)
        .clip(CircleShape)
        .background(Brush.verticalGradient(listOf(Color(0xFFF7EDD8), Color(0xFFCBBFA4)))),
    )
  }
}

// ===== Search Bar =====
@Composable
fun ReelPaperSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "搜索",
  leadingIcon: Painter? = null,
  trailingIcon: Painter? = null,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(42.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(PaperInset),
    contentAlignment = Alignment.CenterStart,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      leadingIcon?.let {
        Icon(painter = it, contentDescription = null, tint = Ink3, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(8.dp))
      }
      BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.weight(1f),
        textStyle = TextStyle(color = Ink, fontSize = 14.sp),
        singleLine = true,
        decorationBox = { innerTextField ->
          if (query.isEmpty()) {
            Text(text = placeholder, color = Ink3, fontSize = 14.sp)
          }
          innerTextField()
        },
      )
      trailingIcon?.let {
        Spacer(modifier = Modifier.width(8.dp))
        Icon(painter = it, contentDescription = null, tint = Ink3, modifier = Modifier.size(17.dp))
      }
    }
  }
}

// ===== Segmented Control =====
@Composable
fun ReelPaperSegmentedControl(
  options: List<String>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(40.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(PaperInset)
      .padding(4.dp),
  ) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
      options.forEachIndexed { index, option ->
        val isSelected = index == selectedIndex
        Box(
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 32.dp)
            .clip(RoundedCornerShape(9.dp))
            .clickable { onSelect(index) }
            .background(
              if (isSelected) CardPaper else Color.Transparent,
              RoundedCornerShape(9.dp),
            )
            .shadow(
              elevation = if (isSelected) 3.dp else 0.dp,
              shape = RoundedCornerShape(9.dp),
              spotColor = Color(0xFF604C24).copy(0.06f),
            ),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = option,
            color = if (isSelected) Ink else Ink2,
            fontSize = 13.sp,
          )
        }
      }
    }
  }
}

// ===== Bottom Sheet =====
@Composable
fun ReelPaperSheet(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 24.dp),
      contentAlignment = Alignment.BottomCenter,
    ) {
      Column(
        modifier = modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(CardPaper)
          .drawBehind {
            // Top highlight inset
            drawRect(
              color = Color.White.copy(alpha = 0.7f),
              topLeft = androidx.compose.ui.geometry.Offset.Zero,
              size = androidx.compose.ui.geometry.Size(size.width, 1.dp.toPx()),
            )
          }
          .padding(20.dp),
        content = content,
      )
    }
  }
}

// ===== Form Input Slot =====
@Composable
fun ReelPaperInputSlot(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  label: String? = null,
  placeholder: String = "",
) {
  Column(modifier = modifier.fillMaxWidth()) {
    label?.let {
      Text(text = it.uppercase(), color = Ink3, fontSize = 11.sp, letterSpacing = 2.sp)
      Spacer(modifier = Modifier.height(4.dp))
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(40.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(PaperInset)
        .padding(horizontal = 12.dp),
      contentAlignment = Alignment.CenterStart,
    ) {
      BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(color = Ink, fontSize = 14.sp),
        singleLine = true,
        decorationBox = { innerTextField ->
          if (value.isEmpty()) {
            Text(text = placeholder, color = Ink3, fontSize = 14.sp)
          }
          innerTextField()
        },
      )
    }
  }
}

// ===== Stepper =====
@Composable
fun ReelPaperStepper(
  value: Int,
  onValueChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
  min: Int = 0,
  max: Int = Int.MAX_VALUE,
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // Minus button
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(RoundedCornerShape(8.dp))
        .clickable { if (value > min) onValueChange(value - 1) }
        .background(CardPaper)
        .shadow(2.dp, RoundedCornerShape(8.dp)),
      contentAlignment = Alignment.Center,
    ) {
      Text(text = "−", color = Ink, fontSize = 16.sp)
    }
    // Value display
    Box(
      modifier = Modifier
        .width(48.dp)
        .height(32.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(PaperInset),
      contentAlignment = Alignment.Center,
    ) {
      Text(text = value.toString(), color = Ink, fontSize = 14.sp)
    }
    // Plus button
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(RoundedCornerShape(8.dp))
        .clickable { if (value < max) onValueChange(value + 1) }
        .background(CardPaper)
        .shadow(2.dp, RoundedCornerShape(8.dp)),
      contentAlignment = Alignment.Center,
    ) {
      Text(text = "+", color = Ink, fontSize = 16.sp)
    }
  }
}
