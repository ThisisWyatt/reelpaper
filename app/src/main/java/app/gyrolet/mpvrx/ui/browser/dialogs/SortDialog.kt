/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.dialogs

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import app.gyrolet.mpvrx.ui.components.AppSlider as Slider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.ui.icons.AppIcon
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.ui.player.controls.components.rememberTvInitialFocusRequester
import app.gyrolet.mpvrx.ui.player.controls.components.tvFocusGroup
import app.gyrolet.mpvrx.ui.player.controls.components.tvFocusHighlight
import app.gyrolet.mpvrx.ui.player.controls.components.tvInitialFocus
import app.gyrolet.mpvrx.ui.theme.AppMotion
import app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors
import app.gyrolet.mpvrx.ui.theme.paper.PaperIconButton
import app.gyrolet.mpvrx.ui.theme.paper.PaperRaisedButton
import app.gyrolet.mpvrx.ui.theme.paper.PaperSolidButton
import app.gyrolet.mpvrx.ui.theme.paper.paperInset
import app.gyrolet.mpvrx.ui.theme.paper.paperRaise
import app.gyrolet.mpvrx.ui.utils.rememberAppHaptics
import kotlin.math.roundToInt

/**
 * The paper redesign of the sort & view dialog (mockup `#modal-sort`): a centered card-paper
 * modal — not a bottom sheet — with letter-spaced section labels, text pill selectors, square
 * paper checkboxes, a −/＋ stepper for grid columns and the 取消/完成 key pair at the bottom.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SortDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit,
  title: String,
  sortType: String,
  onSortTypeChange: (String) -> Unit,
  sortOrderAsc: Boolean,
  onSortOrderChange: (Boolean) -> Unit,
  types: List<String>,
  icons: List<AppIcon>,
  getLabelForType: (String, Boolean) -> Pair<String, String>,
  modifier: Modifier = Modifier,
  mediaTypeToggles: List<VisibilityToggle> = emptyList(),
  visibilityToggles: List<VisibilityToggle> = emptyList(),
  viewModeSelector: MultiViewModeSelector? = null,
  layoutModeSelector: ViewModeSelector? = null,
  manualGridToggle: VisibilityToggle? = null,
  folderGridColumnSelector: GridColumnSelector? = null,
  videoGridColumnSelector: GridColumnSelector? = null,
  showSortOptions: Boolean = true,
  enableViewModeOptions: Boolean = true,
  enableLayoutModeOptions: Boolean = true,
) {
  if (!isOpen) return

  // The same movable content is reused by Home's Folder, Tree and Library call sites.
  // Moving only the open flag would recreate the modal and replay its opening animation.
  val sharedDialog = LocalBrowserSortDialogState.current
  val sheetContent: @Composable () -> Unit = {
    val haptics = rememberAppHaptics()
    var isFieldsExpanded by rememberSaveable { mutableStateOf(false) }

    val (ascLabel, descLabel) = getLabelForType(sortType, sortOrderAsc)

    PaperSortModal(
      onDismiss = onDismiss,
      title = title,
      modifier = modifier,
      footer = {
        // Same width and body height for both keys; the solid key's 3dp step is its shadow.
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          PaperRaisedButton(
            text = androidx.compose.ui.res.stringResource(R.string.generic_cancel),
            onClick = onDismiss,
            height = 50.dp,
            modifier = Modifier.weight(1f),
          )
          PaperSolidButton(
            text = androidx.compose.ui.res.stringResource(R.string.ui_done),
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
          )
        }
      },
    ) {
      if (showSortOptions && types.isNotEmpty()) {
        PaperSectionLabel(
          text = androidx.compose.ui.res.stringResource(R.string.ui_sort_by),
          isFirst = true,
        )
        PaperRadioPillRow(
          options = types,
          selected = sortType,
          onSelect = onSortTypeChange,
          modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
        )
        PaperSectionLabel(text = androidx.compose.ui.res.stringResource(R.string.sort_order))
        PaperRadioPillRow(
          options = listOf(ascLabel, descLabel),
          selected = if (sortOrderAsc) ascLabel else descLabel,
          onSelect = { selected -> onSortOrderChange(selected == ascLabel) },
          modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
        )
      }

      viewModeSelector?.let { viewMode ->
        PaperSectionLabel(text = viewMode.label)
        PaperRadioPillRow(
          options = viewMode.options.map { it.label },
          selected = viewMode.options.firstOrNull { it.isSelected }?.label,
          onSelect = { selected ->
            viewMode.options
              .firstOrNull { it.label == selected }
              ?.let { option ->
                if (enableViewModeOptions && !option.isSelected) {
                  option.onClick()
                  haptics.selection(true)
                }
              }
          },
          modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
        )
      }

      layoutModeSelector?.let { layoutMode ->
        PaperSectionLabel(text = layoutMode.label)
        // A page with three layouts owns its own selection, so the third option decides whether
        // the middle one is the one showing; with only two the flag still describes both.
        val thirdSelected = layoutMode.thirdOption?.isSelected == true
        val isFirstSelected = layoutMode.isFirstOptionSelected && !thirdSelected
        val options =
          buildList {
            add(
              ViewModeOption(
                label = layoutMode.firstOptionLabel,
                icon = layoutMode.firstOptionIcon,
                isSelected = isFirstSelected,
                onClick = { layoutMode.onViewModeChange(true) },
              ),
            )
            add(
              ViewModeOption(
                label = layoutMode.secondOptionLabel,
                icon = layoutMode.secondOptionIcon,
                isSelected = !layoutMode.isFirstOptionSelected && !thirdSelected,
                onClick = { layoutMode.onViewModeChange(false) },
              ),
            )
            layoutMode.thirdOption?.let(::add)
          }
        PaperRadioPillRow(
          options = options.map { it.label },
          selected = options.firstOrNull { it.isSelected }?.label,
          onSelect = { selected ->
            options
              .firstOrNull { it.label == selected }
              ?.let { option ->
                if (enableLayoutModeOptions && !option.isSelected) {
                  option.onClick()
                  haptics.selection(true)
                }
              }
          },
          modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
        )
        val showSeparateLayout =
          !isFirstSelected && layoutMode.checkboxLabel != null && layoutMode.onCheckboxChange != null
        val showManualGrid = manualGridToggle != null && !isFirstSelected
        if (showSeparateLayout || showManualGrid) {
          PaperSectionLabel(
            text = androidx.compose.ui.res.stringResource(R.string.options_section),
          )
          Column(modifier = Modifier.padding(top = 4.dp)) {
            if (showSeparateLayout) {
              PaperCheckboxRow(
                label = layoutMode.checkboxLabel.orEmpty(),
                checked = layoutMode.isCheckboxChecked,
                enabled = enableLayoutModeOptions,
                onCheckedChange = { checked ->
                  layoutMode.onCheckboxChange?.invoke(checked)
                  haptics.selection(checked)
                },
              )
            }
            if (showManualGrid) {
              PaperCheckboxRow(
                label = manualGridToggle?.label.orEmpty(),
                checked = manualGridToggle?.checked == true,
                enabled = enableLayoutModeOptions && (manualGridToggle?.enabled ?: true),
                onCheckedChange = { checked ->
                  manualGridToggle?.onCheckedChange?.invoke(checked)
                  haptics.selection(checked)
                },
              )
            }
          }
        }
      }

      GridColumnsSection(
        folderGridColumnSelector = folderGridColumnSelector,
        videoGridColumnSelector = videoGridColumnSelector,
      )

      if (mediaTypeToggles.isNotEmpty()) {
        PaperSectionLabel(
          text = androidx.compose.ui.res.stringResource(R.string.ui_media_types),
        )
        FlowRow(
          modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          mediaTypeToggles.forEach { toggle ->
            PaperCheckPill(
              label = toggle.label,
              checked = toggle.checked,
              enabled = toggle.enabled,
              onCheckedChange = toggle.onCheckedChange,
            )
          }
        }
      }

      if (visibilityToggles.isNotEmpty()) {
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
              ) { isFieldsExpanded = !isFieldsExpanded }
              .padding(top = 17.dp, bottom = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = androidx.compose.ui.res.stringResource(R.string.ui_fields),
            fontSize = 12.sp,
            letterSpacing = 2.5.sp,
            fontWeight = FontWeight.Bold,
            color = LocalPaperColors.current.inkTertiary,
          )
          Icon(
            imageVector =
              if (isFieldsExpanded) {
                Icons.RoundedFilled.KeyboardArrowUp
              } else {
                Icons.RoundedFilled.KeyboardArrowDown
              },
            contentDescription =
              androidx.compose.ui.res.stringResource(
                if (isFieldsExpanded) R.string.generic_collapse else R.string.generic_expand,
              ),
            tint = LocalPaperColors.current.inkTertiary,
            modifier = Modifier.size(16.dp),
          )
        }
        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .animateContentSize(
                animationSpec = AppMotion.spatial(tween(durationMillis = 250), snap()),
              ),
        ) {
          if (isFieldsExpanded) {
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              visibilityToggles.forEach { toggle ->
                PaperCheckPill(
                  label = toggle.label,
                  checked = toggle.checked,
                  enabled = toggle.enabled,
                  onCheckedChange = toggle.onCheckedChange,
                )
              }
            }
          }
        }
      }
    }
  }
  if (sharedDialog != null && sharedDialog.isOpen.value) {
    sharedDialog.content(sheetContent)
  } else {
    sheetContent()
  }
}

/**
 * The centered paper modal shell (mockup `.modal` + `.sheet`): warm dim scrim over the screen,
 * a card-paper sheet capped at the mockup's 620px height that scrolls when the content is
 * taller, with the title and close key pinned above the body.
 */
