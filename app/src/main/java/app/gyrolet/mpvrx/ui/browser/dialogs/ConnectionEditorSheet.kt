/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.dialogs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.data.network.ServerSuggestion
import app.gyrolet.mpvrx.domain.network.NetworkAddress
import app.gyrolet.mpvrx.domain.network.NetworkConnection
import app.gyrolet.mpvrx.domain.network.NetworkPath
import app.gyrolet.mpvrx.domain.network.NetworkProtocol
import app.gyrolet.mpvrx.domain.network.SmbSharePath
import app.gyrolet.mpvrx.domain.network.normalizedAddress
import app.gyrolet.mpvrx.ui.icons.AppIcon
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.ui.theme.paper.LocalPaperColors
import app.gyrolet.mpvrx.ui.theme.paper.PaperIconButton
import app.gyrolet.mpvrx.ui.theme.paper.PaperRaisedButton
import app.gyrolet.mpvrx.ui.theme.paper.PaperSolidButton
import app.gyrolet.mpvrx.ui.theme.paper.paperInset
import app.gyrolet.mpvrx.ui.theme.paper.paperRaise
import kotlinx.coroutines.launch

/**
 * 添加/编辑连接 per mockup `#modal-addnet`: a centered card-paper modal — protocol text pills,
 * a recessed card form (labeled rows with inset input slots) and the 测试连接 / 保存 key pair.
 * All address parsing and validation is unchanged from the previous bottom-sheet editor.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ConnectionEditorSheet(
  title: String,
  initialConnection: NetworkConnection,
  isEditing: Boolean,
  onDismiss: () -> Unit,
  onSave: (NetworkConnection, clearPassword: Boolean) -> Unit,
  modifier: Modifier = Modifier,
  savedConnections: List<NetworkConnection> = emptyList(),
  onTest: suspend (NetworkConnection) -> Result<Unit> = { Result.success(Unit) },
) {
  var name by remember(initialConnection) { mutableStateOf(initialConnection.name) }
  var protocol by remember(initialConnection) { mutableStateOf(initialConnection.protocol) }
  var host by remember(initialConnection) { mutableStateOf(initialConnection.host) }
  var port by remember(initialConnection) { mutableStateOf(initialConnection.port.toString()) }
  var path by remember(initialConnection) { mutableStateOf(initialConnection.path) }
  var isAnonymous by remember(initialConnection) { mutableStateOf(initialConnection.isAnonymous) }
  var useHttps by remember(initialConnection) { mutableStateOf(initialConnection.useHttps) }
  var username by remember(initialConnection) { mutableStateOf(initialConnection.username) }
  var password by remember(initialConnection) { mutableStateOf("") }
  var passwordVisible by remember(initialConnection) { mutableStateOf(false) }
  var clearPassword by remember(initialConnection) { mutableStateOf(false) }
  var autoConnect by remember(initialConnection) { mutableStateOf(initialConnection.autoConnect) }

  val focusManager = LocalFocusManager.current
  val hostFocusRequester = remember { FocusRequester() }
  val portFocusRequester = remember { FocusRequester() }
  val pathFocusRequester = remember { FocusRequester() }
  val usernameFocusRequester = remember { FocusRequester() }
  val passwordFocusRequester = remember { FocusRequester() }
  val parsedPort = port.toIntOrNull()
  val isPortValid = parsedPort != null && parsedPort in MIN_PORT..MAX_PORT
  val address = remember(host, protocol, useHttps) {
    runCatching { NetworkAddress.parse(host, protocol, useHttps) }.getOrNull()
  }
  val effectiveProtocol = address?.protocol ?: protocol
  val normalizedPath = remember(path, effectiveProtocol, address?.path) {
    runCatching { NetworkPath.from((address?.path ?: path).trim().let { if (effectiveProtocol == NetworkProtocol.SMB) it.replace('\\', '/') else it }).value }.getOrNull()
  }
  val isPathValid = normalizedPath != null &&
    (effectiveProtocol != NetworkProtocol.SMB || runCatching { SmbSharePath(normalizedPath) }.isSuccess)
  val canSave = address != null && isPortValid && isPathValid && (isAnonymous || username.isNotBlank())
  val resolvedConnection = remember(host, port, protocol, path, useHttps, initialConnection) {
    runCatching {
      initialConnection.copy(host = host, port = parsedPort ?: 0, protocol = protocol, path = path, useHttps = useHttps).normalizedAddress()
    }.getOrNull()
  }
  val originalAddress = remember(initialConnection) { runCatching { initialConnection.normalizedAddress() }.getOrNull() }
  val destinationChanged = resolvedConnection?.host?.lowercase() != originalAddress?.host?.lowercase() ||
    resolvedConnection?.protocol != originalAddress?.protocol || resolvedConnection?.port != originalAddress?.port ||
    resolvedConnection?.useHttps != originalAddress?.useHttps

  val dismiss = {
    focusManager.clearFocus()
    onDismiss()
  }
  fun buildDraft(): NetworkConnection? {
    val resolvedAddress = resolvedConnection ?: return null
    return initialConnection.copy(
      name = name.trim().ifBlank { "${effectiveProtocol.displayName} - ${resolvedAddress.host}" },
      protocol = effectiveProtocol,
      host = resolvedAddress.host,
      port = resolvedAddress.port,
      username = if (isAnonymous) "" else username.trim(),
      password = if (isAnonymous) "" else password,
      path = requireNotNull(normalizedPath),
      isAnonymous = isAnonymous,
      useHttps = resolvedAddress.useHttps,
    )
  }
  val save = {
    val draft = buildDraft()
    if (draft != null) {
      focusManager.clearFocus()
      onSave(
        draft,
        isAnonymous || clearPassword || (destinationChanged && password.isEmpty()),
      )
    }
  }

  // 测试连接: attempt a throwaway connection with the draft settings; the mock reports
  // through toasts (`data-toast`), which also keeps the result visible above the fold.
  val context = LocalContext.current
  val testScope = rememberCoroutineScope()
  var isTesting by remember { mutableStateOf(false) }
  LaunchedEffect(name, protocol, host, port, path, username, password, isAnonymous, useHttps) {
    isTesting = false
  }
  fun runTest() {
    val draft = buildDraft() ?: return
    focusManager.clearFocus()
    android.widget.Toast.makeText(context, context.getString(R.string.network_testing), android.widget.Toast.LENGTH_SHORT).show()
    testScope.launch {
      isTesting = true
      try {
        val result = onTest(draft)
        val message =
          if (result.isSuccess) {
            context.getString(R.string.network_test_success)
          } else {
            context.getString(
              R.string.network_test_failed,
              result.exceptionOrNull()?.message.orEmpty().ifBlank { context.getString(R.string.network_test_unknown_error) },
            )
          }
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
      } finally {
        isTesting = false
      }
    }
  }

  PaperConnectionModal(onDismiss = dismiss, title = title, modifier = modifier) {
    // 连接协议
    PaperModalSectionLabel(
      text = stringResource(R.string.ui_protocol),
      modifier = Modifier.padding(top = 8.dp),
    )
    FlowRow(
      modifier = Modifier.fillMaxWidth().selectableGroup().padding(top = 9.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      // Mockup chip order: WebDAV · SFTP · SMB · FTP.
      listOf(NetworkProtocol.WEBDAV, NetworkProtocol.SFTP, NetworkProtocol.SMB, NetworkProtocol.FTP)
        .forEach { entry ->
          PaperProtocolChip(
            label = entry.displayName,
            selected = protocol == entry,
            onSelect = {
              protocol = entry
              port = entry.defaultPort.toString()
              if (entry != NetworkProtocol.WEBDAV) useHttps = false
            },
          )
        }
    }

    // 服务器信息
    PaperModalSectionLabel(text = stringResource(R.string.network_server_info), modifier = Modifier.padding(top = 17.dp))
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(top = 9.dp)
          .paperRaise(RoundedCornerShape(14.dp), LocalPaperColors.current)
          .border(1.dp, LocalPaperColors.current.cardLine, RoundedCornerShape(14.dp))
          .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp),
    ) {
      // 名称
      PaperFormRow {
        PaperFormLabel(stringResource(R.string.ui_name))
        PaperSlot(
          value = name,
          onValueChange = { name = it },
          placeholder = stringResource(R.string.network_name_placeholder),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          keyboardActions = KeyboardActions(onNext = { hostFocusRequester.requestFocus() }),
        )
      }
      PaperFormDivider()
      // 服务器地址 + 端口
      PaperFormRow {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Column(Modifier.weight(1f)) {
            PaperFormLabel(stringResource(R.string.ui_host_ip_address))
            NetworkServerField(
              value = host,
              onValueChange = { input ->
                val pasted = input.length > host.length + 1
                host = input
                // Normalize pasted URLs/UNC paths once, leaving normal hostname typing uninterrupted.
                val parsed = runCatching { NetworkAddress.parse(input, protocol, useHttps) }.getOrNull()
                if (pasted && parsed != null && ("://" in input || input.startsWith("\\\\") || parsed.port != null || parsed.path != null)) {
                  host = parsed.host
                  if (parsed.protocol != protocol || parsed.useHttps != useHttps) {
                    port = (if (parsed.useHttps) 443 else parsed.protocol.defaultPort).toString()
                  }
                  protocol = parsed.protocol
                  useHttps = parsed.useHttps
                  parsed.port?.let { port = it.toString() }
                  parsed.path?.let { path = it }
                }
              },
              onSelected = { selected ->
                host = selected.host
                selected.protocol?.let {
                  protocol = it
                  useHttps = selected.useHttps
                  port = (selected.port ?: if (selected.useHttps) 443 else it.defaultPort).toString()
                  path = selected.path ?: "/"
                }
                if (name.isBlank()) name = selected.name
              },
              savedServers = savedConnections.filter { it.id != initialConnection.id }.mapNotNull(ServerSuggestion::saved),
              modifier = Modifier.focusRequester(hostFocusRequester),
              isError = host.isNotBlank() && address == null,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
              keyboardActions = KeyboardActions(onNext = { portFocusRequester.requestFocus() }),
            )
          }
          Column(Modifier.width(86.dp)) {
            PaperFormLabel(stringResource(R.string.ui_port))
            PaperSlot(
              value = port,
              onValueChange = { port = it.filter(Char::isDigit).take(MAX_PORT_DIGITS) },
              placeholder = "5244",
              isError = !isPortValid,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
              keyboardActions = KeyboardActions(onNext = { pathFocusRequester.requestFocus() }),
              modifier = Modifier.focusRequester(portFocusRequester),
            )
          }
        }
        if (!isPortValid) {
          PaperFormHint(stringResource(R.string.network_connection_invalid_port), isError = true)
        }
      }
      PaperFormDivider()
      // 账号 + 密码
      PaperFormRow {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Column(Modifier.weight(1f)) {
            PaperFormLabel(stringResource(R.string.ui_username))
            PaperSlot(
              value = username,
              onValueChange = { username = it },
              placeholder = stringResource(R.string.ui_optional),
              enabled = !isAnonymous && !clearPassword,
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
              keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
              modifier = Modifier.focusRequester(usernameFocusRequester),
            )
          }
          Column(Modifier.weight(1f)) {
            PaperFormLabel(
              if (isEditing && !destinationChanged) {
                stringResource(R.string.ui_new_password_keep_existing)
              } else {
                stringResource(R.string.ui_password)
              },
            )
            PaperSlot(
              value = password,
              onValueChange = {
                password = it
                if (it.isNotEmpty()) clearPassword = false
              },
              placeholder = stringResource(R.string.ui_optional),
              enabled = !isAnonymous && !clearPassword,
              visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
              keyboardActions = KeyboardActions(onDone = { save() }),
              modifier = Modifier.focusRequester(passwordFocusRequester),
              trailing = {
                PaperIconButton(
                  icon = if (passwordVisible) Icons.RoundedFilled.VisibilityOff else Icons.RoundedFilled.Visibility,
                  contentDescription = stringResource(
                    if (passwordVisible) R.string.playlist_xtream_hide_password else R.string.playlist_xtream_show_password,
                  ),
                  enabled = !isAnonymous && !clearPassword,
                  buttonSize = 32.dp,
                  iconSize = 18.dp,
                  onClick = { passwordVisible = !passwordVisible },
                )
              },
            )
          }
        }
        if (protocol == NetworkProtocol.SMB && !isAnonymous) {
          PaperFormHint(stringResource(R.string.network_smb_username_hint))
        }
      }
      PaperFormDivider()
      // 共享 / 文件夹
      PaperFormRow {
        PaperFormLabel(
          stringResource(if (effectiveProtocol == NetworkProtocol.SMB) R.string.network_smb_share_folder else R.string.ui_path),
        )
        PaperSlot(
          value = path,
          onValueChange = { path = it },
          placeholder = if (effectiveProtocol == NetworkProtocol.SMB) "/Media/Movies" else "/",
          isError = !isPathValid,
          keyboardOptions = KeyboardOptions(imeAction = if (isAnonymous) ImeAction.Done else ImeAction.Next),
          keyboardActions =
            KeyboardActions(
              onNext = { usernameFocusRequester.requestFocus() },
              onDone = { focusManager.clearFocus() },
            ),
          modifier = Modifier.focusRequester(pathFocusRequester),
        )
        if (effectiveProtocol == NetworkProtocol.SMB) {
          PaperFormHint(stringResource(R.string.network_smb_share_hint), isError = !isPathValid)
        }
      }
      // WebDAV HTTPS
      if (protocol == NetworkProtocol.WEBDAV) {
        PaperFormDivider()
        PaperFormRow {
          PaperCheckRow(
            label = stringResource(R.string.ui_use_https_secure_connection),
            checked = useHttps,
            onCheckedChange = { enabled ->
              useHttps = enabled
              if (enabled && port == "80") {
                port = "443"
              } else if (!enabled && port == "443") {
                port = "80"
              }
            },
          )
        }
      }
      // 匿名/访客访问
      PaperFormDivider()
      PaperFormRow {
        PaperCheckRow(
          label = stringResource(R.string.ui_anonymous_guest_access),
          checked = isAnonymous,
          onCheckedChange = { isAnonymous = it },
        )
      }
      // 清除已保存密码（编辑）
      if (isEditing) {
        PaperFormDivider()
        PaperFormRow {
          PaperCheckRow(
            label = stringResource(R.string.ui_clear_saved_password),
            checked = clearPassword,
            enabled = !isAnonymous,
            onCheckedChange = { clear ->
              clearPassword = clear
              if (clear) password = ""
            },
          )
        }
      }
      // 自动连接
      PaperFormDivider()
      PaperFormRow {
        Row(
          modifier = Modifier.fillMaxWidth().toggleable(
            value = autoConnect,
            role = Role.Switch,
            onValueChange = { autoConnect = it },
          ),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(Modifier.weight(1f)) {
            Text(
              text = stringResource(R.string.ui_auto_connect),
              fontSize = 14.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = LocalPaperColors.current.ink,
            )
            Text(
              text = stringResource(R.string.network_auto_connect_hint),
              fontSize = 11.5.sp,
              color = LocalPaperColors.current.inkSecondary,
              modifier = Modifier.padding(top = 2.5.dp),
            )
          }
          PaperSwitch(checked = autoConnect)
        }
      }
    }

    // 测试连接 / 保存
    // Same width and body height for both keys; the solid key's 3dp step is its shadow.
    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      PaperRaisedButton(
        text = stringResource(R.string.network_test_connection),
        onClick = { runTest() },
        enabled = canSave && !isTesting,
        height = 50.dp,
        modifier = Modifier.weight(1f),
      )
      PaperSolidButton(
        text = stringResource(R.string.ui_save),
        onClick = save,
        enabled = canSave,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

/**
 * The centered paper modal shell (mockup `.modal` + `.sheet`), shared shape with the sort modal:
 * warm dim scrim, card-paper sheet capped at 620px that scrolls, title + close key pinned above.
 */
