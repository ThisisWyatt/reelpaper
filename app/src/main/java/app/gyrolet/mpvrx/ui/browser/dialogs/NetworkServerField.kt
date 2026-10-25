/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.data.network.LocalServerDiscovery
import app.gyrolet.mpvrx.data.network.ServerDiscoveryState
import app.gyrolet.mpvrx.data.network.ServerSuggestion
import app.gyrolet.mpvrx.data.network.ServerSuggestionSource
import app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors
import app.gyrolet.mpvrx.ui.theme.paper.paperInset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

/**
 * 服务器地址 slot per mockup `.slot`: recessed paper groove with a borderless ink input, a
 * dropdown anchor for saved/nearby servers, the 查找附近的服务器 link and the discovery hint.
 */
@Composable
internal fun NetworkServerField(
  value: String,
  onValueChange: (String) -> Unit,
  onSelected: (ServerSuggestion) -> Unit,
  savedServers: List<ServerSuggestion>,
  modifier: Modifier = Modifier,
  isError: Boolean = false,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val colors = LocalPaperColors.current
  val discovery = remember(context) { LocalServerDiscovery(context) }
  var expanded by remember { mutableStateOf(false) }
  var query by remember { mutableStateOf("") }
  var scanRequest by remember { mutableIntStateOf(0) }
  var scan by remember { mutableStateOf(ServerDiscoveryState()) }
  var addresses by remember { mutableStateOf(emptyList<ServerSuggestion>()) }

  LaunchedEffect(discovery, expanded, scanRequest) {
    if (expanded) addresses = withContext(Dispatchers.IO) { discovery.localAddresses() }
  }
  LaunchedEffect(discovery, lifecycleOwner, scanRequest) {
    if (scanRequest == 0) return@LaunchedEffect
    lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
      scan = ServerDiscoveryState(isScanning = true)
      try {
        discovery.discover().collect { scan = it }
      } finally {
        scan = scan.copy(isScanning = false)
      }
    }
  }
  val suggestions = remember(savedServers, scan.servers, addresses, query) {
    (savedServers + scan.servers + addresses)
      .distinctBy { listOf(it.host.lowercase(), it.port, it.protocol, it.path, it.useHttps) }
      .filter {
        query.isBlank() || it.host.contains(query, true) || it.name.contains(query, true) ||
          it.protocol?.displayName?.contains(query, true) == true
      }
  }

  Column(modifier) {
    ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = it; query = "" },
    ) {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(44.dp)
            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
            .paperInset(RoundedCornerShape(11.dp), colors)
            .then(
              if (isError) {
                Modifier.border(1.dp, colors.red, RoundedCornerShape(11.dp))
              } else {
                Modifier
              },
            )
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(Modifier.weight(1f)) {
          BasicTextField(
            value = value,
            onValueChange = { onValueChange(it); query = it; expanded = true },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = colors.ink),
            cursorBrush = SolidColor(colors.red),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth(),
          )
          if (value.isEmpty()) {
            Text(
              text = "192.168.31.222",
              fontSize = 14.sp,
              color = colors.inkTertiary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.align(Alignment.CenterStart),
            )
          }
        }
        Box(Modifier.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable)) {
          ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }
      }
      ExposedDropdownMenu(
        shape = MaterialTheme.shapes.extraLargeIncreased,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.heightIn(max = 280.dp),
      ) {
        DropdownMenuItem(
          text = { Text(stringResource(if (scan.isScanning) R.string.network_server_scanning else R.string.network_server_scan)) },
          enabled = !scan.isScanning,
          onClick = { query = ""; scanRequest++ },
        )
        if (suggestions.isEmpty()) {
          DropdownMenuItem(
            text = { Text(stringResource(R.string.network_server_no_matches)) },
            onClick = {},
            enabled = false,
          )
        }
        suggestions.forEach { suggestion ->
          DropdownMenuItem(
            text = {
              Column {
                Text(
                  suggestion.name.ifBlank { suggestion.authority },
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                val sourceLabel = stringResource(
                  when (suggestion.source) {
                    ServerSuggestionSource.SAVED -> R.string.network_server_saved
                    ServerSuggestionSource.DISCOVERED -> R.string.network_server_nearby
                    ServerSuggestionSource.DEVICE -> R.string.network_server_this_device
                    ServerSuggestionSource.LOOPBACK -> R.string.network_server_loopback
                  },
                )
                Text(
                  "${suggestion.authority} · $sourceLabel",
                  style = MaterialTheme.typography.bodySmall,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            },
            trailingIcon = {
              Text(
                suggestion.protocol?.displayName?.let {
                  if (suggestion.useHttps) "$it (HTTPS)" else it
                } ?: stringResource(R.string.network_server_address_only),
                style = MaterialTheme.typography.labelSmall,
              )
            },
            onClick = { expanded = false; query = ""; onSelected(suggestion) },
          )
        }
      }
    }
    // 查找附近的服务器: red ink link, mockup accent style.
    Text(
      text = stringResource(if (scan.isScanning) R.string.network_server_scanning else R.string.network_server_scan),
      fontSize = 13.sp,
      color = colors.red,
      modifier =
        Modifier
          .padding(top = 8.dp)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            enabled = !scan.isScanning,
          ) { query = ""; expanded = true; scanRequest++ },
    )
    Text(
      text = stringResource(
        when {
          scan.unavailable -> R.string.network_server_scan_unavailable
          scanRequest > 0 && !scan.isScanning && scan.servers.isEmpty() -> R.string.network_server_none_found
          else -> R.string.network_server_discovery_hint
        },
      ),
      fontSize = 11.sp,
      lineHeight = 15.sp,
      color = colors.inkTertiary,
      modifier = Modifier.padding(top = 4.dp),
    )
  }
}
