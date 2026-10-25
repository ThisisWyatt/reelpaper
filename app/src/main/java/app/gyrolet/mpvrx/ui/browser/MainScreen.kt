/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import app.gyrolet.mpvrx.ui.utils.NavigationPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.abs
import kotlin.math.roundToInt
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.ui.theme.paper.PaperDock
import app.gyrolet.mpvrx.preferences.AppearancePreferences
import app.gyrolet.mpvrx.preferences.PlayerPreferences
import app.gyrolet.mpvrx.preferences.preference.collectAsState
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.gyrolet.mpvrx.presentation.Screen
import app.gyrolet.mpvrx.presentation.components.ProvideLiquidGlassBackdrop
import app.gyrolet.mpvrx.ui.utils.LocalBackStack
import app.gyrolet.mpvrx.ui.utils.navigateTo
import app.gyrolet.mpvrx.ui.browser.folderlist.FolderListScreen
import app.gyrolet.mpvrx.ui.browser.music.MusicLibraryContent
import app.gyrolet.mpvrx.ui.browser.music.MusicLibraryViewModel
import app.gyrolet.mpvrx.ui.browser.music.MusicTab
import app.gyrolet.mpvrx.ui.browser.networkstreaming.NetworkStreamingScreen
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.ui.icons.PaperIcons
import app.gyrolet.mpvrx.ui.player.controls.components.rememberTvInitialFocusRequester
import app.gyrolet.mpvrx.ui.player.controls.components.tvFocusHighlight
import app.gyrolet.mpvrx.ui.player.controls.components.tvInitialFocus
import app.gyrolet.mpvrx.ui.player.NavigationAnimStyle
import app.gyrolet.mpvrx.ui.utils.navigationDurationMillis
import app.gyrolet.mpvrx.ui.utils.navigationTabAnimationSpec
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import app.gyrolet.mpvrx.ui.theme.AppMotion
import app.gyrolet.mpvrx.ui.theme.LocalAppBackgroundBackdrop
import app.gyrolet.mpvrx.ui.theme.wallpaperAwareBackgroundColor
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import app.gyrolet.mpvrx.ui.liquidglass.drawCapsuleTint
import app.gyrolet.mpvrx.ui.liquidglass.liquidGlassEffects
import app.gyrolet.mpvrx.ui.liquidglass.rememberLiquidGlassSettings
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
object MainScreen : Screen {
  internal enum class MainTab {
    HOME,
    LIBRARY,
    NETWORK,
    PROFILE,
  }

  /**
   * Update selection state and navigation bar visibility
   * This method should be called whenever selection changes
   */
  fun updateSelectionState(
    isInSelectionMode: Boolean,
    isOnlyVideosSelected: Boolean,
    selectionManager: Any?,
  ) {
    NavigationBarState.updateSelectionState(
      inSelectionMode = isInSelectionMode,
      onlyVideos = isOnlyVideosSelected,
    )
  }

  /**
   * Update permission state to control FAB visibility
   */
  fun updatePermissionState(isDenied: Boolean) {
    NavigationBarState.updatePermissionState(isDenied)
  }

  /**
   * Get current permission denied state
   */
  fun getPermissionDeniedState(): Boolean = NavigationBarState.isPermissionDenied

  /**
   * Update bottom navigation bar visibility based on floating bottom bar state
   */
  fun updateBottomBarVisibility(shouldShow: Boolean) {
    NavigationBarState.updateBottomBarVisibility(shouldShow)
  }

