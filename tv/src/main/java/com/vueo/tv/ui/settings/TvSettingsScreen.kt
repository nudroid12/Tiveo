package com.vueo.tv.settings

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.platform.LocalContext
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
import com.vueo.tv.core.TvRuntime
import com.vueo.tv.home.TiveoTopBarState
import com.vueo.tv.ui.TvDesign
import com.vueo.tv.ui.TvSidebarPreferences
import com.vueo.tv.ui.TvSidebarStyle
import com.vueo.tv.update.TvUpdateManager
import com.vueo.tv.update.TvUpdateRelease
import kotlinx.coroutines.launch
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal enum class TvSettingsPage {
    PROFILE,
    VUEO_SYNC,
    PERSONALIZATION,
    CONTENT_MANAGER,
    CONTENT_ADDONS,
    CONTENT_PROVIDERS,
    CONTENT_PROVIDER_HEALTH,
    CONTENT_CATALOGS,
    CONTENT_COLLECTIONS,
    ENHANCEMENTS,
    ENHANCEMENT_TRAKT,
    ENHANCEMENT_TMDB,
    ENHANCEMENT_MDBLIST,
    ENHANCEMENT_GEMINI,
    PLAYBACK,
    SUBTITLES,
    SOURCES,
    APPEARANCE,
    DEVELOPER_OPTIONS,
    MOTION,
    DATA_STORAGE,
    DIAGNOSTICS,
    ABOUT,
}

private data class TvSettingsRootDestination(
    val page: TvSettingsPage,
    val id: String,
    val title: String,
    val section: String? = null,
)

private val TvSettingsRootDestinations = listOf(
    TvSettingsRootDestination(TvSettingsPage.VUEO_SYNC, "vueo-sync", "Account", "Tiveo"),
    TvSettingsRootDestination(TvSettingsPage.PROFILE, "profile", "Profile", "Tiveo"),
    TvSettingsRootDestination(TvSettingsPage.CONTENT_MANAGER, "content-manager", "Content Manager", "Tiveo"),
    TvSettingsRootDestination(TvSettingsPage.ENHANCEMENTS, "enhancements", "Enhancements", "Tiveo"),
    TvSettingsRootDestination(TvSettingsPage.PLAYBACK, "playback", "Playback", "PLAYBACK"),
    TvSettingsRootDestination(TvSettingsPage.SUBTITLES, "subtitles", "Subtitles", "PLAYBACK"),
    TvSettingsRootDestination(TvSettingsPage.SOURCES, "sources", "Sources", "PLAYBACK"),
    TvSettingsRootDestination(TvSettingsPage.APPEARANCE, "appearance", "Appearance", "APP"),
    TvSettingsRootDestination(TvSettingsPage.DATA_STORAGE, "data-storage", "Data & Storage", "APP"),
    TvSettingsRootDestination(TvSettingsPage.ABOUT, "about", "About Tiveo", "APP"),
    TvSettingsRootDestination(TvSettingsPage.DEVELOPER_OPTIONS, "developer-options", "Developer options", "APP"),
)

private const val DEVELOPER_OPTIONS_ENABLED = "developer_options_enabled"
private const val HIDDEN_SETTINGS_CATEGORIES = "developer_hidden_settings_categories"

private val HideableSettingsPages = setOf(
    TvSettingsPage.PROFILE,
    TvSettingsPage.CONTENT_MANAGER,
    TvSettingsPage.ENHANCEMENTS,
    TvSettingsPage.DATA_STORAGE,
    TvSettingsPage.VUEO_SYNC,
)

private fun TvSettingsPage.rootPage(): TvSettingsPage = when (this) {
    TvSettingsPage.CONTENT_ADDONS,
    TvSettingsPage.CONTENT_PROVIDERS,
    TvSettingsPage.CONTENT_PROVIDER_HEALTH,
    TvSettingsPage.CONTENT_COLLECTIONS,
    TvSettingsPage.CONTENT_CATALOGS -> TvSettingsPage.CONTENT_MANAGER
    TvSettingsPage.ENHANCEMENT_TRAKT,
    TvSettingsPage.ENHANCEMENT_TMDB,
    TvSettingsPage.ENHANCEMENT_MDBLIST,
    TvSettingsPage.ENHANCEMENT_GEMINI,
    TvSettingsPage.PERSONALIZATION -> TvSettingsPage.ENHANCEMENTS
    TvSettingsPage.MOTION, TvSettingsPage.DIAGNOSTICS -> TvSettingsPage.DEVELOPER_OPTIONS
    else -> this
}

private fun TvSettingsPage.hasPanelParent(): Boolean = when (this) {
    TvSettingsPage.CONTENT_ADDONS,
    TvSettingsPage.CONTENT_PROVIDERS,
    TvSettingsPage.CONTENT_PROVIDER_HEALTH,
    TvSettingsPage.CONTENT_COLLECTIONS,
    TvSettingsPage.CONTENT_CATALOGS,
    TvSettingsPage.ENHANCEMENT_TRAKT,
    TvSettingsPage.ENHANCEMENT_TMDB,
    TvSettingsPage.ENHANCEMENT_MDBLIST,
    TvSettingsPage.ENHANCEMENT_GEMINI,
    TvSettingsPage.PERSONALIZATION,
    TvSettingsPage.MOTION,
    TvSettingsPage.DIAGNOSTICS -> true
    else -> false
}

