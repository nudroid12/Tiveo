package com.vueo.tv

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import com.vueo.tv.settings.TvMotionBackShortcut
import com.vueo.tv.ui.TvBottomNotice
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vueo.tv.watchnext.TvWatchNextDeepLink
import com.vueo.tv.watchnext.TvWatchNextPublisher
import androidx.compose.ui.Modifier
import com.vueo.shared.core.diagnostics.RuntimeDiagnostics
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    private val motionBackShortcut = TvMotionBackShortcut()
    private val exitHandler = Handler(Looper.getMainLooper())
    private var pendingChannelLink by mutableStateOf<String?>(null)
    private var pendingWatchNextMediaKey by mutableStateOf<String?>(null)
    private var lastExitPressMs: Long? = null
    private var showExitPrompt by mutableStateOf(false)
    private val clearExitPrompt = Runnable {
        showExitPrompt = false
        lastExitPressMs = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RuntimeDiagnostics.install(applicationContext)
        pendingWatchNextMediaKey = TvWatchNextDeepLink.parseMediaKey(intent?.data)
        pendingChannelLink = intent?.data?.takeIf(com.vueo.tv.channels.TvHomeChannels::accepts)?.toString()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            Box(Modifier.fillMaxSize()) {
                VueoTvApp(
                    onExit = ::requestExit,
                    channelLink = pendingChannelLink,
                    onChannelConsumed = { if (pendingChannelLink == it) pendingChannelLink = null },
                    watchNextMediaKey = pendingWatchNextMediaKey,
                    onWatchNextConsumed = { consumed ->
                        if (pendingWatchNextMediaKey == consumed) {
                            pendingWatchNextMediaKey = null
                        }
                    },
                )
                if (showExitPrompt) {
                    TvBottomNotice("Press Back again to exit")
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingChannelLink = intent.data?.takeIf(com.vueo.tv.channels.TvHomeChannels::accepts)?.toString()
        TvWatchNextDeepLink.parseMediaKey(intent.data)?.let { mediaKey ->
            pendingWatchNextMediaKey = mediaKey
        }
    }

    private fun requestExit() {
        val now = SystemClock.elapsedRealtime()
        val previousPress = lastExitPressMs
        if (previousPress != null && now - previousPress < 2_000L) {
            exitHandler.removeCallbacks(clearExitPrompt)
            clearExitPrompt.run()
            finish()
            return
        }
        lastExitPressMs = now
        showExitPrompt = true
        exitHandler.removeCallbacks(clearExitPrompt)
        exitHandler.postDelayed(clearExitPrompt, 2_000L)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (motionBackShortcut.dispatch(event) { super.dispatchKeyEvent(it) }) return true
        return super.dispatchKeyEvent(event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (!hasFocus) motionBackShortcut.cancel()
        super.onWindowFocusChanged(hasFocus)
    }

    override fun onPause() {
        motionBackShortcut.cancel()
        exitHandler.removeCallbacks(clearExitPrompt)
        clearExitPrompt.run()
        TvWatchNextPublisher.schedule(applicationContext)
        com.vueo.tv.channels.TvHomeChannels.schedule(applicationContext)
        super.onPause()
    }

}
