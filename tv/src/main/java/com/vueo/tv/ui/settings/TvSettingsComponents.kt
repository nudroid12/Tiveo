package com.vueo.tv.settings

import androidx.compose.ui.text.input.VisualTransformation
import com.vueo.tv.ui.TvDialogButton
import com.vueo.tv.ui.TvThemedDialog
import com.vueo.tv.ui.motion.LocalTvMotionFinish
import com.vueo.tv.ui.tvDialogInputColors

import com.vueo.tv.ui.TvModalDialog
import com.vueo.tv.ui.theme.TvTabMarkerHost
import com.vueo.tv.ui.theme.tvTabMarker

import android.view.KeyEvent
import com.vueo.shared.core.diagnostics.PerformanceDiagnostics
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vueo.tv.ui.TvBottomNotice
import com.vueo.tv.ui.TvDesign
import com.vueo.tv.home.MODERN_HOME_CONTENT_START_PADDING
import com.vueo.tv.home.TiveoPageContentFocus
import com.vueo.tv.home.TiveoTopBar
import com.vueo.tv.home.TiveoTopBarState
import com.vueo.tv.home.TiveoTopBarHeight
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal data class TvSettingsEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val value: String = "",
    val enabled: Boolean = true,
    val onActivate: (() -> Unit)? = null,
    val onPrevious: (() -> Unit)? = null,
    val onNext: (() -> Unit)? = null,
    val section: String? = null,
    val icon: ImageVector? = null,
    val onRightAction: (() -> Unit)? = null,
    val badge: String? = null,
    val detail: String? = null,
    val accented: Boolean = false,
    val rightActionLabel: String? = null,
    val choices: List<String> = emptyList(),
    val selectedChoiceIndex: Int = -1,
    val switchChecked: Boolean? = null,
    val catalogControls: Boolean = false,
    val providerStatus: String? = null,
    val providerResponse: String? = null,
    val providerFailed: Boolean = false,
    val providerPanelStart: Boolean = false,
    val providerPanelEnd: Boolean = false,
    val supportingContent: (@Composable () -> Unit)? = null,
    val operationStatus: String? = null,
    val iconDrawableRes: Int? = null,
    val showDivider: Boolean = true,
    val identityCard: Boolean = false,
    val actionCard: Boolean = false,
    val formControl: (@Composable (Modifier, () -> Unit, Boolean) -> Unit)? = null,
)

internal data class TvSettingsNavItem(
    val id: String,
    val title: String,
    val section: String? = null,
)

internal data class TvSettingsMetric(
    val value: String,
    val label: String,
)

internal data class TvSettingsEmbeddedHost(
    val panelKey: String,
    val requesterFor: (String) -> FocusRequester,
    val onFocusableRowsChanged: (List<String>, String?) -> Unit,
    val restorePanelFocus: () -> Boolean,
    val requestDeferredPanelRestore: () -> Unit,
    val onLeftToCategory: () -> Unit,
    val onRowFocused: (String) -> Unit,
)

