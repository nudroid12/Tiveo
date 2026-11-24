package com.vueo.tv.settings

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.vueo.shared.core.dna.UserDnaPreferences
import com.vueo.shared.core.dna.UserDnaSnapshot
import com.vueo.shared.core.extensions.CatalogDiscoveryCache
import com.vueo.shared.core.enrichment.MdblistClient
import com.vueo.shared.core.enrichment.TmdbEnhancementClient
import com.vueo.shared.core.plugin.PluginRepositoryDescriptor
import com.vueo.shared.core.plugin.PluginHealthStore
import com.vueo.shared.core.plugin.PluginProviderDescriptor
import com.vueo.shared.core.plugin.ProviderCodeStore
import com.vueo.shared.core.plugin.providerHealthSortKey
import com.vueo.shared.core.profile.ProfileAvatarCatalog
import com.vueo.shared.core.source.SourceDiscoveryCache
import com.vueo.shared.core.storage.AppAccent
import com.vueo.shared.core.storage.AppTheme
import com.vueo.shared.core.storage.PlayerVideoFit
import com.vueo.shared.core.storage.PreferredQuality
import com.vueo.shared.core.storage.SubtitleLanguage
import com.vueo.shared.core.storage.SubtitleSize
import com.vueo.shared.core.storage.SubtitleVisibility
import com.vueo.shared.core.storage.VueoBackupManager
import com.vueo.tv.BuildConfig
import com.vueo.tv.R
import com.vueo.tv.core.TvRuntime
import com.vueo.tv.ui.TvDesign
import com.vueo.tv.ui.TvDialogButton
import com.vueo.tv.ui.TvThemedDialog
import com.vueo.tv.ui.motion.LocalTvMotionFinish
import com.vueo.tv.ui.TvSidebarPreferences
import com.vueo.tv.ui.TvSidebarStyle
import com.vueo.tv.update.TvUpdateManager
import com.vueo.tv.update.TvUpdateRelease
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun TvDataStorageSettings(
    runtime: TvRuntime,
    onNavigate: (String) -> Unit,
    onProfile: () -> Unit,
    onDataChanged: () -> Unit,
    onResetComplete: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val restoreSettingsFocus = rememberTvSettingsDeferredFocusRestore()
    val store = runtime.settingsStore
    var includeCredentials by remember { mutableStateOf(store.includeCredentialsInBackup()) }
    var status by remember { mutableStateOf<String?>(null) }
    var confirmAction by remember { mutableStateOf<String?>(null) }

    var backupResult by remember { mutableStateOf<Pair<String, String>?>(null) }
    var saveDialog by remember { mutableStateOf(false) }
    var backupProgress by remember { mutableStateOf<String?>(null) }
    var backupBusy by remember { mutableStateOf(false) }
    var backupChoices by remember { mutableStateOf<List<TvLocalBackup>?>(null) }
    var backupListError by remember { mutableStateOf<String?>(null) }
    var selectedBackup by remember { mutableStateOf<TvLocalBackup?>(null) }

    fun restoreBackup(uri: Uri) {
        if (backupBusy) return
        backupBusy = true
        backupProgress = "Restoring backup…"
        status = null
        scope.launch {
            try {
                val result = VueoBackupManager.restoreFromUri(context, uri)
                runtime.reloadPersistentConfiguration()
                TvDesign.applyTheme(runtime.settingsStore.appTheme())
                TvDesign.applyAccent(runtime.settingsStore.appAccent())
                includeCredentials = runtime.settingsStore.includeCredentialsInBackup()
                backupResult = "Backup restored" to "${result.valueCount} values restored."
                onDataChanged()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                backupResult = "Restore failed" to (error.message ?: "Unable to restore backup.")
            } finally {
                backupProgress = null
                backupBusy = false
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        restoreSettingsFocus()
        if (uri != null) selectedBackup = TvLocalBackup("The selected backup", uri)
    }
    backupChoices?.let { choices ->
        TvRestoreBackupPicker(
            files = choices,
            message = backupListError,
            onPick = { backupChoices = null; selectedBackup = it },
            onBrowse = {
                backupChoices = null
                try {
                    restoreLauncher.launch(arrayOf("application/json", "text/json", "text/plain"))
                } catch (_: ActivityNotFoundException) {
                    backupResult = "File picker unavailable" to "Choose a backup saved by Tiveo in Restore Backup."
                    restoreSettingsFocus()
                }
            },
            onDismiss = { backupChoices = null; restoreSettingsFocus() },
        )
    }
    selectedBackup?.let { backup ->
        TvConfirmDialog(
            title = "Restore this backup?",
            message = "${backup.name} will replace current local Tiveo data.",
            confirmLabel = "Restore",
            onDismiss = { selectedBackup = null },
            onConfirm = { selectedBackup = null; restoreBackup(backup.uri) },
        )
    }

    if (saveDialog) {
        TvSaveBackupDialog(
            includeCredentials = includeCredentials,
            onDismiss = { saveDialog = false; restoreSettingsFocus() },
            onSave = { name, folder, destination ->
                saveDialog = false
                if (!backupBusy) {
                    backupBusy = true
                    backupProgress = "Saving backup…"
                    val include = includeCredentials
                    scope.launch {
                        try {
                            val backup = TvLocalBackups.save(context, name, folder, include)
                            backupResult = "Backup saved" to "$destination/${backup.name}"
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (error: Exception) {
                            backupResult = "Backup failed" to (error.message ?: "Unable to create backup.")
                        } finally {
                            backupProgress = null
                            backupBusy = false
                        }
                    }
                }
            },
        )
    }

    backupProgress?.let { TvBackupProgressDialog(it) }

    backupResult?.let { (title, message) ->
        TvBackupResultDialog(title, message) {
            backupResult = null
            restoreSettingsFocus()
        }
    }

    confirmAction?.let { action ->
        val (title, message) = when (action) {
            "history" -> "Clear watch history?" to "Remove Watch History for the active profile?"
            "continue" -> "Clear Continue Watching?" to "Remove all Continue Watching entries for the active profile?"
            "cache" -> "Clear cache?" to "Rebuild Home and source-discovery caches on next use?"
            else -> "Reset Tiveo?" to "Clear profiles, configuration, Library and playback data on this TV?"
        }
        TvConfirmDialog(
            title = title,
            message = message,
            confirmLabel = if (action == "reset") "Reset" else "Clear",
            onDismiss = { confirmAction = null },
            onConfirm = {
                confirmAction = null
                scope.launch {
                    when (action) {
                        "history" -> runtime.libraryStore.clearHistory()
                        "continue" -> runtime.libraryStore.clearContinueWatching()
                        "cache" -> {
                            CatalogDiscoveryCache.clearAll(context.applicationContext)
                            SourceDiscoveryCache.clearAll()
                        }
                        "reset" -> {
                            VueoBackupManager.resetUserData(context.applicationContext)
                            runtime.reloadPersistentConfiguration()
                            TvDesign.applyTheme(runtime.settingsStore.appTheme())
                            TvDesign.applyAccent(runtime.settingsStore.appAccent())
                            includeCredentials = runtime.settingsStore.includeCredentialsInBackup()
                        }
                    }
                    status = when (action) {
                        "history" -> "Watch History cleared."
                        "continue" -> "Continue Watching cleared."
                        "cache" -> "Caches cleared."
                        else -> "Tiveo local data reset."
                    }
                    onDataChanged()
                    if (action == "reset") onResetComplete()
                }
            },
        )
    }

    val entries = buildList {
        add(toggleEntry("credentials", "Include API Keys in Backup", "Off by default. Enable only when you explicitly want credentials in the JSON backup.", includeCredentials) {
            includeCredentials = it
            store.setIncludeCredentialsInBackup(it)
        }.copy(section = "BACKUP & RESTORE", icon = Icons.Default.VideoLibrary))
        add(TvSettingsEntry("export", "Create Backup", "Save a JSON backup to ${TvLocalBackups.locationLabel}.",
            if (backupBusy) "Please wait…" else "Export", onActivate = {
                if (!backupBusy) saveDialog = true
            }, section = "BACKUP & RESTORE"))
        add(TvSettingsEntry("restore", "Restore Backup", "Choose a saved Tiveo backup or browse for a JSON file.", "Restore", onActivate = {
            if (!backupBusy) {
                backupBusy = true
                backupListError = null
                scope.launch {
                    try {
                        backupChoices = TvLocalBackups.list(context)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        backupListError = "Unable to read backups in ${TvLocalBackups.locationLabel}."
                        backupChoices = emptyList()
                    } finally {
                        backupBusy = false
                    }
                }
            }
        }, section = "BACKUP & RESTORE"))
        add(TvSettingsEntry("clear-cache", "Catalog & Source Cache", "Clear catalog and source-discovery caches; configuration is preserved.", "Clear", onActivate = { confirmAction = "cache" }, section = "LOCAL DATA", icon = Icons.Default.SettingsInputComponent))
        add(TvSettingsEntry("clear-continue", "Continue Watching", "Remove unfinished playback entries for the active profile only.", "Clear", onActivate = { confirmAction = "continue" }, section = "LOCAL DATA"))
        add(TvSettingsEntry("clear-history", "Watch History", "Clear playback history for the active profile without changing Watchlist.", "Clear", onActivate = { confirmAction = "history" }, section = "LOCAL DATA"))
        add(TvSettingsEntry("reset", "Reset Tiveo Data", "Return local configuration and Library data to a fresh state without uninstalling the APK.", "Reset", onActivate = { confirmAction = "reset" }, section = "RESET"))
        status?.let { add(TvSettingsEntry("status", "Status", it, enabled = false, section = "STATUS")) }
    }

    TvSettingsListScreen("Data & Storage", "Backup, restore, cache, history and local data controls.", entries, onNavigate, onProfile, onBack)
}

internal const val TV_ABOUT_UPDATE_ROW_ID = "update"

@Composable
internal fun TvAboutSettings(
    onNavigate: (String) -> Unit,
    onProfile: () -> Unit,
    onBack: () -> Unit,
    developerOptionsEnabled: Boolean,
    onUnlockDeveloperOptions: () -> Unit,
    onUnlockNotice: (String?) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val restoreSettingsFocus = rememberTvSettingsDeferredFocusRestore()
    val installPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { restoreSettingsFocus() }
    var info by remember { mutableStateOf<AboutInfo?>(null) }
    var tiveoRowPresses by remember(developerOptionsEnabled) { mutableIntStateOf(0) }
    var checking by remember { mutableStateOf(false) }
    var downloading by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var release by remember { mutableStateOf<TvUpdateRelease?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    val available = release?.takeIf { it.isNewerThanCurrent() }

    fun updateAction() {
        if (checking || downloading) return
        if (available != null) {
            if (TvUpdateManager.needsInstallPermission(context)) {
                TvUpdateManager.installPermissionIntent(context)?.let { installPermissionLauncher.launch(it) }
                status = "Allow installs for Tiveo, then choose Update again."
                return
            }
            downloading = true
            progress = 0
            status = null
            scope.launch {
                TvUpdateManager.downloadAndInstall(context.applicationContext, available) { progress = it }
                    .onFailure { status = it.message ?: "Unable to install update." }
                downloading = false
            }
            return
        }

        checking = true
        status = null
        scope.launch {
            val result = TvUpdateManager.check(context.applicationContext, force = true)
            release = result.release
            checking = false
            status = when {
                result.error != null -> result.error
                result.release?.isNewerThanCurrent() == true -> null
                else -> "You're up to date."
            }
        }
    }

    val entries = listOf(
        TvSettingsEntry(
            id = "tiveo-about",
            title = "Tiveo",
            subtitle = "",
            identityCard = true,
            iconDrawableRes = R.drawable.virio_wordmark,
            value = "Version ${BuildConfig.VERSION_NAME}",
            onActivate = {
                if (developerOptionsEnabled) {
                    onUnlockNotice("Developer options are already unlocked.")
                } else {
                    tiveoRowPresses += 1
                    when (val stepsLeft = 7 - tiveoRowPresses) {
                        0 -> {
                            tiveoRowPresses = 0
                            onUnlockNotice(null)
                            onUnlockDeveloperOptions()
                        }
                        1 -> onUnlockNotice("1 step away from Developer options.")
                        2, 3 -> onUnlockNotice("$stepsLeft steps away from Developer options.")
                    }
                }
            },
        ),
        TvSettingsEntry(
            TV_ABOUT_UPDATE_ROW_ID,
            title = when {
                checking -> "Checking for Updates…"
                downloading -> "Downloading Update…"
                available != null && TvUpdateManager.needsInstallPermission(context) -> "Allow Installation"
                available != null -> "Install Update"
                else -> "Check for Updates"
            },
            subtitle = status ?: available?.versionName?.let { "New version · $it" }
                ?: "Check and install the latest Tiveo build.",
            value = when {
                downloading -> "$progress%"
                !checking && available == null -> "Check"
                else -> ""
            },
            onActivate = ::updateAction,
            showDivider = false,
            actionCard = true,
        ),
        TvSettingsEntry(
            id = "about-privacy",
            title = AboutInfo.PRIVACY.title,
            subtitle = "View how Tiveo handles app data.",
            onActivate = { info = AboutInfo.PRIVACY },
            showDivider = false,
            actionCard = true,
        ),
        TvSettingsEntry(
            id = "about-licenses",
            title = AboutInfo.LICENSES.title,
            subtitle = "View data source credits and open source notices.",
            onActivate = { info = AboutInfo.LICENSES },
            showDivider = false,
            actionCard = true,
        ),
    )
    TvSettingsListScreen(
        "About Tiveo", "App information, updates, and legal notices",
        entries, onNavigate, onProfile, onBack,
        preferredFocusId = TV_ABOUT_UPDATE_ROW_ID,
    )

    info?.let { notice ->
        val closeFocus = remember { FocusRequester() }
        val close = {
            info = null
            restoreSettingsFocus()
        }
        TvThemedDialog(notice.title, closeFocus, close) {
            val finish = LocalTvMotionFinish.current
            notice.paragraphs.forEach { paragraph ->
                Text(paragraph, color = TvDesign.Muted, fontSize = 13.sp, lineHeight = 19.sp)
            }
            TvDialogButton(
                label = "Close",
                modifier = Modifier.align(Alignment.End).focusRequester(closeFocus),
                onClick = { finish(close) },
            )
        }
    }
}

private enum class AboutInfo(val title: String, val paragraphs: List<String>) {
    PRIVACY(
        "Privacy",
        listOf(
            "Tiveo stores your settings and library on this device.",
            "When Account Sync is enabled, supported profile, library and connection data are sent to the account server. Manage sync in Settings → Account.",
            "Enabled addons and services receive requests when Tiveo uses them to fetch content or metadata.",
        ),
    ),
    LICENSES(
        "Licenses & Attribution",
        listOf(
            "The collection interface and addon compatibility code include work from Nuvio contributors. The source repository includes GPL-3.0 notices in LICENSES and credits in THIRD_PARTY_COLLECTIONS.md.",
            "This product uses the TMDB API but is not endorsed or certified by TMDB.",
        ),
    ),
}