@Composable
internal fun TvSettingsScreen(
    runtime: TvRuntime,
    active: Boolean = true,
    topBar: TiveoTopBarState,
    onNavigate: (String) -> Unit,
    onProfile: () -> Unit,
    onBack: () -> Unit,
    onManageProfiles: () -> Unit,
    dataVersion: Int,
    onDataChanged: () -> Unit = {},
    onResetComplete: () -> Unit = {},
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.applicationContext.getSharedPreferences("vueo_tv_browse", Context.MODE_PRIVATE)
    }
    var developerOptionsEnabled by remember(preferences) {
        mutableStateOf(preferences.getBoolean(DEVELOPER_OPTIONS_ENABLED, false))
    }
    var hiddenCategories by remember(preferences) {
        mutableStateOf(preferences.getStringSet(HIDDEN_SETTINGS_CATEGORIES, emptySet()).orEmpty().toSet())
    }
    val visibleRoots = remember(developerOptionsEnabled, hiddenCategories) {
        TvSettingsRootDestinations.filter { destination ->
            if (destination.page == TvSettingsPage.DEVELOPER_OPTIONS) {
                developerOptionsEnabled
            } else {
                destination.page !in HideableSettingsPages || destination.id !in hiddenCategories
            }
        }
    }
    var page by remember {
        mutableStateOf(visibleRoots.firstOrNull { it.page == TvSettingsPage.PROFILE }?.page ?: visibleRoots.first().page)
    }
    var panelAutoFocusToken by remember { mutableIntStateOf(0) }

    val selectedRoot = visibleRoots.firstOrNull { it.page == page.rootPage() } ?: visibleRoots.first()
    val visiblePage = if (page.rootPage() == selectedRoot.page) page else selectedRoot.page

    fun setDeveloperOptions(enabled: Boolean) {
        developerOptionsEnabled = enabled
        preferences.edit().putBoolean(DEVELOPER_OPTIONS_ENABLED, enabled).apply()
    }

    fun setCategoryHidden(id: String, hidden: Boolean) {
        val updated = if (hidden) hiddenCategories + id else hiddenCategories - id
        hiddenCategories = updated
        preferences.edit().putStringSet(HIDDEN_SETTINGS_CATEGORIES, updated).apply()
    }

    fun openPanel(next: TvSettingsPage) {
        page = next
        panelAutoFocusToken += 1
    }

    fun backPanel() {
        page = when (visiblePage) {
            TvSettingsPage.CONTENT_PROVIDER_HEALTH -> TvSettingsPage.CONTENT_PROVIDERS
            TvSettingsPage.CONTENT_ADDONS,
            TvSettingsPage.CONTENT_PROVIDERS,
            TvSettingsPage.CONTENT_COLLECTIONS,
            TvSettingsPage.CONTENT_CATALOGS -> TvSettingsPage.CONTENT_MANAGER
            TvSettingsPage.ENHANCEMENT_TRAKT,
            TvSettingsPage.ENHANCEMENT_TMDB,
            TvSettingsPage.ENHANCEMENT_MDBLIST,
            TvSettingsPage.ENHANCEMENT_GEMINI,
            TvSettingsPage.PERSONALIZATION -> TvSettingsPage.ENHANCEMENTS
            TvSettingsPage.MOTION, TvSettingsPage.DIAGNOSTICS -> TvSettingsPage.DEVELOPER_OPTIONS
            else -> visiblePage
        }
        panelAutoFocusToken += 1
    }

    TvSettingsMasterDetailShell(
        active = active,
        categories = visibleRoots.map { TvSettingsNavItem(it.id, it.title, it.section) },
        selectedCategoryId = selectedRoot.id,
        panelKey = visiblePage.name,
        panelAutoFocusToken = panelAutoFocusToken,
        panelHasBack = visiblePage.hasPanelParent(),
        onCategorySelected = { id ->
            visibleRoots.firstOrNull { it.id == id }?.let { destination ->
                if (page != destination.page) page = destination.page
            }
        },
        onPanelBack = ::backPanel,
        topBar = topBar,
        onBack = onBack,
    ) {
        when (visiblePage) {
            TvSettingsPage.VUEO_SYNC -> TvVueoSyncSettings(onNavigate, onProfile, onBack)
            TvSettingsPage.PROFILE -> TvProfileSettings(
                runtime = runtime,
                onOpenDna = onProfile,
                onOpenProfiles = onManageProfiles,
                dataVersion = dataVersion,
            )
            TvSettingsPage.PERSONALIZATION -> TvPersonalizationSettings(
                runtime, onNavigate, onProfile, ::backPanel
            )
            TvSettingsPage.CONTENT_MANAGER -> TvContentManagerHub(
                runtime, onNavigate, onProfile, ::openPanel, onBack
            )
            TvSettingsPage.CONTENT_ADDONS -> TvAddonSettings(
                runtime, onNavigate, onProfile, onDataChanged, ::backPanel
            )
            TvSettingsPage.CONTENT_PROVIDERS -> TvProviderSettings(
                runtime = runtime,
                onNavigate = onNavigate,
                onProfile = onProfile,
                onDataChanged = onDataChanged,
                onOpenProviderHealth = {
                    openPanel(TvSettingsPage.CONTENT_PROVIDER_HEALTH)
                },
                onBack = ::backPanel,
            )
            TvSettingsPage.CONTENT_PROVIDER_HEALTH -> TvProviderHealthOverview(
                runtime, onNavigate, onProfile, ::backPanel
            )
            TvSettingsPage.CONTENT_COLLECTIONS -> com.vueo.shared.collections.ui.CollectionManagerScreen(
                engine = runtime.engine, profileId = runtime.profileStore.activeProfileId(), onBack = ::backPanel,
                image = { url, label, modifier -> com.vueo.tv.ui.TvNetworkImage(url, label, modifier) },
            )
            TvSettingsPage.CONTENT_CATALOGS -> TvCatalogSettings(
                runtime, onNavigate, onProfile, onDataChanged, ::backPanel
            )
            TvSettingsPage.ENHANCEMENTS -> TvEnhancementSettings(
                runtime, onNavigate, onProfile, ::openPanel, onBack
            )
            TvSettingsPage.ENHANCEMENT_TRAKT -> TvTraktEnhancementSettings(
                onNavigate, onProfile, ::backPanel
            )
            TvSettingsPage.ENHANCEMENT_TMDB -> TvTmdbEnhancementSettings(
                runtime, onNavigate, onProfile, ::backPanel
            )
            TvSettingsPage.ENHANCEMENT_MDBLIST -> TvMdblistEnhancementSettings(
                runtime, onNavigate, onProfile, ::backPanel
            )
            TvSettingsPage.ENHANCEMENT_GEMINI -> TvGeminiEnhancementSettings(
                onNavigate, onProfile, ::backPanel
            )
            TvSettingsPage.PLAYBACK -> TvPlaybackSettings(
                runtime, onNavigate, onProfile, onBack
            )
            TvSettingsPage.SUBTITLES -> TvSubtitleSettings(
                runtime, onNavigate, onProfile, onBack
            )
            TvSettingsPage.SOURCES -> TvSourceSettings(
                runtime, onNavigate, onProfile, onBack
            )
            TvSettingsPage.APPEARANCE -> TvAppearanceSettings(
                runtime, onNavigate, onProfile, onBack
            )
            TvSettingsPage.DEVELOPER_OPTIONS -> {
                val entries = buildList {
                    add(TvSettingsEntry(
                        id = "developer-options-enabled",
                        title = "Developer options",
                        subtitle = "Turn off to hide this menu. Press Build number 7 times to open it again.",
                        onActivate = {
                            setDeveloperOptions(false)
                            openPanel(TvSettingsPage.ABOUT)
                        },
                        switchChecked = true,
                    ))
                    TvSettingsRootDestinations.filter { it.page in HideableSettingsPages }.forEach { destination ->
                        val hidden = destination.id in hiddenCategories
                        add(TvSettingsEntry(
                            id = "hide-${destination.id}",
                            title = "Hide ${destination.title}",
                            subtitle = "Hide this category from Settings.",
                            section = "SETTINGS VISIBILITY",
                            onActivate = { setCategoryHidden(destination.id, !hidden) },
                            switchChecked = hidden,
                        ))
                    }
                    add(TvSettingsEntry(
                        id = "motion-tuning",
                        title = "Motion Tuning",
                        subtitle = "Adjust TV animations and transitions.",
                        section = "TOOLS",
                        onActivate = { openPanel(TvSettingsPage.MOTION) },
                    ))
                    add(TvSettingsEntry(
                        id = "diagnostics",
                        title = "Diagnostics",
                        subtitle = "View crash, performance and provider diagnostics.",
                        section = "TOOLS",
                        onActivate = { openPanel(TvSettingsPage.DIAGNOSTICS) },
                    ))
                }
                TvSettingsListScreen(
                    "Developer options", "Customize what appears in Settings and access TV tools.",
                    entries, onNavigate, onProfile, onBack,
                )
            }
            TvSettingsPage.MOTION -> TvMotionSettings(onNavigate, onProfile, ::backPanel)
            TvSettingsPage.DATA_STORAGE -> TvDataStorageSettings(
                runtime, onNavigate, onProfile, onDataChanged, onResetComplete, onBack
            )
            TvSettingsPage.DIAGNOSTICS -> TvDiagnosticsSettings(
                onNavigate = onNavigate,
                onProfile = onProfile,
                onBack = ::backPanel,
            )
            TvSettingsPage.ABOUT -> TvAboutSettings(
                onNavigate = onNavigate,
                onProfile = onProfile,
                onBack = onBack,
                developerOptionsEnabled = developerOptionsEnabled,
                onUnlockDeveloperOptions = { setDeveloperOptions(true) },
            )
        }
    }
}
