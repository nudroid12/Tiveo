package com.vueo.mobile.ui

import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults

import com.vueo.shared.core.storage.RecommendationEngine
import com.vueo.shared.core.diagnostics.PerformanceDiagnostics
import com.vueo.shared.core.diagnostics.PerformanceDiagnostics.Tab
import android.app.Activity
import android.net.Uri
import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.graphics.Typeface
import android.util.TypedValue
import android.os.Build
import android.os.SystemClock
import android.widget.Toast
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ForwardingRenderer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.text.TextOutput
import androidx.media3.common.C
import androidx.media3.common.AudioAttributes
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.MediaItem as Media3MediaItem
import com.vueo.mobile.core.extensions.AddonCategory
import com.vueo.mobile.core.extensions.ExtensionInstaller
import com.vueo.mobile.core.extensions.primaryAddonCategory
import com.vueo.mobile.core.extensions.ExtensionKind
import com.vueo.mobile.core.extensions.MediaExtension
import com.vueo.mobile.core.extensions.UnifiedMediaEngine
import com.vueo.mobile.core.extensions.SourceRanker
import com.vueo.mobile.core.extensions.SourceDiscoveryCache
import com.vueo.mobile.core.extensions.CatalogDiscoveryCache
import com.vueo.mobile.core.enrichment.MdblistClient
import com.vueo.mobile.core.enrichment.MediaRating
import com.vueo.mobile.core.enrichment.TmdbEnhancementClient
import com.vueo.shared.core.enrichment.ContentWarning
import com.vueo.shared.core.enrichment.ContentWarningRepository
import com.vueo.shared.core.detail.DetailPeoplePolicy
import com.vueo.shared.core.detail.DetailUpstreamPolicy
import com.vueo.shared.core.home.HomeCatalogPolicy
import com.vueo.shared.core.home.HomeRecommendationSections
import com.vueo.shared.core.search.DiscoverCatalogPolicy
import com.vueo.shared.core.search.DiscoverSortMode
import com.vueo.shared.core.search.SearchResultOrderPolicy
import com.vueo.shared.core.search.SearchMediaFilter
import com.vueo.shared.core.recommendation.RelatedContentOrchestrator
import com.vueo.shared.core.search.SearchOrchestrator
import com.vueo.shared.core.search.MediaEntityKind
import com.vueo.shared.core.search.MediaEntityTarget
import com.vueo.shared.core.source.SourceDiscoveryEngine
import com.vueo.shared.core.source.SourceDiscoveryRequest
import com.vueo.mobile.core.dna.UserDnaEngine
import com.vueo.mobile.core.dna.UserDnaPreferences
import com.vueo.mobile.core.model.CatalogRow
import com.vueo.mobile.BuildConfig
import com.vueo.mobile.R
import com.vueo.mobile.core.storage.PlaybackStore
import com.vueo.shared.core.storage.ContinueWatchingMetadataRefresh
import com.vueo.mobile.core.storage.LibraryStore
import com.vueo.mobile.core.storage.ProfileStore
import com.vueo.mobile.core.storage.VueoProfile
import com.vueo.mobile.core.storage.LibraryPlaybackEntry
import com.vueo.mobile.core.storage.PreferredQuality
import com.vueo.mobile.core.storage.PlayerOrientation
import com.vueo.mobile.core.storage.PlayerVideoFit
import com.vueo.mobile.core.storage.SettingsStore
import com.vueo.mobile.core.player.PlayerSkipKind
import com.vueo.mobile.core.player.PlayerSkipRepository
import com.vueo.mobile.core.player.PlayerSkipSegment
import com.vueo.mobile.core.player.PlayerPlaybackPhase
import com.vueo.mobile.core.player.PlayerSourceAssessment
import com.vueo.mobile.core.player.PlayerSourceAudioMatch
import com.vueo.mobile.core.player.PlayerSourcePolicy
import com.vueo.shared.core.player.PlayerTrackPolicy
import com.vueo.mobile.core.player.PlayerSourceRecoverySession
import com.vueo.mobile.core.player.PLAYER_REBUFFER_TIMEOUT_MS
import com.vueo.mobile.core.player.PLAYER_RECOVERY_SOURCE_TIMEOUT_MS
import com.vueo.mobile.core.player.PLAYER_STARTUP_TIMEOUT_MS
import com.vueo.mobile.core.storage.VueoDataMigration
import com.vueo.mobile.core.update.VueoUpdateManager
import com.vueo.mobile.core.model.SubtitleTrack
import com.vueo.mobile.core.plugin.PluginStore
import com.vueo.mobile.core.plugin.ProviderCodeSyncManager
import com.vueo.mobile.core.plugin.ProviderCodeStore
import com.vueo.mobile.core.plugin.ProviderHealthStatus
import com.vueo.mobile.core.plugin.ProviderHealthRecord
import com.vueo.mobile.core.plugin.PluginHealthStore
import com.vueo.mobile.core.plugin.PluginSourceEngine
import com.vueo.mobile.core.plugin.PluginRepositoryDescriptor
import com.vueo.mobile.core.plugin.PluginRepositoryClient
import com.vueo.mobile.core.model.EpisodeItem
import com.vueo.mobile.core.model.MediaCompany
import com.vueo.mobile.core.model.MediaItem
import com.vueo.mobile.core.model.MediaPerson
import com.vueo.mobile.core.model.StreamSource
import com.vueo.mobile.core.storage.AddonStore
import com.vueo.mobile.ui.components.NetworkImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