@Composable
private fun PaperConnectionModal(
  onDismiss: () -> Unit,
  title: String,
  modifier: Modifier = Modifier,
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
    val progress by androidx.compose.animation.core.animateFloatAsState(
      targetValue = if (shown) 1f else 0f,
      animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 380f),
      label = "paperConnectionModalPop",
    )
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(Color(0xFF342A18).copy(alpha = 0.45f * progress))
          .displayCutoutPadding()
          .clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null,
          ) { onDismiss },
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier =
          modifier
            .imePadding()
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
            .paperRaise(shape, colors, elevation = 18.dp)
            .border(1.dp, colors.cardLine, shape)
            .clickable(
              interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
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
            contentDescription = stringResource(R.string.generic_close),
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
      }
    }
  }
}

/** Mockup `.mlab`: 12px bold letter-spaced tertiary label above each form group. */
@Composable
private fun PaperModalSectionLabel(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    modifier = modifier,
    fontSize = 12.sp,
    letterSpacing = 2.5.sp,
    fontWeight = FontWeight.Bold,
    color = LocalPaperColors.current.inkTertiary,
  )
}

/** Mockup `.opt` pill inside modals: inset paper chip; selected gets the vermilion ring. */
@Composable
private fun PaperProtocolChip(
  label: String,
  selected: Boolean,
  onSelect: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalPaperColors.current
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
          Modifier.paperInset(shape, colors)
        },
      )
      .selectable(
        selected = selected,
        role = Role.RadioButton,
        onClick = onSelect,
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

/** One `.frow`: 13/15 vertical padding; the caller separates rows with [PaperFormDivider]. */
@Composable
private fun PaperFormRow(content: @Composable () -> Unit) {
  Column(Modifier.fillMaxWidth().padding(top = 13.dp, bottom = 15.dp)) {
    content()
  }
}

@Composable
private fun PaperFormDivider() {
  Box(Modifier.fillMaxWidth().height(1.dp).background(LocalPaperColors.current.line))
}

/** `.frow label`: 12px tertiary bold, letter-spaced. */
@Composable
private fun PaperFormLabel(text: String) {
  Text(
    text = text,
    fontSize = 12.sp,
    letterSpacing = 2.sp,
    fontWeight = FontWeight.Bold,
    color = LocalPaperColors.current.inkTertiary,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = Modifier.padding(bottom = 9.dp),
  )
}

/** Small supporting line under a row (SMB hints, invalid port, …). */
@Composable
private fun PaperFormHint(text: String, isError: Boolean = false) {
  val colors = LocalPaperColors.current
  Text(
    text = text,
    fontSize = 11.sp,
    color = if (isError) colors.red else colors.inkSecondary,
    modifier = Modifier.padding(top = 6.dp),
  )
}

/**
 * Mockup `.slot`: 44dp recessed paper groove with a borderless ink input and tertiary
 * placeholder; [trailing] hosts e.g. the password visibility key.
 */
@Composable
private fun PaperSlot(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "",
  enabled: Boolean = true,
  isError: Boolean = false,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  trailing: (@Composable RowScope.() -> Unit)? = null,
) {
  val colors = LocalPaperColors.current
  val shape = RoundedCornerShape(11.dp)
  Row(
    modifier
      .fillMaxWidth()
      .height(44.dp)
      .paperInset(shape, colors)
      .then(if (isError) Modifier.border(1.dp, colors.red, shape) else Modifier)
      .padding(horizontal = 13.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(Modifier.weight(1f)) {
      BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle =
          LocalTextStyle.current.copy(
            fontSize = 14.sp,
            color = if (enabled) colors.ink else colors.ink.copy(alpha = 0.38f),
          ),
        cursorBrush = SolidColor(colors.red),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = Modifier.fillMaxWidth(),
      )
      if (value.isEmpty() && placeholder.isNotEmpty()) {
        Text(
          text = placeholder,
          fontSize = 14.sp,
          color = colors.inkTertiary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.align(Alignment.CenterStart),
        )
      }
    }
    trailing?.invoke(this)
  }
}

/** Mockup `.chk`: square recessed paper box + label, filled vermilion when on. */
@Composable
private fun PaperCheckRow(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  val colors = LocalPaperColors.current
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .toggleable(
          value = checked,
          enabled = enabled,
          role = Role.Checkbox,
          onValueChange = onCheckedChange,
        )
        .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(9.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val boxShape = RoundedCornerShape(7.dp)
    Box(
      Modifier
        .size(21.dp)
        .graphicsLayer { if (!enabled) alpha = 0.38f }
        .then(
          if (checked) {
            Modifier
              .background(Brush.verticalGradient(listOf(colors.redHighlight, colors.red)), boxShape)
              .border(0.5.dp, colors.redDark.copy(alpha = 0.4f), boxShape)
          } else {
            Modifier.paperInset(boxShape, colors)
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
    Text(
      text = label,
      fontSize = 13.sp,
      color = when {
        !enabled -> colors.ink.copy(alpha = 0.38f)
        checked -> colors.ink
        else -> colors.inkSecondary
      },
    )
  }
}

/** Mockup `.sw`: 48×29 recessed track with a paper knob; vermilion gradient when on. */
@Composable
private fun PaperSwitch(checked: Boolean, modifier: Modifier = Modifier) {
  val colors = LocalPaperColors.current
  val knobShift by androidx.compose.animation.core.animateDpAsState(
    targetValue = if (checked) 21.5.dp else 2.5.dp,
    animationSpec = androidx.compose.animation.core.spring(stiffness = 1200f),
    label = "paper_switch_knob",
  )
  Box(
    modifier
      .size(width = 48.dp, height = 29.dp)
      .then(
        if (checked) {
          Modifier
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.verticalGradient(listOf(colors.redHighlight, colors.red)))
        } else {
          Modifier.paperInset(RoundedCornerShape(15.dp), colors)
        },
      ),
    contentAlignment = Alignment.CenterStart,
  ) {
    Box(
      Modifier
        .padding(start = knobShift)
        .size(24.dp)
        .shadow(elevation = 2.dp, shape = androidx.compose.foundation.shape.CircleShape)
        .background(colors.card, androidx.compose.foundation.shape.CircleShape),
    )
  }
}

private const val MIN_PORT = 1
private const val MAX_PORT = 65_535
private const val MAX_PORT_DIGITS = 5