internal val LocalTvSettingsEmbeddedHost = staticCompositionLocalOf<TvSettingsEmbeddedHost?> { null }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TvSettingsMasterDetailShell(
    categories: List<TvSettingsNavItem>,
    active: Boolean,
    selectedCategoryId: String,
    panelKey: String,
    panelAutoFocusToken: Int,
    panelHasBack: Boolean,
    pinDialogOpen: Boolean,
    bottomNotice: String?,
    panelUsesSharedRows: Boolean,
    onCategoryOpened: (String) -> Unit,
    onPanelBack: () -> Unit,
    topBar: TiveoTopBarState,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (categories.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val categoryTextStyle = LocalTextStyle.current.copy(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
    )
    val arrowTextStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
    // One compact width, sized for the longest label (Content Manager in this menu).
    // Measure the focused weight too so labels never truncate when focus moves.
    val categoryColumnWidth = remember(categories, textMeasurer, density, categoryTextStyle, arrowTextStyle) {
        val labelWidth = categories.maxOf { category ->
            textMeasurer.measure(category.title, style = categoryTextStyle, maxLines = 1).size.width
        }
        val arrowWidth = textMeasurer.measure("›", style = arrowTextStyle, maxLines = 1).size.width
        with(density) { (labelWidth + arrowWidth).toDp() } + 46.dp
    }
    val contentStartPadding = MODERN_HOME_CONTENT_START_PADDING
    val categoryListState = rememberLazyListState()
    val categoryRequesters = remember { mutableMapOf<String, FocusRequester>() }
    categories.forEach { category -> categoryRequesters.getOrPut(category.id) { FocusRequester() } }
    val panelStateHolder = rememberSaveableStateHolder()
    val panelGroupRequester = remember { FocusRequester() }
    val panelRequesters = remember { mutableMapOf<String, MutableMap<String, FocusRequester>>() }
    val panelFirstRowIds = remember { mutableMapOf<String, String>() }
    val panelLastFocusedIds = remember { mutableMapOf<String, String>() }
    val panelFocusableRowIds = remember { mutableMapOf<String, List<String>>() }
    var lastPane by remember { mutableStateOf("category") }
    // Back's exit step belongs to user intent, not transient focus callbacks.
    var backExitPending by remember { mutableStateOf(false) }
    var panelRestoreJob by remember { mutableStateOf<Job?>(null) }
    val shellScope = rememberCoroutineScope()
    val currentPanelKey by rememberUpdatedState(panelKey)
    val panelFocusReady = active && !pinDialogOpen && LocalWindowInfo.current.isWindowFocused
    val currentPanelFocusReady by rememberUpdatedState(panelFocusReady)
    var appliedPanelFocusToken by remember { mutableIntStateOf(0) }
    val explicitPanelFocusPending = panelAutoFocusToken > appliedPanelFocusToken

    fun requesterForPanelRow(key: String, rowId: String): FocusRequester =
        panelRequesters.getOrPut(key) { mutableMapOf() }.getOrPut(rowId) { FocusRequester() }

    fun updatePanelFocusableRows(key: String, rowIds: List<String>, preferredFocusId: String?) {
        val previousRows = panelFocusableRowIds[key].orEmpty()
        panelFocusableRowIds[key] = rowIds

        if (rowIds.isEmpty()) {
            panelFirstRowIds.remove(key)
            return
        }

        panelFirstRowIds[key] = rowIds.first()
        val previousFocusedId = panelLastFocusedIds[key]
        if (previousFocusedId == null) {
            panelLastFocusedIds[key] = preferredFocusId?.takeIf { it in rowIds } ?: rowIds.first()
            return
        }

        if (previousFocusedId !in rowIds) {
            val previousIndex = previousRows.indexOf(previousFocusedId)
            val fallbackIndex = if (previousIndex >= 0) {
                previousIndex.coerceAtMost(rowIds.lastIndex)
            } else {
                0
            }
            panelLastFocusedIds[key] = preferredFocusId?.takeIf { it in rowIds } ?: rowIds[fallbackIndex]
        }
    }

    fun panelFocusTargetId(key: String): String? {
        val focusableRows = panelFocusableRowIds[key].orEmpty()
        if (focusableRows.isEmpty()) return null
        return panelLastFocusedIds[key]?.takeIf { it in focusableRows }
            ?: panelFirstRowIds[key]?.takeIf { it in focusableRows }
            ?: focusableRows.first()
    }

    fun focusGlobalNav() {
        panelRestoreJob?.cancel()
        topBar.settingsPanelTransition = false
        appliedPanelFocusToken = panelAutoFocusToken
        topBar.focus("Settings")
    }

    fun focusSelectedCategory(): Boolean {
        panelRestoreJob?.cancel()
        appliedPanelFocusToken = panelAutoFocusToken
        lastPane = "category"
        topBar.settingsPanelTransition = false
        val requester = categoryRequesters.getValue(selectedCategoryId)
        if (runCatching { requester.requestFocus() }.getOrDefault(false)) {
            topBar.settingsPanelTransition = false
            return true
        }
        val index = categories.indexOfFirst { it.id == selectedCategoryId }
        if (index < 0) {
            topBar.settingsPanelTransition = false
            return false
        }
        shellScope.launch {
            categoryListState.scrollToItem(index)
            withFrameNanos { }
            if (lastPane == "category" && !topBar.hasFocus && panelKey == currentPanelKey) {
                runCatching { requester.requestFocus() }
                topBar.settingsPanelTransition = false
            }
        }
        return true
    }

    fun focusPanel(): Boolean {
        if (!currentPanelFocusReady || panelKey != currentPanelKey) return false
        if (panelUsesSharedRows) {
            val target = panelFocusTargetId(panelKey) ?: return false
            val first = panelFirstRowIds[panelKey]
            val rowId = listOfNotNull(target, first).distinct().firstOrNull { id ->
                runCatching { requesterForPanelRow(panelKey, id).requestFocus() }.getOrDefault(false)
            } ?: return false
            panelLastFocusedIds[panelKey] = rowId
        } else if (!runCatching { panelGroupRequester.requestFocus() }.getOrDefault(false)) {
            return false
        }
        lastPane = "panel"
        appliedPanelFocusToken = panelAutoFocusToken
        topBar.settingsPanelTransition = false
        return true
    }

    fun requestDeferredPanelRestore(explicit: Boolean = false) {
        panelRestoreJob?.cancel()
        panelRestoreJob = shellScope.launch {
            // A panel replacement can temporarily leave the focused row detached.
            // Retry until the destination row is genuinely focusable. Do not steal
            // focus back after the user intentionally moved to categories/sidebar.
            withFrameNanos { }
            for (waitMs in listOf(0L, 32L, 64L, 120L)) {
                if (waitMs > 0) delay(waitMs)
                if (!currentPanelFocusReady || (!explicit && topBar.hasFocus) ||
                    lastPane != "panel" || panelKey != currentPanelKey
                ) return@launch
                if (focusPanel()) return@launch
            }
            // The saved row can also be off-screen or detached after a panel refresh.
            if (currentPanelFocusReady && !topBar.hasFocus && lastPane == "panel" && panelKey == currentPanelKey) {
                focusSelectedCategory()
            }
        }
    }

    BackHandler(enabled = active) {
        PerformanceDiagnostics.captureRuntimeEvent(
            "TV_TAB_BACK tab=Settings action=" + when {
                backExitPending || topBar.hasFocus -> "home"
                lastPane == "panel" && panelHasBack -> "panel_parent"
                lastPane == "panel" -> "category"
                else -> "open_navigation"
            }
        )
        when {
            backExitPending || topBar.hasFocus -> {
                panelRestoreJob?.cancel()
                onBack()
            }
            lastPane == "panel" && panelHasBack -> onPanelBack()
            lastPane == "panel" -> focusSelectedCategory()
            else -> {
                backExitPending = true
                focusGlobalNav()
            }
        }
    }

    LaunchedEffect(active) {
        if (!active) {
            panelRestoreJob?.cancel()
            return@LaunchedEffect
        }
        withFrameNanos { }
        if (!topBar.hasFocus && !topBar.settingsPanelTransition &&
            lastPane == "category" && !explicitPanelFocusPending
        ) {
            runCatching { categoryRequesters.getValue(selectedCategoryId).requestFocus() }
        }
    }

    LaunchedEffect(panelAutoFocusToken, panelFocusReady) {
        if (!panelFocusReady || !explicitPanelFocusPending) return@LaunchedEffect

        // Keep the handoff pending while the PIN dialog owns the window.
        lastPane = "panel"
        requestDeferredPanelRestore(explicit = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvDesign.Black),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.nativeKeyEvent.keyCode in setOf(
                            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
                            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
                            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER,
                            KeyEvent.KEYCODE_NUMPAD_ENTER,
                        )) {
                        backExitPending = false
                    }
                    false
                }
                .padding(
                    start = contentStartPadding,
                    end = contentStartPadding,
                    top = TiveoTopBarHeight + 10.dp,
                    bottom = 28.dp,
                )
                .padding(horizontal = 4.dp, vertical = 12.dp),
        ) {
            Column(
                modifier = Modifier
                    .width(categoryColumnWidth)
                    .fillMaxHeight(),
            ) {
                Text(
                    text = "Settings",
                    color = TvDesign.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 12.dp, bottom = 16.dp),
                )

                LazyColumn(
                    state = categoryListState,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    categories.forEachIndexed { index, category ->
                        item(key = category.id) {
                            TvSettingsCategoryRow(
                                category = category,
                                selected = category.id == selectedCategoryId,
                                requester = categoryRequesters.getValue(category.id),
                                focusBlocked = topBar.settingsPanelTransition || explicitPanelFocusPending,
                                onFocused = {
                                    if (active && !topBar.hasFocus && !topBar.settingsPanelTransition &&
                                        !explicitPanelFocusPending && lastPane == "panel"
                                    ) {
                                        requestDeferredPanelRestore()
                                    }
                                },
                                onLeft = ::focusGlobalNav,
                                onRight = {
                                    panelRestoreJob?.cancel()
                                    onCategoryOpened(category.id)
                                    true
                                },
                                onUp = {
                                    if (index == 0) {
                                        focusGlobalNav()
                                        true
                                    } else {
                                        runCatching {
                                            categoryRequesters.getValue(categories[index - 1].id).requestFocus()
                                        }.getOrDefault(false)
                                    }
                                },
                                onDown = {
                                    if (index >= categories.lastIndex) true else runCatching {
                                        categoryRequesters.getValue(categories[index + 1].id).requestFocus()
                                        true
                                    }.getOrDefault(false)
                                },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(18.dp))
            Box(
                Modifier.fillMaxHeight().width(1.dp)
                    .background(TvDesign.White.copy(alpha = .10f)),
            )
            Spacer(Modifier.width(24.dp))

            CompositionLocalProvider(
                LocalTvSettingsEmbeddedHost provides TvSettingsEmbeddedHost(
                    panelKey = panelKey,
                    requesterFor = { rowId -> requesterForPanelRow(panelKey, rowId) },
                    onFocusableRowsChanged = { rowIds, preferredFocusId ->
                        updatePanelFocusableRows(panelKey, rowIds, preferredFocusId)
                    },
                    restorePanelFocus = {
                        if (lastPane == "panel" && !explicitPanelFocusPending) focusPanel() else false
                    },
                    requestDeferredPanelRestore = { requestDeferredPanelRestore() },
                    onLeftToCategory = { focusSelectedCategory() },
                    onRowFocused = { rowId ->
                        if (active && !topBar.hasFocus && !explicitPanelFocusPending &&
                            !topBar.settingsPanelTransition
                        ) {
                            panelLastFocusedIds[panelKey] = rowId
                            lastPane = "panel"
                        }
                    },
                )
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .focusRequester(panelGroupRequester)
                        .focusGroup(),
                ) {
                    panelStateHolder.SaveableStateProvider(panelKey) {
                        content()
                    }
                }
            }
        }

        TiveoPageContentFocus(
            topBar = topBar,
            tab = "Settings",
            active = active,
            onReturnToContent = {
                backExitPending = false
                if (explicitPanelFocusPending) {
                    if (panelFocusReady) {
                        lastPane = "panel"
                        requestDeferredPanelRestore(explicit = true)
                    }
                } else if (lastPane != "panel" || !focusPanel()) {
                    focusSelectedCategory()
                }
            },
        )

        bottomNotice?.let { TvBottomNotice(it) }
    }
}

