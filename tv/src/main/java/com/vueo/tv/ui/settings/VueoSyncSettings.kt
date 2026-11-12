package com.vueo.tv.settings

import android.view.KeyEvent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.vueo.tv.ui.TvDesign
import com.vueo.tv.ui.tvDialogInputColors
import androidx.compose.ui.platform.LocalContext
import com.vueo.shared.core.sync.VueoSyncService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun TvVueoSyncSettings(onNavigate: (String)->Unit,onProfile: ()->Unit,onBack: ()->Unit) {
    val context=LocalContext.current
    val service=remember { VueoSyncService.get(context) }
    val status by service.state.collectAsState()
    val revision by service.revision.collectAsState()
    val connected=remember(revision) { service.connected() }
    val syncOn=remember(revision) { service.syncEnabled() }
    val scope=rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var confirmSignOut by remember { mutableStateOf(false) }
    fun action(block: suspend ()->String) {
        if(busy) return
        busy=true
        scope.launch {
            try {
                message = block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                message = VueoSyncService.message(error)
            } finally {
                password = ""
                busy = false
            }
        }
    }
    fun authenticate(register: Boolean) {
        if (busy) return
        if (email.isBlank() || password.isBlank()) {
            message = "Enter your email and password."
            return
        }
        action { service.signIn(email, password, register) }
    }

    val entries = buildList {
        if (!connected) {
            add(TvSettingsEntry("email", "Email", "", formControl = { modifier, left, first ->
                TvAccountField("Email", email, { email = it }, busy, false, modifier, left, first)
            }))
            add(TvSettingsEntry("password", "Password", "", formControl = { modifier, left, first ->
                TvAccountField("Password", password, { password = it }, busy, true, modifier, left, first)
            }))
            add(TvSettingsEntry("sign-in", "Sign In", "", formControl = { modifier, left, _ ->
                var focused by remember { mutableStateOf(false) }
                Button(
                    onClick = { authenticate(register = false) },
                    modifier = modifier.accountActionNavigation(left)
                        .onFocusChanged { focused = it.isFocused }
                        .border(2.dp, if (focused) TvDesign.Black else Color.Transparent, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TvDesign.White,
                        contentColor = TvDesign.Black,
                    ),
                ) { Text(if (busy) "Please wait…" else "Sign In") }
            }))
            add(TvSettingsEntry("register", "Create Account", "", formControl = { modifier, left, _ ->
                var focused by remember { mutableStateOf(false) }
                TextButton(
                    onClick = { authenticate(register = true) },
                    modifier = modifier.accountActionNavigation(left, last = true)
                        .onFocusChanged { focused = it.isFocused },
                    contentPadding = PaddingValues(8.dp),
                ) {
                    Text(
                        "New to Tiveo? Create Account",
                        color = if (focused) TvDesign.Focus else TvDesign.White,
                        textDecoration = if (focused) TextDecoration.Underline else TextDecoration.None,
                    )
                }
            }))
            if (message.isNotBlank()) add(TvSettingsEntry("message", message, "", enabled = false))
        } else {
            add(TvSettingsEntry("status", "Account", service.email(), "Connected", enabled = false, section = "ACCOUNT"))
            add(TvSettingsEntry("message", "Account Sync", if (message.isBlank() || message == status) status else "$message\n$status", enabled = false))
            add(TvSettingsEntry("sync-all", if(syncOn) "Sync On" else "Sync Off",
                "One control for all account data.", value=if(syncOn) "On" else "Off", enabled=!busy,
                onActivate={action {service.setSyncEnabledAndWait(!syncOn); service.state.value}}, section=null))
            add(TvSettingsEntry("sync-now","Sync Now","Sync your library and connections now.",enabled=!busy && syncOn,
                onActivate={action { service.syncNow(); service.state.value }},section="ACTIONS"))
            add(TvSettingsEntry("sign-out",if(confirmSignOut) "Confirm Sign Out" else "Sign Out","Local data stays on this TV.",enabled=!busy,
                onActivate={if(confirmSignOut) action {service.signOut(); confirmSignOut=false; "Signed out"} else confirmSignOut=true}))
            if(confirmSignOut) add(TvSettingsEntry("cancel-sign-out","Cancel","Stay connected.",onActivate={confirmSignOut=false}))

        }
    }
    TvSettingsListScreen(
        if (connected) "Account" else "Sign in to Tiveo",
        if (connected) "Your library and connections across mobile and TV."
        else "Use the same account you use on mobile.",
        entries, onNavigate, onProfile, onBack,
    )
}

private fun Modifier.accountActionNavigation(onLeft: () -> Unit, last: Boolean = false) =
    onPreviewKeyEvent { event ->
        when (event.nativeKeyEvent.keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (event.type == KeyEventType.KeyDown) onLeft()
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> last
            else -> false
        }
    }

@Composable
private fun TvAccountField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    busy: Boolean,
    secret: Boolean,
    modifier: Modifier,
    onLeft: () -> Unit,
    first: Boolean,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var editing by remember { mutableStateOf(false) }

    var keyboardRequest by remember { mutableIntStateOf(0) }
    LaunchedEffect(editing, busy, keyboardRequest) {
        if (editing && !busy) {
            withFrameNanos { }
            keyboard?.show()
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .onFocusChanged {
                if (!it.isFocused && editing) {
                    editing = false
                    keyboard?.hide()
                }
            }
            .onPreviewKeyEvent { event ->
                when (event.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                        if (busy) false
                        else {
                            if (event.type == KeyEventType.KeyUp) {
                                editing = true
                                keyboardRequest++
                            }
                            true
                        }
                    }
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        if (editing) false
                        else {
                            if (event.type == KeyEventType.KeyDown) onLeft()
                            true
                        }
                    }
                    KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> {
                        if (editing) false
                        else {
                            val up = event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_UP
                            if (event.type == KeyEventType.KeyDown && (!up || !first)) {
                                focusManager.moveFocus(if (up) FocusDirection.Up else FocusDirection.Down)
                            }
                            true
                        }
                    }
                    else -> false
                }
            },
        readOnly = busy || !editing,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (secret) KeyboardType.Password else KeyboardType.Email,
            imeAction = ImeAction.Done,
            showKeyboardOnFocus = false,
        ),
        keyboardActions = KeyboardActions(onDone = {
            editing = false
            keyboard?.hide()
        }),
        shape = RoundedCornerShape(8.dp),
        colors = tvDialogInputColors(),
    )
}