internal class MobileHomeRetainedState {
    var catalogRows by mutableStateOf(
        CatalogDiscoveryCache
            .home(allowStale = true)
            .orEmpty()
    )
        internal set

    var loadedContentVersion by mutableIntStateOf(Int.MIN_VALUE)
        internal set

    var featuredMediaKey by mutableStateOf<String?>(null)
        internal set

    // Keep the expensive presentation layer warm across bottom-tab switches.
    // Catalog rows were already retained; v114 extends the same idea to the
    // featured strip, Continue Watching and recommendation sections so Home can
    // paint immediately while fresh library state hydrates off Main.
    var featuredItems by mutableStateOf<List<MediaItem>>(emptyList())
        internal set

    var continueWatching by mutableStateOf<List<LibraryPlaybackEntry>>(emptyList())
        internal set

    var recommendationEngine = RecommendationEngine.TMDB
        internal set

    var recommendations by mutableStateOf(HomeRecommendationSections())
        internal set

    var presentationProfileId by mutableStateOf<String?>(null)
        internal set
}

private fun mergeHomeCatalogRows(
    baseline: List<CatalogRow>,
    fresh: List<CatalogRow>,
): List<CatalogRow> {
    if (baseline.isEmpty()) return fresh
    if (fresh.isEmpty()) return baseline

    val freshById = fresh.associateBy { it.id }

    return baseline.map { row ->
        freshById[row.id] ?: row
    } + fresh.filter { freshRow ->
        baseline.none { it.id == freshRow.id }
    }
}

private fun MediaItem.homeFeaturedKey(): String =
    "${type.lowercase()}:$id"

