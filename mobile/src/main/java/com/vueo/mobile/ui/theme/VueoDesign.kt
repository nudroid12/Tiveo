package com.vueo.mobile.ui

import com.vueo.shared.ui.appearance.AppAppearance

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vueo.mobile.R
import com.vueo.mobile.core.storage.AppAccent
import com.vueo.mobile.core.storage.AppTheme

internal object VueoPalette {
    val Theme get() = AppAppearance.theme
    val AccentChoice get() = AppAppearance.accentChoice
    val Background get() = AppAppearance.palette.background
    val Nav get() = AppAppearance.palette.nav
    val Surface get() = AppAppearance.palette.surface
    val SurfaceElevated get() = AppAppearance.palette.elevated
    val SurfaceStrong get() = AppAppearance.palette.raised
    val ProfileSurface get() = AppAppearance.palette.profile
    val ProfileSurfaceAlt get() = AppAppearance.palette.profileAlt
    val Stroke get() = AppAppearance.palette.stroke
    val Muted get() = AppAppearance.palette.muted
    val Accent get() = AppAppearance.accent
    val OnAccent get() = AppAppearance.onAccent
    val AccentContainer get() = AppAppearance.accentContainer
    val FocusSurface get() = AppAppearance.focusSurface
    // Branding and semantic colors do not change with the interactive theme.
    val BrandLime = Color(0xFFB6FF00)

    fun applyTheme(theme: AppTheme) = AppAppearance.applyTheme(theme)
    fun applyAccent(accent: AppAccent) = AppAppearance.applyAccent(accent)

    val Success = Color(0xFF72DF87)
    val Warning = Color(0xFFFFB84D)
    val Error = Color(0xFFFF6767)
}

internal fun AppAccent.composeColor(): Color = Color(argb)

@Composable
internal fun VueoBrandLockup(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Image(
        painter = painterResource(R.drawable.virio_wordmark),
        contentDescription = "Tiveo",
        modifier = modifier.width(if (compact) 94.dp else 124.dp).aspectRatio(1075f / 210f),
        contentScale = ContentScale.Fit,
    )
}