  @SuppressLint("ComposableNaming")
  @Composable
  override fun Content() {
    val backStack = LocalBackStack.current
    val appearancePreferences = koinInject<AppearancePreferences>()
    val playerPreferences = koinInject<PlayerPreferences>()
    val navStyle by playerPreferences.appNavStyle.collectAsState()
    val animSpeed by playerPreferences.animationSpeed.collectAsState()
    val duration = navigationDurationMillis(animSpeed)
    val reduceMotion = AppMotion.shouldReduceMotion()
    var persistentSelectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    val showLibraryTab by appearancePreferences.showLibraryTab.collectAsState()
    val showProfileTab by appearancePreferences.showProfileTab.collectAsState()
    val showNetworkTab by appearancePreferences.showNetworkTab.collectAsState()
    val liquidGlassEnabled by appearancePreferences.liquidGlassEnabled.collectAsState()
    val progressiveNavigationBlur by appearancePreferences.progressiveNavigationBlur.collectAsState()
    val appBackgroundBackdrop = LocalAppBackgroundBackdrop.current
    val backdropBackgroundColor = MaterialTheme.colorScheme.background
    val liquidLayerBackdrop = rememberLayerBackdrop {
      // Previews or other hosts without a background capture still need an opaque source.
      if (appBackgroundBackdrop == null) drawRect(backdropBackgroundColor)
      drawContent()
    }
    // Transparent pages contain text without the wallpaper behind it. Composite the real
    // background before blur/refraction so the original sharp text cannot show through.
    val liquidNavigationBackdrop =
      if (appBackgroundBackdrop != null) {
        rememberCombinedBackdrop(appBackgroundBackdrop, liquidLayerBackdrop)
      } else {
        liquidLayerBackdrop
      }
    val hideNavigationBar = NavigationBarState.shouldHideNavigationBar
    val isPermissionDenied = NavigationBarState.isPermissionDenied
    val isDualPaneFolderSelected = NavigationBarState.isDualPaneFolderSelected
    val isMiniPlayerVisible = NavigationBarState.isMiniPlayerVisible

    val bottomNavigationOrder by appearancePreferences.bottomNavigationOrder.collectAsState()
    val visibleTabs =
      remember(showLibraryTab, showProfileTab, showNetworkTab, bottomNavigationOrder) {
        mainNavigationTabs(
          showLibrary = showLibraryTab,
          showProfile = showProfileTab,
          showNetwork = showNetworkTab,
          order = bottomNavigationOrder,
        )
      }
    val navigationTabs = visibleTabs.takeIf { it.size > 1 }.orEmpty()
    val isTelevision = app.gyrolet.mpvrx.utils.device.DeviceFormFactor.isTelevision(
      androidx.compose.ui.platform.LocalContext.current,
    )
    val showTvRail = isTelevision && visibleTabs.isNotEmpty() && !isPermissionDenied
    val tvRailFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    var isTvRailFocused by remember { mutableStateOf(false) }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    // Track whether the floating pill nav bar is on screen so the mini player can
    // sit at the very bottom when navigating to screens without it.
    DisposableEffect(Unit) {
      onDispose {
        NavigationBarState.isNavBarVisible = false
        NavigationBarState.isDualPaneLayout = false
      }
    }
    SideEffect {
      NavigationBarState.isNavBarVisible =
        !isTelevision &&
          backStack.lastOrNull() == MainScreen && !hideNavigationBar && navigationTabs.isNotEmpty() && !isPermissionDenied
      NavigationBarState.navigationBarBottomSpacing = NavigationBarState.floatingBarSpacing
    }

    val coroutineScope = rememberCoroutineScope()

    val initialPageIndex =
      remember(visibleTabs) {
        visibleTabs.indexOf(persistentSelectedTab).coerceAtLeast(0)
      }

    val pagerState =
      rememberPagerState(
        initialPage = initialPageIndex,
        pageCount = { visibleTabs.size },
      )
    var tabNavigationJob by remember { mutableStateOf<Job?>(null) }
    val tabContentAlpha = remember { Animatable(1f) }
    var isDirectTabJumpInProgress by remember { mutableStateOf(false) }

    // Adjacent tabs are pre-composed for instant swipes, but not during startup: composing Music
    // beside Home doubled first-frame work and started its library scan and cover decoding while
    // Home was being scrolled. Pre-compose once the lists have been idle for a moment.
    var adjacentTabsWarm by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
      if (adjacentTabsWarm) return@LaunchedEffect
      delay(ADJACENT_TAB_WARMUP_DELAY_MS)
      while (true) {
        val idleFor = android.os.SystemClock.uptimeMillis() - NavigationBarState.lastContentScrollUptimeMs
        if (idleFor >= ADJACENT_TAB_IDLE_MS) break
        delay(ADJACENT_TAB_IDLE_MS - idleFor)
      }
      adjacentTabsWarm = true
    }

    LaunchedEffect(pagerState, visibleTabs) {
      tabNavigationJob?.cancelAndJoin()
      tabNavigationJob = null
      if (visibleTabs.isEmpty()) {
        persistentSelectedTab = MainTab.HOME
        return@LaunchedEffect
      }

      val restorePage = visibleTabs.indexOf(persistentSelectedTab).takeIf { it >= 0 } ?: 0
      val isRestorePageSettled =
        pagerState.settledPage == restorePage &&
          pagerState.currentPage == restorePage &&
          pagerState.currentPageOffsetFraction == 0f
      if (!isRestorePageSettled) {
        pagerState.scrollToPage(restorePage)
      }

      snapshotFlow { pagerState.settledPage }
        .collect { page ->
          visibleTabs.getOrNull(page)?.let { settledTab ->
            persistentSelectedTab = settledTab
            if (settledTab != MainTab.HOME) {
              NavigationBarState.isDualPaneFolderSelected = false
            }
          }
        }
    }