@Composable
private fun PaperSortModal(
  onDismiss: () -> Unit,
  title: String,
  modifier: Modifier = Modifier,
  footer: @Composable () -> Unit = {},
  content: @Composable () -> Unit,
) {
  val colors = LocalPaperColors.current
  val configuration = LocalConfiguration.current
  val maxSheetHeight = minOf(620.dp, (configuration.screenHeightDp - 52).dp).coerceAtLeast(200.dp)
  val shape = RoundedCornerShape(18.dp)
  val scrollState = rememberScrollState()

  Dialog(
    onDismissRequest = onDismiss,
    properties =
      DialogProperties(
        usePlatformDefaultWidth = false,
        decorFitsSystemWindows = false,
      ),
  ) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    // Mockup `pop`: scale .9→1 with a 14px drop-in, scrim fades over `fade`.
    val progress by animateFloatAsState(
      targetValue = if (shown) 1f else 0f,
      animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
      label = "paperModalPop",
    )
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(Color(0xFF342A18).copy(alpha = 0.45f * progress))
          .displayCutoutPadding()
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
          ) { onDismiss },
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier =
          modifier
            .padding(horizontal = 24.dp, vertical = 26.dp)
            .widthIn(max = 520.dp)
            .heightIn(max = maxSheetHeight)
            .graphicsLayer {
              alpha = progress
              val scale = 0.9f + 0.1f * progress
              scaleX = scale
              scaleY = scale
              translationY = (1f - progress) * 14.dp.toPx()
            }
            .shadow(
              elevation = 24.dp,
              shape = shape,
              clip = false,
              ambientColor = Color(0xFF1E1608).copy(alpha = 0.45f),
              spotColor = Color(0xFF1E1608).copy(alpha = 0.45f),
            )
            .paperRaise(shape, colors, elevation = 18.dp)
            .border(1.dp, colors.cardLine, shape)
            .clip(shape)
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
            ) { }
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = colors.ink,
          )
          PaperIconButton(
            icon = Icons.RoundedFilled.Close,
            contentDescription = androidx.compose.ui.res.stringResource(R.string.generic_close),
            onClick = onDismiss,
          )
        }
        Column(
          modifier =
            Modifier
              .weight(1f, fill = false)
              .verticalScroll(scrollState),
        ) {
          content()
        }
        footer()
      }
    }
  }
}

