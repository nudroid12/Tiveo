package com.vueo.tv.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vueo.tv.R

internal val TiveoTopBarHeight = 64.dp

private val topBarTabs = listOf("Home", "Search", "Library", "Settings", "Profile")

internal class TiveoTopBarState {
    private val requesters = topBarTabs.associateWith { FocusRequester() }
    private val contentFocusCallbacks = mutableMapOf<String, () -> Unit>()

    var focusedTab by mutableStateOf<String?>(null)
        private set
    val hasFocus: Boolean get() = focusedTab != null
    var focusBlocked by mutableStateOf(false)

    var pendingContentTab by mutableStateOf<String?>(null)
        private set

    fun requesterFor(tab: String): FocusRequester = requesters.getValue(tab)

    fun focus(tab: String): Boolean =
        runCatching { requesterFor(tab).requestFocus() }.getOrDefault(false)

    fun updateFocus(tab: String, focused: Boolean) {
        if (focused) {
            focusedTab = tab
            pendingContentTab = null
        } else if (focusedTab == tab) {
            focusedTab = null
            pendingContentTab = null
        }
    }

    fun registerContent(tab: String, requestFocus: () -> Unit) {
        contentFocusCallbacks[tab] = requestFocus
    }

    fun unregisterContent(tab: String, requestFocus: () -> Unit) {
        if (contentFocusCallbacks[tab] === requestFocus) contentFocusCallbacks.remove(tab)
    }

    fun returnToContent(tab: String) {
        val requestFocus = contentFocusCallbacks[tab]
        pendingContentTab = if (requestFocus == null) tab else null
        requestFocus?.invoke()
    }
}

@Composable
internal fun TiveoPageContentFocus(
    topBar: TiveoTopBarState,
    tab: String,
    active: Boolean,
    onReturnToContent: () -> Unit,
) {
    val latestReturnToContent by rememberUpdatedState(onReturnToContent)
    DisposableEffect(topBar, tab, active) {
        val requestFocus = { latestReturnToContent() }
        if (active) topBar.registerContent(tab, requestFocus)
        onDispose { topBar.unregisterContent(tab, requestFocus) }
    }
    LaunchedEffect(topBar, tab, active, topBar.pendingContentTab) {
        if (active && topBar.pendingContentTab == tab) {
            withFrameNanos { }
            if (topBar.hasFocus && topBar.pendingContentTab == tab) topBar.returnToContent(tab)
        }
    }
}

@Composable
internal fun TiveoTopBar(
    state: TiveoTopBarState,
    selected: String,
    canFocus: Boolean,
    onNavigate: (String) -> Unit,
    onReturnToContent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .92f), Color.Transparent)))
            .height(TiveoTopBarHeight)
            .padding(horizontal = MODERN_HOME_CONTENT_START_PADDING),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.virio_wordmark),
            contentDescription = "Tiveo",
            modifier = Modifier.padding(end = 14.dp).width(94.dp).height(28.dp),
        )
        topBarTabs.forEachIndexed { index, tab ->
            val focused = state.focusedTab == tab
            val isSelected = tab == selected
            val underlineScale by animateFloatAsState(
                targetValue = when {
                    focused -> 1f
                    isSelected -> .38f
                    else -> 0f
                },
                animationSpec = tween(durationMillis = 145),
                label = "tabUnderline",
            )
            Column(
                modifier = Modifier
                    .focusRequester(state.requesterFor(tab))
                    .focusProperties {
                        this.canFocus = canFocus
                        left = topBarTabs.getOrNull(index - 1)?.let(state::requesterFor) ?: FocusRequester.Cancel
                        right = topBarTabs.getOrNull(index + 1)?.let(state::requesterFor) ?: FocusRequester.Cancel
                        up = FocusRequester.Cancel
                        down = FocusRequester.Cancel
                    }
                    .onFocusChanged { focus ->
                        val gainedFocus = focus.isFocused && state.focusedTab != tab
                        state.updateFocus(tab, focus.isFocused)
                        if (gainedFocus && canFocus) onNavigate(tab)
                    }
                    .onPreviewKeyEvent {
                        if (it.key != Key.DirectionDown) return@onPreviewKeyEvent false
                        if (it.type == KeyEventType.KeyDown && it.nativeKeyEvent.repeatCount == 0) {
                            onReturnToContent()
                        }
                        true
                    }
                    .semantics { this.selected = isSelected }
                    .clickable(role = Role.Tab) {
                        when (tab) {
                            selected -> onReturnToContent()
                            else -> onNavigate(tab)
                        }
                    }
                    .width(IntrinsicSize.Max)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (tab == "Library") "My List" else tab,
                    color = if (isSelected || focused) Color.White else Color.White.copy(alpha = .76f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                )
                Box(
                    Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                        .height(2.dp)
                        .graphicsLayer { scaleX = underlineScale }
                        .background(Color.White, RoundedCornerShape(1.dp)),
                )
            }
        }
    }
}