    val targetPage = pagerState.targetPage.coerceIn(0, (visibleTabs.size - 1).coerceAtLeast(0))
    val selectedTab = visibleTabs.getOrNull(targetPage) ?: visibleTabs.firstOrNull() ?: MainTab.HOME

    val onTabSelected: (MainScreen.MainTab) -> Unit = { tab ->
      val targetIndex = visibleTabs.indexOf(tab)
      val isAlreadySettled =
        targetIndex >= 0 &&
          pagerState.settledPage == targetIndex &&
          !pagerState.isScrollInProgress &&
          pagerState.currentPageOffsetFraction == 0f
      if (targetIndex >= 0 && !isAlreadySettled) {
        // Bottom-nav taps are destination changes, not a request to visibly traverse every
        // intermediate tab. Keep the native pager animation for adjacent destinations, but
        // fade through long jumps so heavyweight Music/Network pages are never animated across.
        val previousJob = tabNavigationJob
        previousJob?.cancel()
        tabNavigationJob =
          coroutineScope.launch {
            previousJob?.join()

            val currentIndex = pagerState.currentPage.coerceIn(0, (visibleTabs.size - 1).coerceAtLeast(0))
            val isLongJump = abs(targetIndex - currentIndex) > 1

            tabContentAlpha.stop()
            tabContentAlpha.snapTo(1f)

            if (navStyle == NavigationAnimStyle.None || reduceMotion) {
              isDirectTabJumpInProgress = true
              try {
                pagerState.scrollToPage(targetIndex)
              } finally {
                tabContentAlpha.snapTo(1f)
                isDirectTabJumpInProgress = false
              }
            } else if (!isLongJump) {
              pagerState.animateScrollToPage(
                page = targetIndex,
                animationSpec = navigationTabAnimationSpec(navStyle, animSpeed),
              )
            } else {
              isDirectTabJumpInProgress = true
              try {
                // Fade the current destination out first. The actual pager jump happens while
                // content is hidden, then the destination fades in after it has been composed.
                tabContentAlpha.animateTo(
                  targetValue = 0f,
                  animationSpec =
                    tween(
                      durationMillis = navigationDurationMillis(animSpeed, 85),
                      easing = FastOutSlowInEasing,
                    ),
                )
                pagerState.scrollToPage(targetIndex)
                withFrameNanos { }
                tabContentAlpha.animateTo(
                  targetValue = 1f,
                  animationSpec =
                    tween(
                      durationMillis = navigationDurationMillis(animSpeed, 145),
                      easing = FastOutSlowInEasing,
                    ),
                )
              } finally {
                tabContentAlpha.snapTo(1f)
                isDirectTabJumpInProgress = false
              }
            }
          }
      }
    }