@Composable
internal fun HomeScreen(
    engine: UnifiedMediaEngine,
    retainedState: MobileHomeRetainedState,
    activeProfileId: String,
    contentVersion: Int,
    booting: Boolean,
    addonsPrepared: Boolean,
    libraryStore: LibraryStore,
    libraryVersion: Int,
    onLibraryChanged: () -> Unit,
    onOpenContentManager: () -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    onPlaybackClick:
        (LibraryPlaybackEntry) -> Unit,
    onOpenCollection: (String, String) -> Unit,
    onSeeAll: (CatalogRow) -> Unit,
    profileAvatar: @Composable () -> Unit,
    onPlayFeatured: (MediaItem) -> Unit,
) {
    val context =
        LocalContext.current
    val collectionHomeState = com.vueo.shared.collections.ui.rememberCollectionHomeState(activeProfileId)
    val trakt = remember { com.vueo.shared.core.trakt.TraktService.get(context) }
    val traktProfile = trakt.profileId()
    val traktRevision by trakt.catalogRevision.collectAsState()
    val traktLibraryRevision by trakt.libraryRevision.collectAsState()
    var traktRows by remember(traktProfile) { mutableStateOf(trakt.cachedHomeRows(traktProfile)) }
    LaunchedEffect(traktProfile, contentVersion) { trakt.scheduleSync(traktProfile) }
    LaunchedEffect(traktProfile, traktRevision, contentVersion) { traktRows = trakt.homeRows(traktProfile) }
    LaunchedEffect(traktProfile, traktLibraryRevision) {
        if (traktLibraryRevision > 0L) onLibraryChanged()
    }
    val recommendationSettings = remember { SettingsStore(context.applicationContext) }
    val recommendationPlugins = remember { PluginStore(context.applicationContext) }
    val recommendationEngine = recommendationSettings.recommendationEngine()
    val recommendationTraktClientId = recommendationSettings.traktClientId()
    val recommendationKey = recommendationPlugins.tmdbApiKey()
    val recommendationTmdbEnabled = recommendationSettings.tmdbRecommendationsEnabled()
    val recommendationSimilarEnabled = recommendationSettings.tmdbSimilarTitlesEnabled()
    val homeAddonStore =
        remember {
            AddonStore(
                context.applicationContext
            )
        }

    val catalogOrder =
        remember(
            contentVersion
        ) {
            homeAddonStore
                .catalogOrder()
        }

    val disabledCatalogKeys =
        remember(
            contentVersion
        ) {
            homeAddonStore
                .disabledCatalogKeys()
        }

    fun arrangeHomeRows(catalogRows: List<CatalogRow>): List<CatalogRow> {
        val activeKeys = HomeCatalogPolicy.defaultOrder(engine.activeStremioAddons()).toSet()
        val disabledAddons = engine.stremioAddons()
            .filterNot { engine.isExtensionEnabled(it.descriptor.installationId) }.map { it.descriptor.installationId }
        val validRows = catalogRows.filter { row ->
            if (addonsPrepared) row.id in activeKeys
            else com.vueo.shared.core.extensions.AddonInstallationIdentity.isScopedCatalogKey(row.id) && disabledAddons.none { row.id.startsWith("$it:") }
        }
        return HomeCatalogPolicy.orderRows(
            rows = engine.filterVisibleRows(validRows),
            catalogOrder = homeAddonStore.catalogOrder(),
            disabledCatalogKeys = homeAddonStore.disabledCatalogKeys(),
            defaultCatalogOrder = HomeCatalogPolicy.defaultOrder(engine.stremioAddons()),
            random = homeAddonStore.randomCatalogOrder(),
        )
    }

    val profileStore =
        remember {
            ProfileStore(
                context.applicationContext
            )
        }

    val dnaPreferences =
        remember {
            UserDnaPreferences(
                context.applicationContext
            )
        }

    val dnaEngine =
        remember(
            libraryStore
        ) {
            UserDnaEngine(
                libraryStore
            )
        }

    val forYouEnabled = dnaPreferences.shouldShowForYou(activeProfileId)
    val becauseEnabled = dnaPreferences.shouldShowBecauseYouWatched(activeProfileId)

    val personalizedHomeEnabled =
        dnaPreferences
            .shouldPersonalizeRecommendations(
                activeProfileId
            )

    // The root profile-scoped SaveableStateProvider retains this position and
    // each LazyRow's default rememberLazyListState across route/tab disposal.
    val listState = rememberLazyListState()

    var rows by remember(
        contentVersion,
        catalogOrder,
        disabledCatalogKeys,
    ) {
        val restoredRows =
            retainedState.catalogRows
                .ifEmpty {
                    CatalogDiscoveryCache
                        .home(
                            allowStale = true
                        )
                        .orEmpty()
                }

        mutableStateOf(
            arrangeHomeRows(restoredRows)
        )
    }

    LaunchedEffect(rows) {
        retainedState.catalogRows = rows
    }

    // Metadata repair is optional background work. Wait until Home has actual
    // content and give first paint a short head start so cold startup is not
    // competing with extra metadata requests/profile writes.
    LaunchedEffect(
        engine,
        contentVersion,
        activeProfileId,
        booting,
        addonsPrepared,
        rows.isNotEmpty(),
    ) {
        if (booting || !addonsPrepared || rows.isEmpty()) return@LaunchedEffect
        delay(900L)
        if (ContinueWatchingMetadataRefresh.refresh(libraryStore, engine::loadMeta)) {
            onLibraryChanged()
        }
    }

    var loading by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf<String?>(
            null
        )
    }

    var continueWatchingAction by remember {
        mutableStateOf<
            LibraryPlaybackEntry?
        >(null)
    }

    var catalogAction by remember {
        mutableStateOf<MediaItem?>(
            null
        )
    }

    var continueWatchingActionAnchor by remember {
        mutableStateOf(
            IntOffset.Zero
        )
    }

    var catalogActionAnchor by remember {
        mutableStateOf(
            IntOffset.Zero
        )
    }

    LaunchedEffect(
        contentVersion,
        addonsPrepared,
    ) {
        val cachedRows =
            CatalogDiscoveryCache
                .home(
                    allowStale = true
                )
                .orEmpty()

        if (cachedRows.isNotEmpty()) {
            rows =
                arrangeHomeRows(cachedRows)
        }

        if (booting) {
            loading = false
            return@LaunchedEffect
        }

        if (!addonsPrepared) {
            // Remote manifests are deliberately outside the app boot gate.
            // Cached rows remain visible, while a cache miss keeps a lightweight
            // Home loading state until the configured addon set is ready.
            loading = rows.isEmpty()
            return@LaunchedEffect
        }

        val hasFreshCache =
            !CatalogDiscoveryCache
                .home(
                    allowStale = false
                )
                .isNullOrEmpty()

        val shouldRefresh =
            retainedState.loadedContentVersion != contentVersion ||
                !hasFreshCache

        if (!shouldRefresh) {
            loading = false
            error = null
            return@LaunchedEffect
        }

        loading =
            rows.isEmpty()
        error = null

        val refreshBaseline = rows

        runCatching {
            engine.loadCatalogRows(
                forceRefresh =
                    refreshBaseline.isNotEmpty(),
                catalogOrder =
                    catalogOrder,
                disabledCatalogKeys =
                    disabledCatalogKeys,
                onPartial = { partialRows ->
                    if (partialRows.isNotEmpty()) {
                        rows =
                            arrangeHomeRows(
                                mergeHomeCatalogRows(baseline = refreshBaseline, fresh = partialRows),
                            )
                        loading = false
                    }
                },
            )
        }.onSuccess {
            fresh ->
            rows = arrangeHomeRows(fresh)
            if (fresh.isNotEmpty() && profileStore.kidsAgeLimit() == null) {
                CatalogDiscoveryCache
                    .persistHome(
                        context =
                            context
                                .applicationContext,
                        rows = fresh,
                    )
            }

            retainedState.loadedContentVersion =
                contentVersion
        }.onFailure {
            failure ->
            if (failure is CancellationException) {
                throw failure
            }

            error =
                failure.message

            if (
                rows.isEmpty()
            ) {
                rows =
                    arrangeHomeRows(CatalogDiscoveryCache.home(allowStale = true).orEmpty())
            }

            retainedState.loadedContentVersion =
                contentVersion
        }

        loading = false
    }

    var featuredItems by remember {
        mutableStateOf(retainedState.featuredItems)
    }

    LaunchedEffect(rows) {
        val freshFeatured = PerformanceDiagnostics.measuredContext(Dispatchers.Default, "mobile_home_presentation_1", Tab.HOME) {
            val allItems =
                rows
                    .asSequence()
                    .flatMap {
                        it.items
                            .asSequence()
                    }
                    .distinctBy {
                        "${it.type}:${it.id}"
                    }
                    .toList()

            val withBackdrop =
                allItems
                    .asSequence()
                    .filter {
                        !it.background
                            .isNullOrBlank()
                    }
                    .take(7)
                    .toList()

            if (withBackdrop.isNotEmpty()) {
                withBackdrop
            } else {
                allItems.take(7)
            }
        }

        featuredItems = freshFeatured
        retainedState.featuredItems = freshFeatured
    }

    var continueWatching by remember(activeProfileId) {
        mutableStateOf(
            if (retainedState.presentationProfileId == activeProfileId) {
                retainedState.continueWatching
            } else {
                emptyList()
            }
        )
    }

    var homeRecommendations by remember(
        activeProfileId,
        personalizedHomeEnabled,
        recommendationEngine,
    ) {
        mutableStateOf(
            if (
                personalizedHomeEnabled &&
                retainedState.presentationProfileId == activeProfileId
            ) {
                if (retainedState.recommendationEngine == recommendationEngine) retainedState.recommendations
                else HomeRecommendationSections()
            } else {
                HomeRecommendationSections()
            }
        )
    }

    // Library hydration is independent of streaming catalog rows.
    var recommendationSnapshot by remember(activeProfileId) {
        mutableStateOf<com.vueo.shared.core.storage.LibraryDetailSnapshot?>(null)
    }
    LaunchedEffect(libraryVersion, activeProfileId) {
        val snapshot = PerformanceDiagnostics.measuredContext(Dispatchers.IO, "mobile_home_library_snapshot_2", Tab.HOME) {
            libraryStore.detailSnapshot()
        }
        continueWatching = snapshot.continueWatching
        retainedState.continueWatching = snapshot.continueWatching
        retainedState.presentationProfileId = activeProfileId
        recommendationSnapshot = snapshot
    }
    val ratingRevision by com.vueo.shared.core.storage.RatingChanges.revision.collectAsState()
    LaunchedEffect(ratingRevision, recommendationSnapshot, activeProfileId, personalizedHomeEnabled,
        recommendationEngine, recommendationKey, recommendationTmdbEnabled,
        recommendationSimilarEnabled, recommendationTraktClientId, forYouEnabled, becauseEnabled) {
        val snapshot = recommendationSnapshot ?: return@LaunchedEffect
        if (!personalizedHomeEnabled) {
            com.vueo.shared.core.home.HomeRecommendationFeeds.cancelProfile(activeProfileId)
            homeRecommendations = HomeRecommendationSections()
            retainedState.recommendations = homeRecommendations
            return@LaunchedEffect
        }
        val request = com.vueo.shared.core.home.HomeRecommendationFeeds.Request(activeProfileId,
            snapshot.history, snapshot.watchlist, recommendationEngine, recommendationKey,
            recommendationTraktClientId, recommendationTmdbEnabled, recommendationSimilarEnabled, forYouEnabled, becauseEnabled)
        val feed = com.vueo.shared.core.home.HomeRecommendationFeeds.observe(context, request)
        feed.collect { sections ->
            homeRecommendations = sections
            retainedState.recommendations = sections
            retainedState.recommendationEngine = recommendationEngine
        }
    }

    val visibleFeaturedItems = engine.filterVisible(featuredItems)
    val catalogRows = engine.filterVisibleRows(rows + traktRows)
    val savedFeatured = remember(visibleFeaturedItems, libraryVersion) {
        visibleFeaturedItems.filter(libraryStore::isWatchlisted).map { it.homeFeaturedKey() }.toSet()
    }
    val forYouItems = if (forYouEnabled) engine.filterVisible(homeRecommendations.forYou) else emptyList()
    val becauseYouWatchedSeed = if (becauseEnabled) homeRecommendations.becauseYouWatchedSeed?.takeIf(engine::allowsContent) else null
    val becauseYouWatchedItems = if (becauseEnabled) engine.filterVisible(homeRecommendations.becauseYouWatched) else emptyList()

        LazyColumn(
        state = listState,
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color(0xFF0B0B0B)
                ),
        contentPadding =
            PaddingValues(
                bottom = 24.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                16.dp
            ),
    ) {
        item(key = "home_header") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.virio_wordmark),
                    contentDescription = "Tiveo",
                    modifier = Modifier.width(120.dp).height(24.dp),
                    contentScale = ContentScale.Fit,
                )
                profileAvatar()
            }
        }
        if (
            visibleFeaturedItems
                .isNotEmpty()
        ) {
            item(
                key =
                    "home_featured"
            ) {
                HomeFeaturedCarousel(
                    items =
                        visibleFeaturedItems,
                    selectedMediaKey =
                        retainedState.featuredMediaKey,
                    onSelectedMediaKeyChange = {
                        retainedState.featuredMediaKey = it
                    },
                    isSaved = { it.homeFeaturedKey() in savedFeatured },
                    onToggleSaved = {
                        libraryStore.toggleWatchlist(it)
                        onLibraryChanged()
                    },
                    onViewDetails = onMediaClick,
                    onPlay = onPlayFeatured,
                )
            }
        }

        if (
            (booting || !addonsPrepared || loading) &&
            rows.isEmpty()
        ) {
            item(
                key =
                    "home_loading"
            ) {
                HomeLoadingState()
            }
        }

        if (
            !booting &&
            addonsPrepared &&
            !loading &&
            rows.isEmpty()
        ) {
            item(
                key =
                    "home_empty"
            ) {
                EmptyHomeCard(
                    hasAddons =
                        engine
                            .stremioAddons()
                            .isNotEmpty(),
                    error = error,
                    onOpenContentManager =
                        onOpenContentManager,
                )
            }
        }

        if (
            continueWatching.isNotEmpty()
        ) {
            item(
                key =
                    "continue_watching"
            ) {
                HomeContinueWatchingSection(
                    entries =
                        continueWatching,
                    onPlaybackClick =
                        onPlaybackClick,
                    onHold = {
                        entry,
                        anchor ->
                        continueWatchingAction =
                            entry
                        continueWatchingActionAnchor =
                            anchor
                    },
                )
            }
        }

        if (
        forYouItems.size >= 4
    ) {
        item(
            key =
                "home_for_you"
        ) {
            HomePersonalizedSection(
                title =
                    "For You",
                contextLabel =
                    "Your DNA",
                items =
                    forYouItems,
                onMediaClick =
                    onMediaClick,
                onMediaHold = {
                    item,
                    anchor ->
                    catalogAction = item
                    catalogActionAnchor = anchor
                },
            )
        }
    }

    if (
        becauseYouWatchedSeed !=
            null &&
        becauseYouWatchedItems
            .size >= 4
    ) {
        item(
            key =
                "home_because_you_watched"
        ) {
            HomePersonalizedSection(
                title =
                    "Because You Watched " +
                        becauseYouWatchedSeed
                            .name,
                contextLabel =
                    "Recent viewing",
                items =
                    becauseYouWatchedItems,
                onMediaClick =
                    onMediaClick,
                onMediaHold = {
                    item,
                    anchor ->
                    catalogAction = item
                    catalogActionAnchor = anchor
                },
            )
        }
    }

        collectionHomeState.error?.let { error ->
            item(key = "user-collections-error") { Text(error, color = VueoPalette.Muted, modifier = Modifier.padding(16.dp)) }
        }
        items(collectionHomeState.collections.filter { it.enabled && it.folders.isNotEmpty() }, key = { "collection:${it.id}" }) { collection ->
            com.vueo.shared.collections.ui.CollectionHomeRow(
                collection = collection,
                image = { url, label, modifier -> NetworkImage(url, label, modifier) },
                onOpen = onOpenCollection,
            )
        }
            items(
            items = catalogRows,
            key = {
                "catalog:${it.id}"
            },
        ) {
            row ->
            CatalogSection(
                row = row,
                onMediaClick =
                    onMediaClick,
                onMediaHold = {
                    item,
                    anchor ->
                    catalogAction = item
                    catalogActionAnchor = anchor
                },
                onSeeAll = {
                    onSeeAll(row)
                },
            )
        }
    }

    continueWatchingAction?.let {
        entry ->
        ContinueWatchingActionsDialog(
            entry = entry,
            anchor =
                continueWatchingActionAnchor,
            onDismiss = {
                continueWatchingAction =
                    null
            },
            onResume = {
                continueWatchingAction =
                    null
                onPlaybackClick(
                    entry
                )
            },
            onViewDetails = {
                continueWatchingAction =
                    null
                onMediaClick(
                    entry.media
                )
            },
            onRemove = {
                libraryStore
                    .removeFromContinueWatching(
                        entry
                    )
                continueWatchingAction =
                    null
                onLibraryChanged()
            },
        )
    }

    catalogAction?.let {
        item ->
        CatalogActionsDialog(
            item = item,
            anchor =
                catalogActionAnchor,
            isWatchlisted =
                libraryStore
                    .isWatchlisted(
                        item
                    ),
            isMarkedWatched =
                libraryStore
                    .isMarkedWatched(
                        item
                    ),
            onDismiss = {
                catalogAction = null
            },
            onViewDetails = {
                catalogAction = null
                onMediaClick(item)
            },
            onToggleWatchlist = {
                libraryStore
                    .toggleWatchlist(
                        item
                    )
                catalogAction = null
                onLibraryChanged()
            },
            onToggleWatched = {
                libraryStore
                    .setMarkedWatched(
                        media = item,
                        watched =
                            !libraryStore
                                .isMarkedWatched(
                                    item
                                ),
                    )
                catalogAction = null
                onLibraryChanged()
            },
        )
    }
}

