package com.vueo.tv.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vueo.tv.R

private val VirioHomeTabs = listOf("Home", "Search", "Library", "Settings", "Profile")

@Composable
internal fun VirioHomeTopBar(
    homeRequester: FocusRequester,
    canFocus: Boolean,
    onFocused: () -> Unit,
    onReturnToContent: () -> Unit,
    onNavigate: (String) -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val requesters = remember(homeRequester) {
        VirioHomeTabs.map { if (it == "Home") homeRequester else FocusRequester() }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .92f), Color.Transparent)))
            .height(64.dp)
            .padding(horizontal = MODERN_HOME_CONTENT_START_PADDING),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.virio_wordmark),
            contentDescription = "Virio",
            modifier = Modifier.padding(end = 14.dp).width(94.dp).height(28.dp),
        )
        VirioHomeTabs.forEachIndexed { index, destination ->
            var focused by remember(destination) { mutableStateOf(false) }
            val home = destination == "Home"
            Text(
                text = if (destination == "Library") "My List" else destination,
                color = when {
                    focused -> Color.Black
                    home -> Color.White
                    else -> Color.White.copy(alpha = .76f)
                },
                fontSize = 14.sp,
                fontWeight = if (home || focused) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .focusRequester(requesters[index])
                    .focusProperties {
                        this.canFocus = canFocus
                        left = requesters.getOrNull(index - 1) ?: FocusRequester.Cancel
                        right = requesters.getOrNull(index + 1) ?: FocusRequester.Cancel
                        up = FocusRequester.Cancel
                        down = FocusRequester.Cancel
                    }
                    .onFocusChanged {
                        focused = it.isFocused
                        if (focused) onFocused()
                    }
                    .onPreviewKeyEvent {
                        if (it.key != Key.DirectionDown) return@onPreviewKeyEvent false
                        if (it.type == KeyEventType.KeyDown) onReturnToContent()
                        true
                    }
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (focused) Color.White else Color.Transparent)
                    .clickable(role = Role.Button) {
                        when (destination) {
                            "Home" -> onReturnToContent()
                            "Profile" -> onProfile()
                            else -> onNavigate(destination)
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
            )
        }
    }
}