    val shouldReturnHome by remember(pagerState) {
      derivedStateOf {
        pagerState.settledPage != 0 || pagerState.currentPage != 0 || pagerState.isScrollInProgress
      }
    }
    // Register before page content: selection, search and nested navigation handle Back first.
    BackHandler(enabled = backStack.lastOrNull() == MainScreen && shouldReturnHome) {
      onTabSelected(MainTab.HOME)
    }
    // TV: Back from page content first moves focus to the side rail (NuvioTV behaviour); once the
    // rail owns focus, Back falls through to the return-home handler above and then exits.
    BackHandler(enabled = showTvRail && backStack.lastOrNull() == MainScreen && !isTvRailFocused) {
      runCatching { tvRailFocusRequester.requestFocus() }
    }
    val onTvTabSelected: (MainTab) -> Unit = { tab ->
      onTabSelected(tab)
      val pendingJob = tabNavigationJob
      coroutineScope.launch {
        pendingJob?.join()
        withFrameNanos { }
        focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Right)
      }
    }

    // Keep the app's established Haze path for the normal navigation bar. Liquid Glass uses the
    // separate Kyant layer backdrop declared with the screen state above.
    val navigationBackdrop = rememberHazeState()

    val mainNavBar = @Composable { modifier: Modifier ->
      PaperDock(
        visibleTabs = navigationTabs,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        modifier = modifier,
      )
    }

    val isDualPaneLayout = !isPermissionDenied &&
      isDualPaneFolderSelected && selectedTab == MainTab.HOME
    SideEffect {
      NavigationBarState.isDualPaneLayout = backStack.lastOrNull() == MainScreen && isDualPaneLayout
    }
    val contentBottomPadding =
      when {
        isTelevision -> 24.dp
        isDualPaneLayout -> NavigationBarState.bottomBarClearance
        else -> NavigationBarState.bottomOverlayClearance
      }
    val tvContentStartPadding = if (showTvRail) TvSideNavigationDefaults.CollapsedWidth else 0.dp
    // Scaffold with bottom navigation bar
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = wallpaperAwareBackgroundColor(),
    ) { paddingValues ->
      Box(modifier = Modifier.fillMaxSize()) {
        if (visibleTabs.isEmpty()) {
          Box(
            modifier =
              Modifier
                .fillMaxSize()
                .padding(start = tvContentStartPadding)
                .then(
                    when {
                      liquidGlassEnabled -> Modifier.layerBackdrop(liquidLayerBackdrop)
                      progressiveNavigationBlur -> Modifier.hazeSource(navigationBackdrop)
                      else -> Modifier
                    },
                  ),
          ) {
            CompositionLocalProvider(
              LocalNavigationBarHeight provides contentBottomPadding,
              LocalMainNavigationBar provides mainNavBar,
              LocalIsMainTabPage provides true,
            ) {
              FolderListScreen.Content()
            }
          }
        } else {
          CompositionLocalProvider(
            LocalNavigationBarHeight provides contentBottomPadding,
            LocalMainNavigationBar provides mainNavBar,
            LocalIsMainTabPage provides true,
          ) {
            NavigationPager(
              state = pagerState,
              modifier =
                Modifier
                  .fillMaxSize()
                  .padding(start = tvContentStartPadding)
                  .clipToBounds()
                  .graphicsLayer { alpha = tabContentAlpha.value }
                  .nestedScroll(NavigationBarState.navScrollConnection)
                  .then(
                    when {
                      liquidGlassEnabled -> Modifier.layerBackdrop(liquidLayerBackdrop)
                      progressiveNavigationBlur -> Modifier.hazeSource(navigationBackdrop)
                      else -> Modifier
                    },
                  ),
              key = { page -> visibleTabs[page].name },
              beyondViewportPageCount = if (adjacentTabsWarm) 1 else 0,
              userScrollEnabled = !isTelevision && !isPermissionDenied && !isDirectTabJumpInProgress,
            ) { page ->
              val tab = visibleTabs.getOrNull(page) ?: return@NavigationPager
              when (tab) {
                MainTab.HOME -> FolderListScreen.Content()
                MainTab.LIBRARY -> app.gyrolet.mpvrx.ui.browser.library.LibraryScreen.Content()
                MainTab.NETWORK -> NetworkStreamingScreen.Content()
                MainTab.PROFILE -> app.gyrolet.mpvrx.ui.browser.profile.ProfileScreen.Content()
              }
            }
          }
        }

        if (showTvRail) {
          TvSideNavigation(
            tabs = visibleTabs,
            selectedTab = selectedTab,
            onTabSelected = onTvTabSelected,
            onOpenSettings = { backStack.navigateTo(app.gyrolet.mpvrx.ui.preferences.PreferencesScreen) },
            selectedItemFocusRequester = tvRailFocusRequester,
            onRailFocusChanged = { isTvRailFocused = it },
            modifier = Modifier.align(Alignment.CenterStart),
          )
        }

        // Animated bottom navigation bar with slide animations
        ProvideLiquidGlassBackdrop(
          backdrop = liquidLayerBackdrop,
          enabled = liquidGlassEnabled,
        ) {
          AnimatedVisibility(
            visible = !isTelevision && !hideNavigationBar && navigationTabs.isNotEmpty() && !isPermissionDenied,
            enter = if (navStyle == NavigationAnimStyle.None) EnterTransition.None else
              slideInVertically(
                animationSpec = tween(duration, easing = FastOutSlowInEasing),
                initialOffsetY = { fullHeight -> fullHeight / 2 },
              ) + fadeIn(tween((duration * 0.82f).roundToInt())),
            exit = if (navStyle == NavigationAnimStyle.None) ExitTransition.None else
              slideOutVertically(
                animationSpec = tween((duration * 0.78f).roundToInt(), easing = FastOutSlowInEasing),
                targetOffsetY = { fullHeight -> fullHeight / 2 },
              ) + fadeOut(tween((duration * 0.62f).roundToInt())),
            modifier =
              Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart),
          ) {
            Box(
              Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                  WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                )
                .padding(bottom = NavigationBarState.floatingBarSpacing),
            ) {
              PaperDock(
                visibleTabs = navigationTabs,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.align(Alignment.Center),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ProgressiveNavigationBackdrop(
  state: HazeState,
  modifier: Modifier = Modifier,
) {
  val surfaceColor = MaterialTheme.colorScheme.surface
  BoxWithConstraints(modifier = modifier) {
    val heightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
    val blurStyle = remember(surfaceColor, heightPx) {
      HazeBlurStyle {
        blurRadius(28.dp)
        noiseFactor(0f)
        backgroundColor(surfaceColor)
        colorEffects(listOf(HazeColorEffect.tint(surfaceColor.copy(alpha = 0.16f))))
        fallbackColorEffect(HazeColorEffect.tint(surfaceColor.copy(alpha = 0.85f)))
        progressive(HazeProgressive.verticalGradient(endY = heightPx * 0.75f))
        mask(
          Brush.verticalGradient(
            0f to Color.Transparent,
            0.5f to Color.Black,
            1f to Color.Black,
          ),
        )
      }
    }
    Box(Modifier.fillMaxSize().hazeBlur(input = HazeInput.Sources(state), style = blurStyle))
  }
}

@Composable
internal fun mainNavigationLabel(tab: MainScreen.MainTab): String =
  stringResource(when (tab) {
    MainScreen.MainTab.HOME -> R.string.ui_home
    MainScreen.MainTab.LIBRARY -> R.string.ui_library
    MainScreen.MainTab.NETWORK -> R.string.ui_network
    MainScreen.MainTab.PROFILE -> R.string.ui_profile
  })

@Composable
internal fun MainTabIcon(
  tab: MainScreen.MainTab,
  tint: Color,
  contentDescription: String?,
  iconSize: androidx.compose.ui.unit.Dp = MainNavigationIconSize,
) {
  if (tab == MainScreen.MainTab.PROFILE) {
    app.gyrolet.mpvrx.ui.browser.profile.ProfileAvatar(
      size = iconSize,
      tint = tint,
      contentDescription = contentDescription,
    )
    return
  }
  // Mockup dock glyphs are thin ink strokes (#i-home / #i-bookmark / #i-globe), not the
  // filled Material Symbols set, which reads too heavy on the paper dock.
  val icon = when (tab) {
    MainScreen.MainTab.HOME -> PaperIcons.Home
    MainScreen.MainTab.LIBRARY -> PaperIcons.Bookmark
    MainScreen.MainTab.NETWORK -> PaperIcons.Globe
    MainScreen.MainTab.PROFILE -> PaperIcons.User
  }
  Icon(
    icon,
    contentDescription = contentDescription,
    tint = tint,
    modifier = Modifier.size(iconSize),
  )
}

private val MainNavigationIconSize = 28.dp
private val NavigationBarEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
private const val ADJACENT_TAB_WARMUP_DELAY_MS = 2_500L
private const val ADJACENT_TAB_IDLE_MS = 1_200L

/**
 * Bottom-navigation tab order. Home is the permanent root so Back never exits directly from another
 * tab. The Profile tab absorbs Recents, Playlists and Snapshots, which stay reachable from inside it.
 */
internal fun mainNavigationTabs(
  showLibrary: Boolean,
  showProfile: Boolean,
  showNetwork: Boolean,
  order: List<String> = emptyList(),
): List<MainScreen.MainTab> {
  val available = buildList {
    add(MainScreen.MainTab.HOME)
    if (showLibrary) add(MainScreen.MainTab.LIBRARY)
    if (showNetwork) add(MainScreen.MainTab.NETWORK)
    if (showProfile) add(MainScreen.MainTab.PROFILE)
  }
  val byName = available.associateBy { it.name }
  return listOf(MainScreen.MainTab.HOME) +
    (order.mapNotNull(byName::get) + available)
      .distinct()
      .filterNot { it == MainScreen.MainTab.HOME }
}

// Content clearance above the visible app bars, excluding system insets.
val LocalNavigationBarHeight = compositionLocalOf { 0.dp }

// True for a screen hosted as a bottom-navigation page; false when the same screen is pushed on the back stack.
val LocalIsMainTabPage = compositionLocalOf { false }

// CompositionLocal for main navigation bar
val LocalMainNavigationBar =
  compositionLocalOf<@Composable (Modifier) -> Unit> {
    { }
  }