@Composable
private fun TvSettingsCategoryRow(
    category: TvSettingsNavItem,
    selected: Boolean,
    requester: FocusRequester,
    focusBlocked: Boolean,
    onFocused: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Boolean,
    onUp: () -> Boolean,
    onDown: () -> Boolean,
) {
    var focused by remember(category.id) { mutableStateOf(false) }
    val showFocus = focused && !focusBlocked
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .focusRequester(requester)
            .focusProperties { canFocus = !focusBlocked }
            .onFocusChanged { state ->
                focused = state.isFocused
                if (state.isFocused) onFocused()
            }
            .onPreviewKeyEvent { event ->
                val keyCode = event.nativeKeyEvent.keyCode
                when {
                    event.type == KeyEventType.KeyDown && keyCode == KeyEvent.KEYCODE_DPAD_LEFT -> {
                        onLeft()
                        true
                    }
                    event.type == KeyEventType.KeyDown && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> onRight()
                    event.type == KeyEventType.KeyDown && keyCode == KeyEvent.KEYCODE_DPAD_UP -> onUp()
                    event.type == KeyEventType.KeyDown && keyCode == KeyEvent.KEYCODE_DPAD_DOWN -> onDown()
                    event.isTvActivationKey() -> {
                        if (event.type == KeyEventType.KeyUp) onRight()
                        true
                    }
                    else -> false
                }
            }
            .background(
                when {
                    showFocus -> TvDesign.White
                    selected -> TvDesign.SurfaceRaised.copy(alpha = .55f)
                    else -> Color.Transparent
                },
                TvDesign.CardShape,
            )
            .focusable()
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = category.title,
            color = if (showFocus) TvDesign.Black else if (selected) TvDesign.White else TvDesign.Muted,
            fontSize = 16.sp,
            fontWeight = if (showFocus || selected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "›",
            color = if (showFocus) TvDesign.Black else if (selected) TvDesign.Muted else Color.Transparent,
            fontSize = 20.sp,
        )
    }
}

