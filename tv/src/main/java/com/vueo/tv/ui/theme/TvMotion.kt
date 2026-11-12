package com.vueo.tv.ui.motion

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color

/**
 * VUEO TV motion language.
 *
 * Screen changes use an overlapping handoff instead of the old delayed
 * fade-through. The incoming surface starts immediately; the outgoing surface
 * remains opaque for a short hold before fading. This prevents the black app
 * background from flashing between two expensive TV Compose trees.
 *
 * Only the incoming screen receives a very small depth scale so full-screen
 * surfaces are never both scaling at once.
 */
internal object TvMotion {
    const val FOCUS_IN_MS = 120
    const val FOCUS_OUT_MS = 90
    const val QUICK_MS = 110
    const val ELEMENT_MS = 160
    const val PANEL_IN_MS = 200
    const val PANEL_OUT_MS = 125
    const val SCREEN_IN_MS = 175
    const val SCREEN_OUT_MS = 128
    const val TAB_IN_MS = 170
    const val TAB_OUT_MS = 130
    const val TAB_OUT_DELAY_MS = 40
    const val BACKDROP_MS = 270

    val EaseOut = CubicBezierEasing(0.22f, 0.61f, 0.36f, 1f)
    val EaseIn = CubicBezierEasing(0.40f, 0f, 1f, 1f)
    val EaseInOut = CubicBezierEasing(0.40f, 0f, 0.20f, 1f)
}

/** Forward/deeper route transition with no incoming delay. */
private fun originalScreenFadeThrough(
    enterDurationMillis: Int = TvMotion.SCREEN_IN_MS,
    exitDurationMillis: Int = TvMotion.SCREEN_OUT_MS,
    initialScale: Float = 0.990f,
): ContentTransform =
    (
        fadeIn(
            animationSpec = tween(
                durationMillis = enterDurationMillis,
                easing = TvMotion.EaseOut,
            ),
        ) +
            scaleIn(
                initialScale = initialScale,
                animationSpec = tween(
                    durationMillis = (enterDurationMillis + 15),
                    easing = TvMotion.EaseOut,
                ),
            )
    ) togetherWith
        fadeOut(
            animationSpec = tween(
                durationMillis = exitDurationMillis,
                delayMillis = 42,
                easing = TvMotion.EaseInOut,
            ),
        )

/** Reverse route transition. The returning screen starts immediately. */
private fun originalScreenBackTransition(): ContentTransform =
    (
        fadeIn(
            animationSpec = tween(
                durationMillis = 150,
                easing = TvMotion.EaseOut,
            ),
        ) +
            scaleIn(
                initialScale = 1.008f,
                animationSpec = tween(
                    durationMillis = 165,
                    easing = TvMotion.EaseOut,
                ),
            )
    ) togetherWith
        fadeOut(
            animationSpec = tween(
                durationMillis = 112,
                delayMillis = 34,
                easing = TvMotion.EaseInOut,
            ),
        )

/** Crossfade the content while the top bar keeps its position and focus. */
private fun originalTabCrossTransition(): ContentTransform =
    fadeIn(
        animationSpec = tween(
            durationMillis = TvMotion.TAB_IN_MS,
            easing = TvMotion.EaseInOut,
        ),
    ) togetherWith fadeOut(
        animationSpec = tween(
            durationMillis = TvMotion.TAB_OUT_MS,
            delayMillis = TvMotion.TAB_OUT_DELAY_MS,
            easing = TvMotion.EaseInOut,
        ),
    )

internal fun tvImmediateCut(): ContentTransform =
    EnterTransition.None togetherWith ExitTransition.None

/** Player route uses fade only so video never appears to zoom. */
private fun originalPlayerFadeThrough(
    enterDurationMillis: Int = 165,
    exitDurationMillis: Int = 105,
): ContentTransform =
    fadeIn(
        animationSpec = tween(
            durationMillis = enterDurationMillis,
            easing = TvMotion.EaseOut,
        ),
    ) togetherWith
        fadeOut(
            animationSpec = tween(
                durationMillis = exitDurationMillis,
                delayMillis = 32,
                easing = TvMotion.EaseInOut,
            ),
        )

private fun originalPanelEnter(): EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = TvMotion.PANEL_IN_MS,
            delayMillis = 8,
            easing = TvMotion.EaseOut,
        ),
    ) +
        scaleIn(
            initialScale = 0.985f,
            animationSpec = tween(
                durationMillis = TvMotion.PANEL_IN_MS,
                delayMillis = 8,
                easing = TvMotion.EaseOut,
            ),
        )

private fun originalPanelExit(): ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = TvMotion.PANEL_OUT_MS,
            easing = TvMotion.EaseInOut,
        ),
    ) +
        scaleOut(
            targetScale = 0.992f,
            animationSpec = tween(
                durationMillis = TvMotion.PANEL_OUT_MS,
                easing = TvMotion.EaseInOut,
            ),
        )

private fun originalFocusSpec(focused: Boolean = true): FiniteAnimationSpec<Float> =
    tween(
        durationMillis = if (focused) TvMotion.FOCUS_IN_MS else TvMotion.FOCUS_OUT_MS,
        easing = TvMotion.EaseOut,
    )

private fun originalFocusColorSpec(focused: Boolean = true): FiniteAnimationSpec<Color> =
    tween(
        durationMillis = if (focused) TvMotion.FOCUS_IN_MS else TvMotion.FOCUS_OUT_MS,
        easing = TvMotion.EaseOut,
    )


internal fun tvScreenFadeThrough(
    enterDurationMillis: Int = TvMotion.SCREEN_IN_MS,
    exitDurationMillis: Int = TvMotion.SCREEN_OUT_MS,
    initialScale: Float = .990f,
): ContentTransform = tvTunedContent(TvMotionGroup.FORWARD, originalScreenFadeThrough(enterDurationMillis, exitDurationMillis, initialScale))
internal fun tvScreenBackTransition(): ContentTransform = tvTunedContent(TvMotionGroup.BACK, originalScreenBackTransition())
internal fun tvTabCrossTransition(): ContentTransform = tvTunedContent(TvMotionGroup.TABS, originalTabCrossTransition())
internal fun tvPlayerFadeThrough(enterDurationMillis: Int = 165, exitDurationMillis: Int = 105): ContentTransform =
    tvTunedContent(TvMotionGroup.PLAYER_ROUTE, originalPlayerFadeThrough(enterDurationMillis, exitDurationMillis))
internal fun tvPanelEnter(): EnterTransition = tvTunedEnter(TvMotionGroup.PANEL, originalPanelEnter())
internal fun tvPanelExit(): ExitTransition = tvTunedExit(TvMotionGroup.PANEL, originalPanelExit())
internal fun tvFocusSpec(focused: Boolean = true): FiniteAnimationSpec<Float> =
    tvTunedSpec(TvMotionGroup.FOCUS, focused, originalFocusSpec(focused))
internal fun tvFocusColorSpec(focused: Boolean = true): FiniteAnimationSpec<Color> =
    tvTunedSpec(TvMotionGroup.FOCUS, focused, originalFocusColorSpec(focused))
