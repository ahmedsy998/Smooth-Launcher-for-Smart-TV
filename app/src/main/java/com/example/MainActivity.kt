package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppCategory
import com.example.model.AppItem
import com.example.model.BuiltInSection
import com.example.ui.components.ReferenceBackgroundCanvas
import com.example.ui.components.RightSideNavBar
import com.example.ui.components.TopStatusArea
import com.example.ui.screens.*
import com.example.ui.theme.LuminousCyanGlow
import com.example.ui.theme.SmoothTvLauncherTheme
import com.example.viewmodel.LauncherViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val launcherViewModel: LauncherViewModel = viewModel()
            val uiState by launcherViewModel.uiState.collectAsStateWithLifecycle()

            SmoothTvLauncherTheme(typographyConfig = uiState.typographyConfig) {
                SmoothTvLauncherRoot(viewModel = launcherViewModel)
            }
        }
    }
}

@Composable
fun SmoothTvLauncherRoot(
    viewModel: LauncherViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val firstNavFocusRequester = remember { FocusRequester() }
    val bannerFocusRequester = remember { FocusRequester() }
    val firstAppFocusRequester = remember { FocusRequester() }
    val sectionContentFocusRequester = remember { FocusRequester() }

    var selectedAppForDialog by remember { mutableStateOf<AppItem?>(null) }

    // Official Android Document Picker for Custom Background Import (JPEG, PNG, WebP)
    val backgroundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.previewCustomBackgroundUri(uri.toString())
        }
    }

    // Official Android Document Picker for Opening Media / Files in the Files Section
    val openMediaDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(viewIntent)
            }.onFailure {
                viewModel.showStatusMessage("تم اختيار الملف: ${uri.lastPathSegment ?: uri}")
            }
        }
    }

    // Request initial focus on the right-side navigation bar on first launch
    LaunchedEffect(Unit) {
        delay(120)
        runCatching { firstNavFocusRequester.requestFocus() }
    }

    // Auto-dismiss status toast message
    LaunchedEffect(uiState.statusBannerMessageAr) {
        if (uiState.statusBannerMessageAr != null) {
            delay(3500)
            viewModel.clearStatusMessage()
        }
    }

    // TV Remote Back Button handling: return from secondary sections to Home screen and restore focus
    BackHandler(enabled = uiState.activeSection != BuiltInSection.HOME) {
        viewModel.navigateToSection(BuiltInSection.HOME)
        if (uiState.remoteNavConfig.rememberLastSettingsFocus) {
            runCatching { firstNavFocusRequester.requestFocus() }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .testTag("launcher_scaffold")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("launcher_root_box")
        ) {
            // 1. Full-Bleed Reference / Custom Wallpaper Background Layer
            ReferenceBackgroundCanvas(
                backgroundConfig = uiState.backgroundConfig,
                customWallpaperBitmap = uiState.activeWallpaperBitmap,
                performanceConfig = uiState.performanceConfig
            )

            // 2. Widescreen 16:9 Stage (RTL LayoutDirection is active: first child in Row is on the far RIGHT)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Far-Right Vertical Navigation Menu
                RightSideNavBar(
                    menuItems = uiState.visibleMenuItems,
                    selectedMenuId = uiState.selectedMenuId,
                    compactWidth = uiState.homeLayoutConfig.compactNavMenu,
                    typographyConfig = uiState.typographyConfig,
                    performanceConfig = uiState.performanceConfig,
                    firstNavFocusRequester = firstNavFocusRequester,
                    onSelectMenuItem = { item ->
                        viewModel.selectMenuItem(item)
                    },
                    onMoveFocusToContent = {
                        if (uiState.activeSection == BuiltInSection.HOME) {
                            runCatching { bannerFocusRequester.requestFocus() }
                        } else {
                            runCatching { sectionContentFocusRequester.requestFocus() }
                        }
                    }
                )

                // Main Content Area to the Left of the Right-Side Navigation Menu
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("main_content_stage"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Minimal Top Status Area (Current Time, Arabic Date, Real-Time Wi-Fi Indicator ONLY)
                    TopStatusArea(
                        clockDateConfig = uiState.clockDateConfig,
                        typographyConfig = uiState.typographyConfig,
                        networkStatus = uiState.networkStatus
                    )

                    // Active Section Viewport
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (uiState.activeSection) {
                            BuiltInSection.HOME -> {
                                HomeSectionContent(
                                    uiState = uiState,
                                    bannerFocusRequester = bannerFocusRequester,
                                    firstAppFocusRequester = firstAppFocusRequester,
                                    onSelectBannerIndex = { viewModel.setBannerIndex(it) },
                                    onNextBanner = { viewModel.nextBanner() },
                                    onPreviousBanner = { viewModel.previousBanner() },
                                    onExecuteBannerAction = { viewModel.executeBannerAction(it) },
                                    onLaunchApp = { viewModel.launchApp(it) },
                                    onOpenAppOptions = { selectedAppForDialog = it },
                                    onOpenAllApplications = {
                                        viewModel.navigateToSection(BuiltInSection.APPLICATIONS)
                                    },
                                    onMoveFocusToRightNav = {
                                        runCatching { firstNavFocusRequester.requestFocus() }
                                    }
                                )
                            }

                            BuiltInSection.APPLICATIONS -> {
                                ApplicationsSectionContent(
                                    uiState = uiState,
                                    contentFocusRequester = sectionContentFocusRequester,
                                    onLaunchApp = { viewModel.launchApp(it) },
                                    onOpenAppOptions = { selectedAppForDialog = it },
                                    onMoveFocusToRightNav = {
                                        runCatching { firstNavFocusRequester.requestFocus() }
                                    }
                                )
                            }

                            BuiltInSection.MOVIES -> {
                                MediaCategorySectionContent(
                                    sectionTitleAr = "الأفلام والسينما",
                                    sectionSubtitleAr = "تطبيقات الأفلام والفيديو المثبتة على جهازك بالإضافة إلى مشغل ملفات الفيديو المحلية",
                                    apps = uiState.moviesApps,
                                    allInstalledApps = uiState.installedApps,
                                    targetCategory = AppCategory.STREAMING_MOVIES,
                                    uiState = uiState,
                                    contentFocusRequester = sectionContentFocusRequester,
                                    onLaunchApp = { viewModel.launchApp(it) },
                                    onOpenAppOptions = { selectedAppForDialog = it },
                                    onAssignAppCategory = { pkg, cat -> viewModel.setAppCategory(pkg, cat) },
                                    onOpenLocalFilesSection = {
                                        viewModel.navigateToSection(BuiltInSection.FILES)
                                    },
                                    onMoveFocusToRightNav = {
                                        runCatching { firstNavFocusRequester.requestFocus() }
                                    }
                                )
                            }

                            BuiltInSection.SERIES -> {
                                MediaCategorySectionContent(
                                    sectionTitleAr = "المسلسلات والبث التلفزيوني",
                                    sectionSubtitleAr = "تطبيقات المسلسلات والقنوات المباشرة المثبتة على جهازك",
                                    apps = uiState.seriesApps,
                                    allInstalledApps = uiState.installedApps,
                                    targetCategory = AppCategory.STREAMING_SERIES,
                                    uiState = uiState,
                                    contentFocusRequester = sectionContentFocusRequester,
                                    onLaunchApp = { viewModel.launchApp(it) },
                                    onOpenAppOptions = { selectedAppForDialog = it },
                                    onAssignAppCategory = { pkg, cat -> viewModel.setAppCategory(pkg, cat) },
                                    onOpenLocalFilesSection = {
                                        viewModel.navigateToSection(BuiltInSection.FILES)
                                    },
                                    onMoveFocusToRightNav = {
                                        runCatching { firstNavFocusRequester.requestFocus() }
                                    }
                                )
                            }

                            BuiltInSection.FAVORITES -> {
                                FavoritesSectionContent(
                                    uiState = uiState,
                                    contentFocusRequester = sectionContentFocusRequester,
                                    onLaunchApp = { viewModel.launchApp(it) },
                                    onToggleFavorite = { viewModel.toggleFavoriteApp(it) },
                                    onOpenAppOptions = { selectedAppForDialog = it },
                                    onMoveFocusToRightNav = {
                                        runCatching { firstNavFocusRequester.requestFocus() }
                                    }
                                )
                            }

                            BuiltInSection.FILES -> {
                                FilesSectionContent(
                                    uiState = uiState,
                                    contentFocusRequester = sectionContentFocusRequester,
                                    onOpenDirectory = { path -> viewModel.loadDirectoryFiles(path) },
                                    onLaunchSystemFilePicker = {
                                        runCatching {
                                            openMediaDocumentLauncher.launch(arrayOf("*/*"))
                                        }
                                    },
                                    onShowMessage = { msg -> viewModel.showStatusMessage(msg) }
                                )
                            }

                            BuiltInSection.SETTINGS -> {
                                SettingsScreenContent(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    contentFocusRequester = sectionContentFocusRequester,
                                    onPickBackgroundFromFiles = {
                                        runCatching {
                                            backgroundPickerLauncher.launch(
                                                arrayOf("image/jpeg", "image/png", "image/webp", "image/*")
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Floating Status Toast Message
            AnimatedVisibility(
                visible = uiState.statusBannerMessageAr != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 24.dp, start = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xEE121B3A))
                        .border(1.5.dp, LuminousCyanGlow, RoundedCornerShape(14.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                        .testTag("status_feedback_toast")
                ) {
                    Text(
                        text = uiState.statusBannerMessageAr.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }

    // App Management Dialog (Pin to Home, Reorder, Favorite, Shortcut, Category)
    selectedAppForDialog?.let { currentApp ->
        // Keep dialog synced with latest state
        val syncedApp = uiState.installedApps.find { it.packageName == currentApp.packageName } ?: currentApp
        AppItemManagementDialog(
            app = syncedApp,
            onDismiss = { selectedAppForDialog = null },
            onLaunch = { viewModel.launchApp(syncedApp) },
            onTogglePinToHome = { viewModel.togglePinAppToHome(syncedApp.packageName) },
            onMoveRightInHomeRow = { viewModel.moveHomeRowApp(syncedApp.packageName, moveLeftInRtl = false) },
            onMoveLeftInHomeRow = { viewModel.moveHomeRowApp(syncedApp.packageName, moveLeftInRtl = true) },
            onToggleFavorite = { viewModel.toggleFavoriteApp(syncedApp.packageName) },
            onAddShortcutToNavMenu = { viewModel.addAppShortcutToMenu(syncedApp) },
            onChangeCategory = { cat -> viewModel.setAppCategory(syncedApp.packageName, cat) }
        )
    }
}