@Composable
internal fun TvSettingsProfilePanel(
    profileName: String,
    profileSubtitle: String,
    avatarDrawableRes: Int?,
    myListCount: Int?,
    watchedCount: Int?,
    dnaValue: String,
    tastePreview: String,
    onOpenDna: () -> Unit,
    onSwitchProfiles: () -> Unit,
) {
    val host = LocalTvSettingsEmbeddedHost.current ?: return
    val profileRequester = host.requesterFor("profile-card")
    val switchRequester = host.requesterFor("switch-profiles")
    LaunchedEffect(host.panelKey) {
        host.onFocusableRowsChanged(listOf("profile-card", "switch-profiles"), null)
        delay(24)
        host.restorePanelFocus()
    }
    var profileFocused by remember(profileName) { mutableStateOf(false) }
    var switchFocused by remember(profileName) { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Profile",
            color = TvDesign.White,
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Your Tiveo profile and local taste at a glance.",
            color = TvDesign.Muted,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            modifier = Modifier.padding(top = 5.dp, bottom = 16.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(profileRequester)
                    .onFocusChanged { state ->
                        profileFocused = state.isFocused
                        if (state.isFocused) host.onRowFocused("profile-card")
                    }
                    .onPreviewKeyEvent { event ->
                        val keyCode = event.nativeKeyEvent.keyCode
                        when {
                            event.type == KeyEventType.KeyDown && keyCode == KeyEvent.KEYCODE_DPAD_LEFT -> {
                                host.onLeftToCategory()
                                true
                            }
                            event.type == KeyEventType.KeyDown && keyCode == KeyEvent.KEYCODE_DPAD_UP -> true
                            event.type == KeyEventType.KeyDown && keyCode == KeyEvent.KEYCODE_DPAD_DOWN -> {
                                runCatching { switchRequester.requestFocus() }
                                true
                            }
                            event.type == KeyEventType.KeyDown && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> true
                            event.isTvActivationKey() -> {
                                if (event.type == KeyEventType.KeyUp) onOpenDna()
                                true
                            }
                            else -> false
                        }
                    }
                    .clip(RoundedCornerShape(15.dp))
                    .background(if (profileFocused) TvDesign.SurfaceRaised else TvDesign.Surface)
                    .border(
                        if (profileFocused) 1.5.dp else 0.dp,
                        if (profileFocused) TvDesign.White.copy(alpha = .88f) else TvDesign.White.copy(alpha = 0f),
                        RoundedCornerShape(15.dp),
                    )
                    .focusable()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(TvDesign.SurfaceRaised),
                    contentAlignment = Alignment.Center,
                ) {
                    if (avatarDrawableRes != null) {
                        Image(
                            painter = painterResource(avatarDrawableRes),
                            contentDescription = profileName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Text(
                            text = profileName.trim().firstOrNull()?.uppercase() ?: "V",
                            color = TvDesign.White,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = profileName,
                        color = TvDesign.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = profileSubtitle,
                        color = TvDesign.Muted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = "›",
                    color = if (profileFocused) TvDesign.White else TvDesign.Dim,
                    fontSize = 31.sp,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TvProfileStat(Modifier.weight(1f), "Watchlist", myListCount?.toString() ?: "—")
                TvProfileStat(Modifier.weight(1f), "Watched", watchedCount?.toString() ?: "—")
                TvProfileStat(Modifier.weight(1f), "DNA", dnaValue)
            }

            Text(
                text = tastePreview,
                color = TvDesign.Muted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(switchRequester)
                    .onFocusChanged { state ->
                        switchFocused = state.isFocused
                        if (state.isFocused) host.onRowFocused("switch-profiles")
                    }
                    .onPreviewKeyEvent { event ->
                        val keyCode = event.nativeKeyEvent.keyCode
                        when {
                            event.type == KeyEventType.KeyDown && keyCode == KeyEvent.KEYCODE_DPAD_LEFT -> {
                                host.onLeftToCategory()
                                true
                            }
                            event.type == KeyEventType.KeyDown && keyCode == KeyEvent.KEYCODE_DPAD_UP -> {
                                runCatching { profileRequester.requestFocus() }
                                true
                            }
                            event.type == KeyEventType.KeyDown && keyCode == KeyEvent.KEYCODE_DPAD_DOWN -> true
                            event.type == KeyEventType.KeyDown && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> true
                            event.isTvActivationKey() -> {
                                if (event.type == KeyEventType.KeyUp) onSwitchProfiles()
                                true
                            }
                            else -> false
                        }
                    }
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (switchFocused) TvDesign.White else TvDesign.Surface)
                    .border(
                        if (switchFocused) 1.5.dp else 1.dp,
                        if (switchFocused) TvDesign.White else TvDesign.White.copy(alpha = .06f),
                        RoundedCornerShape(14.dp),
                    )
                    .focusable()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "⇄",
                    color = if (switchFocused) TvDesign.Black else TvDesign.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    text = "Switch Profiles",
                    color = if (switchFocused) TvDesign.Black else TvDesign.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun TvProfileStat(
    modifier: Modifier,
    label: String,
    value: String,
) {
    Column(
        modifier = modifier
            .height(86.dp)
            .background(TvDesign.Surface, RoundedCornerShape(13.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(value, color = TvDesign.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text(label, color = TvDesign.Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TvSettingsMetricsRow(
    metrics: List<TvSettingsMetric>,
    compact: Boolean = false,
    inlineLabels: Boolean = false,
) {
    if (compact && !inlineLabels) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            metrics.forEach { metric ->
                Text("${metric.value} ${metric.label}", color = TvDesign.Muted, fontSize = 12.sp)
            }
        }
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TvDesign.Surface, RoundedCornerShape(16.dp))
            .border(1.dp, TvDesign.White.copy(alpha = .08f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = if (inlineLabels) 12.dp else 11.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        metrics.forEach { metric ->
            if (inlineLabels) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(metric.value, color = TvDesign.Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Text(metric.label, color = TvDesign.Muted, fontSize = 12.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            } else Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = metric.value,
                    color = TvDesign.Accent,
                    fontSize = if (compact) 17.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = metric.label,
                    color = TvDesign.Muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TvSettingsListScreen(
    title: String,
    subtitle: String,
    entries: List<TvSettingsEntry>,
    onNavigate: (String) -> Unit,
    onProfile: () -> Unit,
    onBack: () -> Unit,
    topLabel: String? = null,
    footer: String? = null,
    metrics: List<TvSettingsMetric> = emptyList(),
    preferredFocusId: String? = null,
    addonLayout: Boolean = false,
    contentManager: Boolean = false,
    centerHeader: Boolean = false,
) {
    val embeddedHost = LocalTvSettingsEmbeddedHost.current
    if (embeddedHost != null) {
        TvSettingsEmbeddedPanel(
            title = title,
            subtitle = subtitle,
            entries = entries,
            host = embeddedHost,
            topLabel = topLabel,
            footer = footer,
            metrics = metrics,
            preferredFocusId = preferredFocusId,
            addonLayout = addonLayout,
            contentManager = contentManager,
            centerHeader = centerHeader,
        )
        return
    }

    BackHandler(onBack = onBack)
    val accountHeader = title == "Account" && entries.any { it.id == "sync-now" }
    val accountHeaderIds = setOf("status", "message", "sync-all", "sync-now", "sign-out", "cancel-sign-out")

    val contentStartPadding = MODERN_HOME_CONTENT_START_PADDING
    val rowRequesters = remember { mutableMapOf<String, FocusRequester>() }
    entries.forEach { entry -> rowRequesters.getOrPut(entry.id) { FocusRequester() } }
    val focusableIds = entries.filter { it.enabled }.map { it.id }
    val firstFocusable = entries.firstOrNull { it.enabled }
    val lastFocusable = entries.lastOrNull { it.enabled }
    var lastFocusedId by remember { mutableStateOf(firstFocusable?.id.orEmpty()) }
    var previousFocusableIds by remember { mutableStateOf(focusableIds) }
    val topBar = remember { TiveoTopBarState() }

    LaunchedEffect(focusableIds, preferredFocusId, topBar.hasFocus) {
        if (topBar.hasFocus || focusableIds.isEmpty()) return@LaunchedEffect

        preferredFocusId?.takeIf { it in focusableIds }?.let { lastFocusedId = it }

        if (lastFocusedId !in focusableIds) {
            val previousIndex = previousFocusableIds.indexOf(lastFocusedId)
            val fallbackIndex = if (previousIndex >= 0) {
                previousIndex.coerceAtMost(focusableIds.lastIndex)
            } else {
                0
            }
            lastFocusedId = focusableIds[fallbackIndex]
        }
        previousFocusableIds = focusableIds

        delay(90)
        runCatching { rowRequesters.getValue(lastFocusedId).requestFocus() }
    }

    fun focusSettingsNav() {
        topBar.focus("Settings")
    }

    fun restoreContentFocus(): Boolean {
        val targetId = lastFocusedId.takeIf { it in focusableIds } ?: focusableIds.firstOrNull() ?: return false
        val requester = rowRequesters[targetId] ?: return false
        lastFocusedId = targetId
        return runCatching { requester.requestFocus(); true }.getOrDefault(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvDesign.Black),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = contentStartPadding,
                    end = contentStartPadding,
                    top = TiveoTopBarHeight + 10.dp,
                    bottom = 28.dp,
                ),
        ) {
            if (!topLabel.isNullOrBlank()) {
                Text(
                    text = topLabel.uppercase(),
                    color = TvDesign.Dim,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
                Spacer(Modifier.height(5.dp))
            }

            Text(
                text = title,
                modifier = if (centerHeader) Modifier.fillMaxWidth() else Modifier,
                textAlign = if (centerHeader) TextAlign.Center else TextAlign.Start,
                color = TvDesign.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    textAlign = if (centerHeader) TextAlign.Center else TextAlign.Start,
                    color = TvDesign.Muted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = (if (centerHeader) Modifier.fillMaxWidth() else Modifier)
                    .padding(top = 6.dp, bottom = 16.dp),
                )
            } else {
                Spacer(Modifier.height(12.dp))
            }

            if (metrics.isNotEmpty()) {
                TvSettingsMetricsRow(metrics = metrics, inlineLabels = contentManager)
                Spacer(Modifier.height(12.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .focusRestorer {
                        val targetId = lastFocusedId.takeIf { it in focusableIds } ?: focusableIds.firstOrNull()
                        targetId?.let { rowRequesters[it] } ?: FocusRequester.Default
                    }
                    .focusGroup(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                if (accountHeader) item(key = "account-header") {
                    TvAccountSummaryPanel(entries, { rowRequesters.getValue(it) },
                        { lastFocusedId = it }, ::focusSettingsNav)
                }
                if (addonLayout) item(key = "workspace-controls") {
                    TvAddonControls(entries, { rowRequesters.getValue(it) },
                        { lastFocusedId = it }, ::focusSettingsNav)
                }
                var previousSection: String? = null
                entries.filterNot { (accountHeader && it.id in accountHeaderIds) || (addonLayout && (it.isAddonControl() || it.isRepositoryAction())) }.forEachIndexed { index, entry ->
                    val section = entry.section?.takeIf { it.isNotBlank() }
                    if (section != null && section != previousSection) {
                        item(key = "section-$index-$section") {
                            Text(
                                text = section.uppercase(),
                                color = TvDesign.Dim,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp,
                                modifier = Modifier.padding(start = 8.dp, top = if (index == 0) 2.dp else 10.dp, bottom = 1.dp),
                            )
                        }
                        previousSection = section
                    }

                    item(key = entry.id) {
                        TvContentSettingsRow(
                            entry = entry,
                            contentLayout = addonLayout,
                            entries = entries,
                            requesterFor = { rowRequesters.getValue(it) },
                            onEntryFocused = { lastFocusedId = it },
                            requester = rowRequesters.getValue(entry.id),
                            first = entry.id == firstFocusable?.id,
                            last = entry.id == lastFocusable?.id,
                            onLeftToSidebar = ::focusSettingsNav,
                            onFocused = {
                                lastFocusedId = entry.id
                            },
                        )
                    }
                }
                if (!footer.isNullOrBlank()) {
                    item(key = "settings-footer") {
                        Text(
                            text = footer,
                            color = TvDesign.Dim,
                            fontSize = 9.sp,
                            lineHeight = 13.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }

        TiveoTopBar(
            state = topBar,
            selected = "Settings",
            canFocus = true,
            onNavigate = { tab -> if (tab == "Profile") onProfile() else onNavigate(tab) },
            onReturnToContent = { restoreContentFocus() },
            modifier = Modifier.align(Alignment.TopStart),
        )
    }
}

@Composable
private fun TvSettingsEmbeddedPanel(
    title: String,
    subtitle: String,
    entries: List<TvSettingsEntry>,
    host: TvSettingsEmbeddedHost,
    topLabel: String?,
    footer: String?,
    metrics: List<TvSettingsMetric>,
    preferredFocusId: String?,
    addonLayout: Boolean = false,
    contentManager: Boolean = false,
    centerHeader: Boolean = false,
) {
    val accountHeader = title == "Account" && entries.any { it.id == "sync-now" }
    val accountHeaderIds = setOf("status", "message", "sync-all", "sync-now", "sign-out", "cancel-sign-out")
    val focusableIds = entries.filter { it.enabled }.map { it.id }
    val firstFocusable = entries.firstOrNull { it.enabled }
    val lastFocusable = entries.lastOrNull { it.enabled }

    LaunchedEffect(host.panelKey, focusableIds, preferredFocusId) {
        host.onFocusableRowsChanged(focusableIds, preferredFocusId)
        if (focusableIds.isNotEmpty()) {
            withFrameNanos { }
            host.restorePanelFocus()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (!topLabel.isNullOrBlank()) {
            Text(
                text = topLabel.uppercase(),
                color = TvDesign.Dim,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
            Spacer(Modifier.height(5.dp))
        }

        Text(
            text = title,
            modifier = if (centerHeader) Modifier.fillMaxWidth() else Modifier,
            textAlign = if (centerHeader) TextAlign.Center else TextAlign.Start,
            color = TvDesign.White,
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold,
        )
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                textAlign = if (centerHeader) TextAlign.Center else TextAlign.Start,
                color = TvDesign.Muted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = (if (centerHeader) Modifier.fillMaxWidth() else Modifier)
                    .padding(top = 5.dp, bottom = 16.dp),
            )
        } else {
            Spacer(Modifier.height(12.dp))
        }

        if (metrics.isNotEmpty()) {
            TvSettingsMetricsRow(metrics = metrics, compact = !addonLayout, inlineLabels = contentManager)
            Spacer(Modifier.height(if (addonLayout) 12.dp else 6.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = if (addonLayout) PaddingValues(0.dp) else PaddingValues(start = 3.dp, end = 3.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            if (accountHeader) item(key = "account-header") {
                TvAccountSummaryPanel(entries, host)
            }
            if (addonLayout) item(key = "workspace-controls") {
                TvAddonControls(entries, host.requesterFor, host.onRowFocused, host.onLeftToCategory)
            }
            var previousSection: String? = null
            entries.filterNot { (accountHeader && it.id in accountHeaderIds) || (addonLayout && (it.isAddonControl() || it.isRepositoryAction())) }.forEachIndexed { index, entry ->
                val section = entry.section?.takeIf { it.isNotBlank() }
                if (section != null && section != previousSection) {
                    item(key = "embedded-section-$index-$section") {
                        Text(
                            text = section.uppercase(),
                            color = TvDesign.Dim,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                            modifier = Modifier.padding(start = 8.dp, top = if (index == 0) 2.dp else 10.dp, bottom = 1.dp),
                        )
                    }
                    previousSection = section
                }

                item(key = entry.id) {
                    TvContentSettingsRow(
                        entry = entry,
                        contentLayout = addonLayout,
                        entries = entries,
                        requesterFor = host.requesterFor,
                        onEntryFocused = host.onRowFocused,
                        requester = host.requesterFor(entry.id),
                        first = entry.id == firstFocusable?.id,
                        last = entry.id == lastFocusable?.id,
                        onLeftToSidebar = host.onLeftToCategory,
                        onFocused = { host.onRowFocused(entry.id) },
                    )
                }
            }

            if (!footer.isNullOrBlank()) {
                item(key = "embedded-settings-footer") {
                    Text(
                        text = footer,
                        color = TvDesign.Dim,
                        fontSize = 9.sp,
                        lineHeight = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun TvSettingsRow(
    entry: TvSettingsEntry,
    requester: FocusRequester,
    first: Boolean,
    last: Boolean,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
    onLeftToSidebar: () -> Unit,
    onFocused: () -> Unit,
) {
    if (entry.identityCard) {
        val logo = checkNotNull(entry.iconDrawableRes)
        var focused by remember(entry.id) { mutableStateOf(false) }
        Column(
            modifier = modifier.fillMaxWidth()
                .focusRequester(requester)
                .onFocusChanged {
                    focused = it.isFocused
                    if (it.isFocused) onFocused()
                }
                .onPreviewKeyEvent { event ->
                    when {
                        event.isTvActivationKey() -> {
                            if (event.type == KeyEventType.KeyUp) entry.onActivate?.invoke()
                            true
                        }
                        event.type != KeyEventType.KeyDown -> false
                        event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_LEFT -> {
                            onLeftToSidebar()
                            true
                        }
                        else -> when (event.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_RIGHT -> true
                            KeyEvent.KEYCODE_DPAD_UP -> first
                            KeyEvent.KEYCODE_DPAD_DOWN -> last
                            else -> false
                        }
                    }
                }
                .focusable(enabled = entry.enabled)
                .padding(top = 14.dp, bottom = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Image(
                painter = painterResource(logo),
                contentDescription = entry.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.width(156.dp).height(36.dp),
            )
            Text(
                text = entry.value,
                color = if (focused) TvDesign.Black else TvDesign.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(if (focused) TvDesign.White else TvDesign.SurfaceRaised, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        return
    }
    entry.formControl?.let { control ->
        Box(modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
            control(
                Modifier.widthIn(max = 460.dp).fillMaxWidth()
                    .focusRequester(requester)
                    .onFocusChanged { if (it.isFocused) onFocused() },
                onLeftToSidebar,
                first,
            )
        }
        return
    }
    if (entry.catalogControls) {
        TvCatalogControlsRow(entry, requester, first, last, modifier, onLeftToSidebar, onFocused)
        return
    }
    if (entry.supportingContent != null) {
        TvSubtitleFontSettingsCard(entry, requester, first, last, modifier, onLeftToSidebar, onFocused)
        return
    }
    var focused by remember(entry.id) { mutableStateOf(false) }
    val canAdjust = entry.onPrevious != null || entry.onNext != null
    val shape = if (entry.actionCard) RoundedCornerShape(50) else TvDesign.CardShape
    val divider = TvDesign.White.copy(alpha = .09f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (compact) 48.dp else 58.dp)
            .focusRequester(requester)
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onFocused()
            }
            .onPreviewKeyEvent { event ->
                if (!entry.enabled) return@onPreviewKeyEvent false
                val keyCode = event.nativeKeyEvent.keyCode
                when {
                    first &&
                        event.type == KeyEventType.KeyDown &&
                        keyCode == KeyEvent.KEYCODE_DPAD_UP -> true
                    last &&
                        event.type == KeyEventType.KeyDown &&
                        keyCode == KeyEvent.KEYCODE_DPAD_DOWN -> true
                    event.type == KeyEventType.KeyDown &&
                        keyCode == KeyEvent.KEYCODE_DPAD_LEFT &&
                        entry.onPrevious == null -> {
                        onLeftToSidebar()
                        true
                    }
                    event.type == KeyEventType.KeyDown &&
                        keyCode == KeyEvent.KEYCODE_DPAD_LEFT &&
                        entry.onPrevious != null -> {
                        entry.onPrevious.invoke()
                        true
                    }
                    event.type == KeyEventType.KeyDown &&
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT &&
                        entry.onNext != null -> {
                        entry.onNext.invoke()
                        true
                    }
                    event.type == KeyEventType.KeyDown &&
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT &&
                        entry.onRightAction != null -> {
                        entry.onRightAction.invoke()
                        true
                    }
                    event.type == KeyEventType.KeyDown &&
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> true
                    event.isTvActivationKey() -> {
                        if (event.type == KeyEventType.KeyUp) entry.onActivate?.invoke()
                        true
                    }
                    else -> false
                }
            }
            .background(
                when {
                    focused -> TvDesign.White
                    entry.actionCard -> TvDesign.Surface
                    else -> Color.Transparent
                },
                shape,
            )
            .border(
                1.dp,
                when {
                    focused -> Color.Transparent
                    entry.actionCard -> TvDesign.White.copy(alpha = .14f)
                    else -> Color.Transparent
                },
                shape,
            )
            .drawBehind {
                if (!focused && entry.showDivider) {
                    val y = size.height - .5.dp.toPx()
                    drawLine(divider, Offset(12.dp.toPx(), y), Offset(size.width - 12.dp.toPx(), y), 1.dp.toPx())
                }
            }
            .focusable(enabled = entry.enabled)
            .padding(horizontal = 16.dp, vertical = if (compact) 7.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (entry.iconDrawableRes != null) {
            Image(
                painter = painterResource(entry.iconDrawableRes),
                contentDescription = null,
                modifier = Modifier.size(if (compact) 32.dp else 44.dp),
            )
            Spacer(Modifier.width(14.dp))
        } else if (!entry.badge.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(if (compact) 32.dp else 44.dp)
                    .background(TvDesign.Accent.copy(alpha = .12f), RoundedCornerShape(13.dp))
                    .border(1.dp, TvDesign.Accent.copy(alpha = .28f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entry.badge.orEmpty(),
                    color = if (focused) TvDesign.Black else TvDesign.Accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(14.dp))
        } else {
            entry.icon?.let { icon ->
                Box(
                    modifier = Modifier
                        .size(if (compact) 32.dp else 44.dp)
                        .background(
                            color = if (entry.accented) {
                                TvDesign.Accent.copy(alpha = if (focused) .16f else .09f)
                            } else if (focused) {
                                TvDesign.Black.copy(alpha = .08f)
                            } else {
                                TvDesign.SurfaceRaised.copy(alpha = .62f)
                            },
                            shape = RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = when {
                            focused -> TvDesign.Black
                            !entry.enabled -> TvDesign.Dim
                            entry.accented -> TvDesign.Accent
                            else -> TvDesign.White.copy(alpha = .92f)
                        },
                        modifier = Modifier.size(if (compact) 19.dp else 23.dp)
                            .then(tvContentRefreshMotion(entry)),
                    )
                }
                Spacer(Modifier.width(14.dp))
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = entry.title,
                color = if (focused) TvDesign.Black else if (entry.enabled) TvDesign.White else TvDesign.Dim,
                fontSize = 16.sp,
                fontWeight = if (focused) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (entry.subtitle.isNotBlank()) {
                Text(
                    text = entry.subtitle,
                    color = if (focused) TvDesign.Black else if (entry.enabled) TvDesign.Muted else TvDesign.Dim.copy(alpha = .65f),
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            entry.supportingContent?.invoke()
            if (entry.choices.isNotEmpty()) {
                val selectedIndex = entry.selectedChoiceIndex.coerceIn(0, entry.choices.lastIndex)
                val firstVisible = (selectedIndex - 1)
                    .coerceAtLeast(0)
                    .coerceAtMost((entry.choices.size - 3).coerceAtLeast(0))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    entry.choices.drop(firstVisible).take(3).forEachIndexed { offset, choice ->
                        val choiceIndex = firstVisible + offset
                        val selected = choiceIndex == selectedIndex
                        Text(
                            text = choice,
                            color = if (focused) TvDesign.Black else if (selected) TvDesign.White else TvDesign.Muted,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .background(
                                    if (selected) TvDesign.Accent.copy(alpha = .28f)
                                    else TvDesign.White.copy(alpha = .045f),
                                    RoundedCornerShape(50),
                                )
                                .border(
                                    1.dp,
                                    if (selected) TvDesign.Accent.copy(alpha = .72f)
                                    else TvDesign.White.copy(alpha = .12f),
                                    RoundedCornerShape(50),
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                        )
                    }
                }
            }
            entry.detail?.takeIf { it.isNotBlank() }?.let { detail ->
                Text(
                    text = detail,
                    color = if (focused) TvDesign.Black else if (entry.enabled) TvDesign.Dim else TvDesign.Dim.copy(alpha = .52f),
                    fontSize = 9.5.sp,
                    lineHeight = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            entry.operationStatus?.takeIf { it.isNotBlank() }?.let { message ->
                Text(message, color = if (focused) TvDesign.Black else TvDesign.Accent, fontSize = 11.sp, lineHeight = 15.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            if (entry.switchChecked != null) {
                TvContentToggle(entry.switchChecked, focused = focused)
            } else if (entry.value.isNotBlank()) {
                Text(
                    text = buildString {
                        if (canAdjust && focused) append("‹  ")
                        append(entry.value)
                        if (canAdjust && focused) append("  ›")
                    },
                    color = when {
                        !entry.enabled -> TvDesign.Dim
                        focused -> TvDesign.Black
                        entry.accented -> TvDesign.Accent
                        else -> TvDesign.Muted
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .background(
                            color = if (entry.accented) {
                                TvDesign.Accent.copy(alpha = if (focused) .16f else .08f)
                            } else if (focused) {
                                TvDesign.Black.copy(alpha = .08f)
                            } else {
                                TvDesign.White.copy(alpha = .045f)
                            },
                            shape = RoundedCornerShape(50),
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            } else if (entry.onActivate != null && entry.rightActionLabel.isNullOrBlank()) {
                Text(
                    text = "›",
                    color = if (focused) TvDesign.Black else TvDesign.Dim,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Light,
                )
            }

            if (focused && !entry.rightActionLabel.isNullOrBlank()) {
                Text(
                    text = "${entry.rightActionLabel}  ›",
                    color = TvDesign.Black,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
internal fun rememberTvSettingsDeferredFocusRestore(): () -> Unit {
    val embeddedHost = LocalTvSettingsEmbeddedHost.current
    return remember(embeddedHost) {
        { embeddedHost?.requestDeferredPanelRestore?.invoke() }
    }
}

@Composable
internal fun TvTextEntryDialog(
    title: String,
    initialValue: String,
    secret: Boolean = false,
    placeholder: String = "",
    confirmLabel: String = "Save",
    busy: Boolean = false,
    busyLabel: String = "Working…",
    message: String? = null,
    restoreOnSave: Boolean = true,
    allowSecretReveal: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var value by remember(title, initialValue) { mutableStateOf(initialValue) }
    var secretVisible by remember(title, initialValue, secret) { mutableStateOf(false) }
    val restoreSettingsFocus = rememberTvSettingsDeferredFocusRestore()

    fun dismissAndRestore() {
        onDismiss()
        restoreSettingsFocus()
    }

    fun saveAndRestore() {
        if (busy || value.isBlank()) return
        onSave(value.trim())
        if (restoreOnSave) restoreSettingsFocus()
    }

    val inputFocus = remember { FocusRequester() }
    val revealFocus = remember { FocusRequester() }
    val cancelFocus = remember { FocusRequester() }
    val saveFocus = remember { FocusRequester() }
    val showReveal = secret && allowSecretReveal
    val canSave = !busy && value.isNotBlank()

    TvThemedDialog(title, inputFocus, ::dismissAndRestore, dismissOnBackPress = !busy) {
        val finish = LocalTvMotionFinish.current
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            enabled = !busy,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(inputFocus).focusProperties {
                down = if (showReveal) revealFocus else cancelFocus
            },
            placeholder = { if (placeholder.isNotBlank()) Text(placeholder, color = TvDesign.Dim) },
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            visualTransformation = if (secret && !secretVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            shape = RoundedCornerShape(12.dp),
            colors = tvDialogInputColors(),
        )
        if (showReveal) {
            TvDialogButton(
                label = if (secretVisible) "Hide API key" else "Show API key",
                enabled = !busy,
                modifier = Modifier.focusRequester(revealFocus).focusProperties {
                    up = inputFocus
                    down = cancelFocus
                },
                onClick = { secretVisible = !secretVisible },
            )
        }
        message?.takeIf { it.isNotBlank() }?.let {
            Text(it, color = TvDesign.Muted, fontSize = 12.sp)
        }
        if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = TvDesign.White)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
            val above = if (showReveal) revealFocus else inputFocus
            TvDialogButton(
                label = "Cancel",
                enabled = !busy,
                modifier = Modifier.focusRequester(cancelFocus).focusProperties {
                    up = above
                    if (canSave) right = saveFocus
                },
                onClick = { finish(::dismissAndRestore) },
            )
            TvDialogButton(
                label = if (busy) busyLabel else confirmLabel,
                enabled = canSave,
                modifier = Modifier.focusRequester(saveFocus).focusProperties {
                    up = above
                    left = cancelFocus
                },
                onClick = {
                    // Keep the dialog open while an installation reports progress.
                    if (restoreOnSave) finish(::saveAndRestore) else saveAndRestore()
                },
            )
        }
    }
}

@Composable
internal fun TvDeveloperPinChoice(
    onCancel: () -> Unit,
    onSkip: () -> Unit,
    onSetPin: () -> Unit,
) {
    val skipRequester = remember { FocusRequester() }
    val setPinRequester = remember { FocusRequester() }

    TvModalDialog(onDismissRequest = onCancel, initialFocus = skipRequester) {
        val finish = com.vueo.tv.ui.motion.LocalTvMotionFinish.current
        Box(
            modifier = Modifier.fillMaxSize().background(TvDesign.Black.copy(alpha = .65f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.width(440.dp)
                    .background(TvDesign.Surface, RoundedCornerShape(18.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Protect Developer options?", color = TvDesign.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Text("Set a 4-digit PIN or continue without one.", color = TvDesign.Muted, fontSize = 13.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
                    TvDialogButton(
                        label = "Skip",
                        enabled = true,
                        modifier = Modifier.focusRequester(skipRequester).focusProperties {
                            left = FocusRequester.Cancel
                            right = setPinRequester
                        },
                        onClick = { finish { onSkip() } },
                    )
                    TvDialogButton(
                        label = "Set PIN",
                        enabled = true,
                        modifier = Modifier.focusRequester(setPinRequester).focusProperties {
                            left = skipRequester
                            right = FocusRequester.Cancel
                        },
                        onClick = { finish { onSetPin() } },
                    )
                }
            }
        }
    }
}

@Composable
internal fun TvConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirm",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val restoreSettingsFocus = rememberTvSettingsDeferredFocusRestore()

    fun dismissAndRestore() {
        onDismiss()
        restoreSettingsFocus()
    }

    fun confirmAndRestore() {
        onConfirm()
        restoreSettingsFocus()
    }

    val cancelFocus = remember { FocusRequester() }
    val confirmFocus = remember { FocusRequester() }

    TvThemedDialog(title, cancelFocus, ::dismissAndRestore) {
        val finish = LocalTvMotionFinish.current
        Text(message, color = TvDesign.Muted, fontSize = 13.sp, lineHeight = 19.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
            TvDialogButton(
                label = "Cancel",
                modifier = Modifier.focusRequester(cancelFocus).focusProperties { right = confirmFocus },
                onClick = { finish(::dismissAndRestore) },
            )
            TvDialogButton(
                label = confirmLabel,
                destructive = true,
                modifier = Modifier.focusRequester(confirmFocus).focusProperties { left = cancelFocus },
                onClick = { finish(::confirmAndRestore) },
            )
        }
    }
}

internal fun <T> cycle(values: List<T>, current: T, delta: Int): T {
    if (values.isEmpty()) return current
    val index = values.indexOf(current).takeIf { it >= 0 } ?: 0
    val next = (index + delta).floorMod(values.size)
    return values[next]
}

private fun Int.floorMod(divisor: Int): Int = ((this % divisor) + divisor) % divisor

private fun androidx.compose.ui.input.key.KeyEvent.isTvActivationKey(): Boolean =
    nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
        nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER ||
        nativeKeyEvent.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER

private fun TvSettingsEntry.isWorkspaceTab(): Boolean =
    id.startsWith("addon-category-") || id.startsWith("repository-tab-")

private fun TvSettingsEntry.isAddonControl(): Boolean =
    id in setOf("add", "refresh-addons", "add-repo", "plugins-master", "provider-health", "runtime-diagnostics", "performance-diagnostics") || isWorkspaceTab()

@Composable
private fun TvAddonControls(
    entries: List<TvSettingsEntry>,
    requesterFor: (String) -> FocusRequester,
    onFocused: (String) -> Unit,
    onLeftToSidebar: () -> Unit,
) {
    val actions = entries.filter { it.id in setOf("add", "refresh-addons", "add-repo", "plugins-master") }
    val categories = entries.filter { it.isWorkspaceTab() }
    val systemCards = entries.filter { it.id in setOf("provider-health", "runtime-diagnostics", "performance-diagnostics") }
    val selected = categories.firstOrNull { it.value == "Selected" } ?: categories.firstOrNull()
    val firstAddon = entries.firstOrNull { it.enabled && !it.isAddonControl() && !it.isRepositoryAction() }
    Column(modifier = Modifier.padding(bottom = if (entries.any { it.providerStatus != null }) 8.dp else 0.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            actions.forEachIndexed { index, entry ->
                val actionModifier = Modifier.onPreviewKeyEvent { event ->
                    val keyCode = event.nativeKeyEvent.keyCode
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        if (event.type == KeyEventType.KeyDown) {
                            when {
                                keyCode == KeyEvent.KEYCODE_DPAD_LEFT && index == 0 -> onLeftToSidebar()
                                keyCode == KeyEvent.KEYCODE_DPAD_LEFT -> requesterFor(actions[index - 1].id).requestFocus()
                                index < actions.lastIndex -> requesterFor(actions[index + 1].id).requestFocus()
                            }
                        }
                        true
                    } else false
                }.focusProperties {
                    up = FocusRequester.Cancel
                    down = systemCards.firstOrNull()?.let { requesterFor(it.id) } ?: selected?.let { requesterFor(it.id) } ?: firstAddon?.let { requesterFor(it.id) } ?: FocusRequester.Cancel
                    if (index > 0) left = requesterFor(actions[index - 1].id)
                    if (index < actions.lastIndex) right = requesterFor(actions[index + 1].id)
                    else right = FocusRequester.Cancel
                }
                if (entry.id == "add" || entry.id == "add-repo") {
                    TvSettingsRow(
                        entry = entry, requester = requesterFor(entry.id), first = false, last = false,
                        modifier = Modifier.weight(1f).then(actionModifier),
                        onLeftToSidebar = onLeftToSidebar, onFocused = { onFocused(entry.id) },
                    )
                } else {
                    var focused by remember(entry.id) { mutableStateOf(false) }
                    val shape = RoundedCornerShape(15.dp)
                    val isMaster = entry.id == "plugins-master"
                    Row(
                        modifier = Modifier
                            .width(if (isMaster) 110.dp else if (!entry.operationStatus.isNullOrBlank()) 220.dp else 70.dp).height(70.dp)
                            .then(actionModifier)
                            .focusRequester(requesterFor(entry.id))
                            .onFocusChanged { focused = it.isFocused; if (it.isFocused) onFocused(entry.id) }
                            .onPreviewKeyEvent { event ->
                                if (event.isTvActivationKey()) {
                                    if (event.type == KeyEventType.KeyUp) entry.onActivate?.invoke()
                                    true
                                } else false
                            }
                            .background(if (focused) TvDesign.SurfaceRaised else TvDesign.Surface, shape)
                            .border(1.dp, if (focused) TvDesign.White else TvDesign.White.copy(alpha = .12f), shape)
                            .focusable(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        if (isMaster) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(entry.title, color = TvDesign.White, fontSize = 12.sp)
                            Text(entry.value, color = TvDesign.Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        } else Column(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            entry.icon?.let {
                                Icon(it, contentDescription = entry.title, tint = TvDesign.White,
                                    modifier = Modifier.size(23.dp).then(tvContentRefreshMotion(entry)))
                            }
                            entry.operationStatus?.let {
                                Text(it, color = TvDesign.Muted, fontSize = 10.sp,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
        systemCards.forEachIndexed { index, entry ->
            TvSettingsRow(
                entry = entry, requester = requesterFor(entry.id), first = false, last = false,
                modifier = Modifier.focusProperties {
                    up = requesterFor(if (index > 0) systemCards[index - 1].id else actions.first().id)
                    down = if (index < systemCards.lastIndex) requesterFor(systemCards[index + 1].id)
                        else selected?.let { requesterFor(it.id) } ?: firstAddon?.let { requesterFor(it.id) } ?: FocusRequester.Cancel
                },
                onLeftToSidebar = onLeftToSidebar, onFocused = { onFocused(entry.id) },
            )
        }
TvTabMarkerHost {
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            categories.forEachIndexed { index, entry ->
                var focused by remember(entry.id) { mutableStateOf(false) }
                Text(
                    entry.title,
                    color = if (focused || entry.value == "Selected") TvDesign.White else TvDesign.White.copy(alpha = .58f),
                    fontSize = 14.sp,
                    fontWeight = if (entry.value == "Selected") FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier.tvTabMarker(entry.value == "Selected")
                        .focusRequester(requesterFor(entry.id))
                        .focusProperties {
                            (systemCards.lastOrNull() ?: actions.firstOrNull())?.let { up = requesterFor(it.id) }
                            down = if (firstAddon != null) FocusRequester.Default else FocusRequester.Cancel
                            if (index > 0) left = requesterFor(categories[index - 1].id)
                            if (index < categories.lastIndex) right = requesterFor(categories[index + 1].id)
                            else right = FocusRequester.Cancel
                        }
                        .onFocusChanged {
                            focused = it.isFocused
                            if (it.isFocused) { onFocused(entry.id); entry.onActivate?.invoke() }
                        }
                        .onPreviewKeyEvent { event ->
                            if (index == 0 && event.type == KeyEventType.KeyDown && event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                                onLeftToSidebar(); true
                            } else if (event.isTvActivationKey()) {
                                if (event.type == KeyEventType.KeyUp) entry.onActivate?.invoke()
                                true
                            } else false
                        }
                        .focusable()
                        .padding(vertical = 4.dp),
                )
                if (index < categories.lastIndex) Box(Modifier.width(1.dp).height(20.dp).background(TvDesign.White.copy(alpha = .48f)))
            }
        }
}
    }
}


/** Font preference and preview share one compact card; only the selector is highlighted. */
@Composable
private fun TvSubtitleFontSettingsCard(
    entry: TvSettingsEntry,
    requester: FocusRequester,
    first: Boolean,
    last: Boolean,
    modifier: Modifier,
    onLeftToSidebar: () -> Unit,
    onFocused: () -> Unit,
) {
    var focused by remember(entry.id) { mutableStateOf(false) }
    val shape = RoundedCornerShape(11.dp)
    Column(
        modifier.fillMaxWidth().focusRequester(requester)
            .onFocusChanged { focused = it.isFocused; if (it.isFocused) onFocused() }
            .onPreviewKeyEvent { event ->
                val key = event.nativeKeyEvent.keyCode
                when {
                    event.type == KeyEventType.KeyDown && first && key == KeyEvent.KEYCODE_DPAD_UP -> true
                    event.type == KeyEventType.KeyDown && last && key == KeyEvent.KEYCODE_DPAD_DOWN -> true
                    key == KeyEvent.KEYCODE_DPAD_LEFT -> {
                        if (event.type == KeyEventType.KeyDown) {
                            entry.onPrevious?.invoke() ?: onLeftToSidebar()
                        }
                        true
                    }
                    key == KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        if (event.type == KeyEventType.KeyDown) entry.onNext?.invoke()
                        true
                    }
                    event.isTvActivationKey() -> {
                        if (event.type == KeyEventType.KeyUp) entry.onActivate?.invoke()
                        true
                    }
                    else -> false
                }
            }
            .background(TvDesign.Surface, shape)
            .border(1.dp, TvDesign.White.copy(alpha = if (focused) .28f else .045f), shape)
            .focusable(enabled = entry.enabled)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(entry.title, color = TvDesign.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("Choose your subtitle typeface.", color = TvDesign.Muted, fontSize = 11.sp)
            }
            Row(
                Modifier.width(214.dp).height(36.dp)
                    .background(if (focused) TvDesign.Accent.copy(alpha = .12f) else TvDesign.White.copy(alpha = .045f), RoundedCornerShape(9.dp))
                    .border(if (focused) 2.dp else 1.dp, if (focused) TvDesign.Accent else TvDesign.White.copy(alpha = .15f), RoundedCornerShape(9.dp)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.width(30.dp), contentAlignment = Alignment.Center) { Text("‹", color = TvDesign.White, fontSize = 18.sp) }
                Box(Modifier.width(1.dp).fillMaxHeight().background(TvDesign.White.copy(alpha = .10f)))
                Text(entry.value, color = TvDesign.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Box(Modifier.width(1.dp).fillMaxHeight().background(TvDesign.White.copy(alpha = .10f)))
                Box(Modifier.width(30.dp), contentAlignment = Alignment.Center) { Text("›", color = TvDesign.White, fontSize = 18.sp) }
            }
        }
        entry.supportingContent?.invoke()
    }
}