@Composable
private fun HomeFeaturedCarousel(
    items: List<MediaItem>,
    selectedMediaKey: String?,
    onSelectedMediaKeyChange: (String) -> Unit,
    isSaved: (MediaItem) -> Boolean,
    onToggleSaved: (MediaItem) -> Unit,
    onViewDetails: (MediaItem) -> Unit,
    onPlay: (MediaItem) -> Unit,
) {
    if (items.isEmpty()) return
    val keys = remember(items) { items.map { it.homeFeaturedKey() } }
    val pager = rememberPagerState(
        initialPage = keys.indexOf(selectedMediaKey).coerceAtLeast(0),
        pageCount = { items.size },
    )
    LaunchedEffect(keys) {
        val selectedIndex = keys.indexOf(selectedMediaKey).coerceAtLeast(0)
        if (pager.currentPage != selectedIndex) pager.scrollToPage(selectedIndex)
        snapshotFlow { pager.settledPage }.collect { index ->
            keys.getOrNull(index)?.let(onSelectedMediaKeyChange)
        }
    }
    LaunchedEffect(keys, pager.isScrollInProgress, pager.settledPage) {
        if (keys.size > 1 && !pager.isScrollInProgress) {
            delay(6500L)
            if (!pager.isScrollInProgress) {
                pager.scrollToPage((pager.settledPage + 1) % keys.size)
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(state = pager, key = { keys[it] }) { index ->
            val item = items[index]
            val saved = isSaved(item)
            Box(
                Modifier.padding(horizontal = 16.dp)
                    .fillMaxWidth().aspectRatio(.92f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(VueoPalette.SurfaceStrong)
                    .clickable { onViewDetails(item) },
            ) {
                NetworkImage(
                    url = item.poster ?: item.background,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    fallbackText = item.name,
                )
                Box(Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0f to Color.Transparent, .45f to Color.Transparent,
                        .78f to Color.Black.copy(alpha = .7f), 1f to Color.Black),
                ))
                Column(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        item.name, color = Color.White, fontSize = 28.sp, lineHeight = 31.sp,
                        fontWeight = FontWeight.Black, textAlign = TextAlign.Center,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                    )
                    val metadata = listOfNotNull(
                        item.genres.firstOrNull { it.isNotBlank() },
                        item.releaseInfo?.takeIf { it.isNotBlank() },
                    ).joinToString(" • ")
                    if (metadata.isNotEmpty()) {
                        Text(
                            metadata, color = Color.White.copy(alpha = .88f),
                            fontSize = 13.sp, textAlign = TextAlign.Center,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onPlay(item) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Play")
                        }
                        OutlinedButton(
                            onClick = { onToggleSaved(item) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) {
                            Icon(if (saved) Icons.Default.Check else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("My List", maxLines = 1)
                        }
                    }
                }
            }
        }
        if (keys.size > 1) {
            Row(
                Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                keys.indices.forEach { index ->
                    val active = index == pager.currentPage
                    Box(
                        Modifier.width(if (active) 18.dp else 5.dp).height(5.dp)
                            .background(Color.White.copy(alpha = if (active) 1f else .38f), RoundedCornerShape(50)),
                    )
                }
            }
        }
    }
}