/** Mockup `.mlab`: 12px bold letter-spaced tertiary label above each form group. */
@Composable
private fun PaperSectionLabel(
  text: String,
  modifier: Modifier = Modifier,
  isFirst: Boolean = false,
) {
  Text(
    text = text,
    modifier = modifier.then(if (isFirst) Modifier else Modifier.padding(top = 17.dp)),
    fontSize = 12.sp,
    letterSpacing = 2.5.sp,
    fontWeight = FontWeight.Bold,
    color = LocalPaperColors.current.inkTertiary,
  )
}

/** Mockup `.opt-group` + `.opt`: wrapping row of single-select text pills. */
@Composable
private fun PaperRadioPillRow(
  options: List<String>,
  selected: String?,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val initialFocusRequester =
    rememberTvInitialFocusRequester(options.isNotEmpty(), requestKey = selected.orEmpty())
  FlowRow(
    modifier = modifier.tvFocusGroup().selectableGroup(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    options.forEach { label ->
      PaperRadioPill(
        label = label,
        selected = label == selected,
        onSelect = onSelect,
        modifier =
          if (label == selected) {
            Modifier.tvInitialFocus(initialFocusRequester)
          } else {
            Modifier
          },
      )
    }
  }
}

@Composable
private fun PaperRadioPill(
  label: String,
  selected: Boolean,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  val colors = LocalPaperColors.current
  val haptics = rememberAppHaptics()
  val shape = RoundedCornerShape(10.dp)
  Box(
    modifier
      .heightIn(min = 36.dp)
      .clip(shape)
      .then(
        if (selected) {
          Modifier
            .background(colors.card, shape)
            .border(1.5.dp, colors.red, shape)
        } else {
          Modifier.background(colors.paperInset, shape)
        },
      )
      .tvFocusHighlight(shape, focusedScale = 1.04f)
      .selectable(
        selected = selected,
        enabled = enabled,
        role = Role.RadioButton,
        onClick = {
          if (!selected) {
            onSelect(label)
            haptics.selection(true)
          }
        },
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true),
      )
      .padding(horizontal = 15.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label,
      fontSize = 13.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
      color = if (selected) colors.red else colors.inkSecondary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

/** Toggle pill for the media-type and field groups — same shape as [PaperRadioPill]. */
@Composable
private fun PaperCheckPill(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  val colors = LocalPaperColors.current
  val haptics = rememberAppHaptics()
  val shape = RoundedCornerShape(10.dp)
  Box(
    modifier
      .heightIn(min = 36.dp)
      .clip(shape)
      .then(
        if (checked) {
          Modifier
            .background(colors.card, shape)
            .border(1.5.dp, colors.red, shape)
        } else {
          Modifier.background(colors.paperInset, shape)
        },
      )
      .tvFocusHighlight(shape, focusedScale = 1.04f)
      .toggleable(
        value = checked,
        enabled = enabled,
        role = Role.Checkbox,
        onValueChange = {
          onCheckedChange(!checked)
          haptics.selection(!checked)
        },
      )
      .padding(horizontal = 15.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label,
      fontSize = 13.sp,
      fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
      color = if (checked) colors.red else colors.inkSecondary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

/** Mockup `.chk`: square recessed paper box; filled with the vermilion gradient when on. */
@Composable
private fun PaperCheckbox(checked: Boolean, enabled: Boolean = true) {
  val colors = LocalPaperColors.current
  val shape = RoundedCornerShape(7.dp)
  Box(
    modifier =
      Modifier
        .size(21.dp)
        .graphicsLayer { if (!enabled) alpha = 0.38f }
        .then(
          if (checked) {
            Modifier
              .background(
                Brush.verticalGradient(listOf(colors.redHighlight, colors.red)),
                shape,
              )
              .border(0.5.dp, colors.redDark.copy(alpha = 0.4f), shape)
          } else {
            Modifier.paperInset(shape, colors)
          },
        ),
    contentAlignment = Alignment.Center,
  ) {
    if (checked) {
      Icon(
        imageVector = Icons.RoundedFilled.Check,
        contentDescription = null,
        tint = colors.onRed,
        modifier = Modifier.size(13.dp),
      )
    }
  }
}

@Composable
private fun PaperCheckboxRow(
  label: String,
  checked: Boolean,
  enabled: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  val colors = LocalPaperColors.current
  val haptics = rememberAppHaptics()
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .heightIn(min = 40.dp)
        .clip(RoundedCornerShape(8.dp))
        .toggleable(
          value = checked,
          enabled = enabled,
          role = Role.Checkbox,
          onValueChange = { value ->
            onCheckedChange(value)
            haptics.selection(value)
          },
        )
        .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(9.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    PaperCheckbox(checked = checked, enabled = enabled)
    Text(
      text = label,
      fontSize = 13.sp,
      color = if (checked) colors.ink else colors.inkSecondary,
    )
  }
}

/**
 * Grid-column count as a mockup `.stepper` (− value ＋). The dp-based selectors (cover-art
 * size) keep a slider — a 40–128dp range is not steppable.
 */
@Composable
private fun GridColumnsSection(
  folderGridColumnSelector: GridColumnSelector?,
  videoGridColumnSelector: GridColumnSelector?,
) {
  val selectors = listOfNotNull(folderGridColumnSelector, videoGridColumnSelector)
  if (selectors.isEmpty()) return

  val haptics = rememberAppHaptics()
  val colors = LocalPaperColors.current
  val columnSelectors = selectors.filter { it.unitSuffix == "cols" }
  if (columnSelectors.isNotEmpty()) {
    val minColumns = columnSelectors.minOf { it.valueRange.start.roundToInt() }
    val maxColumns = columnSelectors.maxOf { it.valueRange.endInclusive.roundToInt() }
    PaperSectionLabel(
      text =
        androidx.compose.ui.res
          .stringResource(R.string.grid_columns_range, minColumns, maxColumns),
    )
    if (columnSelectors.size == 1) {
      val selector = columnSelectors.first()
      PaperStepper(
        value = selector.currentValue,
        minValue = selector.valueRange.start.roundToInt(),
        maxValue = selector.valueRange.endInclusive.roundToInt(),
        onValueChange = {
          selector.onValueChange(it)
          haptics.tick()
        },
        modifier = Modifier.padding(top = 11.dp),
      )
    } else {
      Column(
        modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
      ) {
        columnSelectors.forEach { selector ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = selector.label,
              fontSize = 13.sp,
              color = colors.inkSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            PaperStepper(
              value = selector.currentValue,
              minValue = selector.valueRange.start.roundToInt(),
              maxValue = selector.valueRange.endInclusive.roundToInt(),
              onValueChange = {
                selector.onValueChange(it)
                haptics.tick()
              },
            )
          }
        }
      }
    }
  }

  selectors
    .firstOrNull { it.unitSuffix != "cols" }
    ?.let { selector ->
      PaperSectionLabel(text = selector.label)
      Text(
        text = "${selector.currentValue} ${selector.unitSuffix}",
        modifier = Modifier.padding(top = 4.dp),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = colors.red,
      )
      Slider(
        value = selector.currentValue.toFloat(),
        onValueChange = {
          val newValue = it.roundToInt()
          if (newValue != selector.currentValue) {
            selector.onValueChange(newValue)
            haptics.tick()
          }
        },
        valueRange = selector.valueRange,
        steps = selector.steps,
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .tvFocusHighlight(RoundedCornerShape(8.dp), focusedScale = 1.01f),
      )
    }
}

@Composable
private fun PaperStepper(
  value: Int,
  minValue: Int,
  maxValue: Int,
  onValueChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalPaperColors.current
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    PaperStepperKey(
      symbol = "−",
      desc = androidx.compose.ui.res.stringResource(R.string.generic_decrease),
      enabled = value > minValue,
      onClick = { onValueChange(value - 1) },
    )
    Text(
      text = value.toString(),
      modifier = Modifier.widthIn(min = 28.dp),
      fontSize = 15.sp,
      fontWeight = FontWeight.SemiBold,
      textAlign = TextAlign.Center,
      color = colors.ink,
    )
    PaperStepperKey(
      symbol = "＋",
      desc = androidx.compose.ui.res.stringResource(R.string.generic_increase),
      enabled = value < maxValue,
      onClick = { onValueChange(value + 1) },
    )
  }
}

/** Mockup `.stepper button`: 40dp raised card key with a card→paper-in gradient. */
@Composable
private fun PaperStepperKey(
  symbol: String,
  desc: String,
  enabled: Boolean,
  onClick: () -> Unit,
) {
  val colors = LocalPaperColors.current
  val shape = RoundedCornerShape(11.dp)
  Box(
    modifier =
      Modifier
        .size(40.dp)
        .semantics { contentDescription = desc }
        .graphicsLayer { if (!enabled) alpha = 0.38f }
        .shadow(
          elevation = 2.dp,
          shape = shape,
          clip = false,
          ambientColor = colors.shadowTint.copy(alpha = 0.3f),
          spotColor = colors.shadowTint.copy(alpha = 0.3f),
        )
        .border(1.dp, colors.cardLine, shape)
        .background(
          Brush.verticalGradient(listOf(colors.card, colors.paperInset)),
          shape,
        )
        .tvFocusHighlight(shape, focusedScale = 1.06f)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          enabled = enabled,
          role = Role.Button,
          onClick = onClick,
        ),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = symbol,
      fontSize = 20.sp,
      color = colors.ink,
    )
  }
}

data class VisibilityToggle(
  val label: String,
  val checked: Boolean,
  val onCheckedChange: (Boolean) -> Unit,
  val enabled: Boolean = true,
)

data class ViewModeOption(
  val label: String,
  val icon: AppIcon,
  val isSelected: Boolean,
  val onClick: () -> Unit,
)

data class MultiViewModeSelector(
  val label: String,
  val options: List<ViewModeOption>,
)

data class ViewModeSelector(
  val label: String,
  val firstOptionLabel: String,
  val secondOptionLabel: String,
  val firstOptionIcon: AppIcon,
  val secondOptionIcon: AppIcon,
  val isFirstOptionSelected: Boolean,
  val onViewModeChange: (Boolean) -> Unit,
  val checkboxLabel: String? = null,
  val isCheckboxChecked: Boolean = false,
  val onCheckboxChange: ((Boolean) -> Unit)? = null,
  /**
   * A third choice, for pages that have one — the snapshots' mosaic. When it is present it owns the
   * selection state: the caller passes [isFirstOptionSelected] = false and marks this one selected.
   */
  val thirdOption: ViewModeOption? = null,
)

data class GridColumnSelector(
  val label: String,
  val currentValue: Int,
  val onValueChange: (Int) -> Unit,
  val valueRange: ClosedFloatingPointRange<Float> = 1f..4f,
  val steps: Int = 2,
  val unitSuffix: String = "cols",
)
