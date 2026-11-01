package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.components.AppTileCard
import com.example.ui.components.ApplicationBottomRow
import com.example.ui.components.FeaturedBannerCarousel
import com.example.ui.components.NavVectorIcon
import com.example.ui.theme.LuminousCyanGlow
import com.example.ui.theme.LuminousPurpleGlow
import com.example.ui.theme.MidnightNavyElevated
import com.example.ui.theme.SoftLavenderText
import com.example.viewmodel.LauncherUiState
import java.io.File

@Composable
fun HomeSectionContent(
    uiState: LauncherUiState,
    bannerFocusRequester: FocusRequester,
    firstAppFocusRequester: FocusRequester,
    onSelectBannerIndex: (Int) -> Unit,
    onNextBanner: () -> Unit,
    onPreviousBanner: () -> Unit,
    onExecuteBannerAction: (FeaturedBannerItem) -> Unit,
    onLaunchApp: (AppItem) -> Unit,
    onOpenAppOptions: (AppItem) -> Unit,
    onOpenAllApplications: () -> Unit,
    onMoveFocusToRightNav: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_section_content"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Large Cinematic Featured-Content Banner (takes ~62% height matching reference proportions)
        FeaturedBannerCarousel(
            banners = uiState.featuredBanners,
            activeIndex = uiState.activeBannerIndex,
            homeLayoutConfig = uiState.homeLayoutConfig,
            iconConfig = uiState.iconConfig,
            performanceConfig = uiState.performanceConfig,
            bannerFocusRequester = bannerFocusRequester,
            onSelectBannerIndex = onSelectBannerIndex,
            onNextBanner = onNextBanner,
            onPreviousBanner = onPreviousBanner,
            onExecuteBannerAction = onExecuteBannerAction,
            onMoveFocusToRightNav = onMoveFocusToRightNav,
            onMoveFocusDownToApps = {
                runCatching { firstAppFocusRequester.requestFocus() }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.63f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Row of Installed Application Tiles along the bottom
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.37f)
        ) {
            ApplicationBottomRow(
                apps = uiState.homeRowApps,
                iconConfig = uiState.iconConfig,
                typographyConfig = uiState.typographyConfig,
                performanceConfig = uiState.performanceConfig,
                firstAppFocusRequester = firstAppFocusRequester,
                onLaunchApp = onLaunchApp,
                onLongPressApp = onOpenAppOptions,
                onOpenAllApplications = onOpenAllApplications,
                onMoveFocusUpToBanner = {
                    runCatching { bannerFocusRequester.requestFocus() }
                },
                onMoveFocusToRightNav = onMoveFocusToRightNav
            )
        }
    }
}

@Composable
fun ApplicationsSectionContent(
    uiState: LauncherUiState,
    contentFocusRequester: FocusRequester,
    onLaunchApp: (AppItem) -> Unit,
    onOpenAppOptions: (AppItem) -> Unit,
    onMoveFocusToRightNav: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<AppCategory?>(null) }
    var showOnlyHomeRow by remember { mutableStateOf(false) }

    val filteredApps = remember(uiState.installedApps, selectedFilter, showOnlyHomeRow) {
        uiState.installedApps.filter { app ->
            val catMatch = selectedFilter == null || app.category == selectedFilter
            val homeMatch = !showOnlyHomeRow || app.isPinnedToHome
            catMatch && homeMatch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("applications_section_content"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header & Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "التطبيقات المثبتة (${filteredApps.size})",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = "اضغط مطولاً أو زر القائمة على أي تطبيق لتثبيته في الشاشة الرئيسية أو إضافته للمفضلة أو تغيير ترتيبه",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftLavenderText
                )
            }
        }

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                TvFilterChip(
                    label = "كل التطبيقات",
                    selected = selectedFilter == null && !showOnlyHomeRow,
                    onClick = {
                        selectedFilter = null
                        showOnlyHomeRow = false
                    }
                )
            }
            item {
                TvFilterChip(
                    label = "صف الشاشة الرئيسية (${uiState.homeRowApps.size})",
                    selected = showOnlyHomeRow,
                    onClick = {
                        showOnlyHomeRow = !showOnlyHomeRow
                        if (showOnlyHomeRow) selectedFilter = null
                    }
                )
            }
            items(AppCategory.entries) { category ->
                TvFilterChip(
                    label = category.labelAr,
                    selected = selectedFilter == category && !showOnlyHomeRow,
                    onClick = {
                        showOnlyHomeRow = false
                        selectedFilter = if (selectedFilter == category) null else category
                    }
                )
            }
        }

        if (filteredApps.isEmpty()) {
            EmptySectionCard(
                titleAr = "لا توجد تطبيقات مطابقة لهذا التصنيف حالياً",
                subtitleAr = "يمكنك عرض كل التطبيقات وتعيين أي تطبيق ضمن هذا القسم"
            )
        } else {
            val minCellWidth = (uiState.iconConfig.customSizeDp * 1.48f).coerceAtLeast(110f).dp
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = minCellWidth),
                verticalArrangement = Arrangement.spacedBy(uiState.iconConfig.iconSpacingDp.dp),
                horizontalArrangement = Arrangement.spacedBy(uiState.iconConfig.iconSpacingDp.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("all_apps_grid")
            ) {
                itemsIndexed(
                    items = filteredApps,
                    key = { _, app -> app.packageName }
                ) { index, app ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppTileCard(
                            app = app,
                            iconConfig = uiState.iconConfig,
                            typographyConfig = uiState.typographyConfig,
                            performanceConfig = uiState.performanceConfig,
                            modifier = if (index == 0) {
                                Modifier.focusRequester(contentFocusRequester)
                            } else {
                                Modifier
                            },
                            onClick = { onLaunchApp(app) },
                            onLongClick = { onOpenAppOptions(app) },
                            onMoveRightEdge = if (index == 0) onMoveFocusToRightNav else null
                        )
                        // Subtle quick action button under each tile in All Apps screen for easy remote access
                        SmallTvActionPill(
                            text = if (app.isPinnedToHome) "خيارات • مثبت بالرئيسية" else "خيارات التطبيق",
                            onClick = { onOpenAppOptions(app) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaCategorySectionContent(
    sectionTitleAr: String,
    sectionSubtitleAr: String,
    apps: List<AppItem>,
    allInstalledApps: List<AppItem>,
    targetCategory: AppCategory,
    uiState: LauncherUiState,
    contentFocusRequester: FocusRequester,
    onLaunchApp: (AppItem) -> Unit,
    onOpenAppOptions: (AppItem) -> Unit,
    onAssignAppCategory: (String, AppCategory) -> Unit,
    onOpenLocalFilesSection: () -> Unit,
    onMoveFocusToRightNav: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAssignModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("media_category_section_${targetCategory.name}"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sectionTitleAr,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = sectionSubtitleAr,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftLavenderText
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TvActionButton(
                    text = "تخصيص تطبيقات القسم",
                    onClick = { showAssignModal = true }
                )
                TvActionButton(
                    text = "تصفح ملفات الفيديو المحلية",
                    onClick = onOpenLocalFilesSection
                )
            }
        }

        val minCellWidth = (uiState.iconConfig.customSizeDp * 1.48f).coerceAtLeast(110f).dp
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = minCellWidth),
            verticalArrangement = Arrangement.spacedBy(uiState.iconConfig.iconSpacingDp.dp),
            horizontalArrangement = Arrangement.spacedBy(uiState.iconConfig.iconSpacingDp.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(
                items = apps,
                key = { _, app -> app.packageName }
            ) { index, app ->
                AppTileCard(
                    app = app,
                    iconConfig = uiState.iconConfig,
                    typographyConfig = uiState.typographyConfig,
                    performanceConfig = uiState.performanceConfig,
                    modifier = if (index == 0) {
                        Modifier.focusRequester(contentFocusRequester)
                    } else {
                        Modifier
                    },
                    onClick = { onLaunchApp(app) },
                    onLongClick = { onOpenAppOptions(app) },
                    onMoveRightEdge = if (index == 0) onMoveFocusToRightNav else null
                )
            }
        }
    }

    if (showAssignModal) {
        Dialog(onDismissRequest = { showAssignModal = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MidnightNavyElevated,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .heightIn(max = 460.dp)
                    .border(1.5.dp, LuminousPurpleGlow, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "اختر التطبيقات المضافة إلى قسم $sectionTitleAr",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allInstalledApps, key = { it.packageName }) { app ->
                            val isAssigned = app.category == targetCategory
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isAssigned) Color(0x444F46E5) else Color(0x331E293B))
                                    .clickable {
                                        val nextCat = if (isAssigned) AppCategory.GENERAL else targetCategory
                                        onAssignAppCategory(app.packageName, nextCat)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = app.label, color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = if (isAssigned) "مضاف في القسم ✓" else "إضافة للقسم +",
                                    color = if (isAssigned) LuminousCyanGlow else SoftLavenderText,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TvActionButton(
                            text = "إغلاق",
                            onClick = { showAssignModal = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesSectionContent(
    uiState: LauncherUiState,
    contentFocusRequester: FocusRequester,
    onLaunchApp: (AppItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenAppOptions: (AppItem) -> Unit,
    onMoveFocusToRightNav: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showManageFavoritesDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("favorites_section_content"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "المفضلة (${uiState.favoriteApps.size})",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = "تطبيقاتك المفضلة للوصول السريع بضغطة زر واحدة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftLavenderText
                )
            }

            TvActionButton(
                text = "إدارة قائمة المفضلة +",
                modifier = if (uiState.favoriteApps.isEmpty()) {
                    Modifier.focusRequester(contentFocusRequester)
                } else {
                    Modifier
                },
                onClick = { showManageFavoritesDialog = true }
            )
        }

        if (uiState.favoriteApps.isEmpty()) {
            EmptySectionCard(
                titleAr = "لم تقم بإضافة أي تطبيقات إلى المفضلة بعد",
                subtitleAr = "اضغط على «إدارة قائمة المفضلة +» أعلاه لاختيار تطبيقاتك المفضلة"
            )
        } else {
            val minCellWidth = (uiState.iconConfig.customSizeDp * 1.48f).coerceAtLeast(110f).dp
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = minCellWidth),
                verticalArrangement = Arrangement.spacedBy(uiState.iconConfig.iconSpacingDp.dp),
                horizontalArrangement = Arrangement.spacedBy(uiState.iconConfig.iconSpacingDp.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = uiState.favoriteApps,
                    key = { _, app -> app.packageName }
                ) { index, app ->
                    AppTileCard(
                        app = app,
                        iconConfig = uiState.iconConfig,
                        typographyConfig = uiState.typographyConfig,
                        performanceConfig = uiState.performanceConfig,
                        modifier = if (index == 0) {
                            Modifier.focusRequester(contentFocusRequester)
                        } else {
                            Modifier
                        },
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onOpenAppOptions(app) },
                        onMoveRightEdge = if (index == 0) onMoveFocusToRightNav else null
                    )
                }
            }
        }
    }

    if (showManageFavoritesDialog) {
        Dialog(onDismissRequest = { showManageFavoritesDialog = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MidnightNavyElevated,
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .heightIn(max = 460.dp)
                    .border(1.5.dp, LuminousCyanGlow, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "اختر التطبيقات المفضلة",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.installedApps, key = { it.packageName }) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (app.isFavorite) Color(0x446366F1) else Color(0x331E293B))
                                    .clickable { onToggleFavorite(app.packageName) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = app.label, color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = if (app.isFavorite) "★ في المفضلة" else "☆ إضافة للمفضلة",
                                    color = if (app.isFavorite) Color(0xFFFBBF24) else SoftLavenderText
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TvActionButton(
                            text = "تم",
                            onClick = { showManageFavoritesDialog = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilesSectionContent(
    uiState: LauncherUiState,
    contentFocusRequester: FocusRequester,
    onOpenDirectory: (String?) -> Unit,
    onLaunchSystemFilePicker: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("files_section_content"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "مدير الملفات والوسائط المحلية",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = "المسار الحالي: ${uiState.currentFileDirPath.ifEmpty { "التخزين المحلي" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftLavenderText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TvActionButton(
                    text = "المجلد الأعلى ↑",
                    modifier = Modifier.focusRequester(contentFocusRequester),
                    onClick = {
                        val parent = File(uiState.currentFileDirPath).parentFile
                        if (parent != null && parent.canRead()) {
                            onOpenDirectory(parent.absolutePath)
                        } else {
                            onOpenDirectory(null)
                        }
                    }
                )
                TvActionButton(
                    text = "فتح مستعرض ملفات النظام",
                    onClick = onLaunchSystemFilePicker
                )
            }
        }

        if (uiState.localFiles.isEmpty()) {
            EmptySectionCard(
                titleAr = "هذا المجلد فارغ أو يتطلب مستعرض ملفات النظام",
                subtitleAr = "اضغط على «فتح مستعرض ملفات النظام» أعلاه لتصفح وفتح أي ملف أو وسائط"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("local_files_list")
            ) {
                items(uiState.localFiles, key = { it.path }) { fileItem ->
                    var isFocused by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isFocused) Color(0x553B82F6) else Color(0x88111833)
                            )
                            .border(
                                width = if (isFocused) 1.8.dp else 1.dp,
                                color = if (isFocused) LuminousCyanGlow else Color(0x2294A3B8),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .onFocusChanged { isFocused = it.isFocused }
                            .focusable()
                            .clickable {
                                if (fileItem.isDirectory) {
                                    onOpenDirectory(fileItem.path)
                                } else {
                                    onShowMessage("الملف المحدد: ${fileItem.name} (${fileItem.fileTypeLabelAr})")
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NavVectorIcon(
                                iconName = if (fileItem.isDirectory) "folder" else "movies",
                                tint = if (fileItem.isDirectory) LuminousCyanGlow else SoftLavenderText,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = fileItem.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = fileItem.fileTypeLabelAr,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SoftLavenderText
                                )
                            }
                        }

                        Text(
                            text = if (fileItem.isDirectory) "فتح المجلد ←" else "${fileItem.sizeBytes / 1024} ك.ب",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftLavenderText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppItemManagementDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onTogglePinToHome: () -> Unit,
    onMoveRightInHomeRow: () -> Unit,
    onMoveLeftInHomeRow: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddShortcutToNavMenu: () -> Unit,
    onChangeCategory: (AppCategory) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MidnightNavyElevated,
            modifier = Modifier
                .fillMaxWidth(0.68f)
                .border(
                    width = 1.8.dp,
                    brush = Brush.horizontalGradient(listOf(LuminousCyanGlow, LuminousPurpleGlow)),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("app_management_dialog")
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "إدارة التطبيق: ${app.label}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.labelMedium,
                    color = SoftLavenderText
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TvActionButton(
                        text = "تشغيل التطبيق الآن",
                        onClick = {
                            onDismiss()
                            onLaunch()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    TvActionButton(
                        text = if (app.isFavorite) "إزالة من المفضلة ★" else "إضافة إلى المفضلة ☆",
                        onClick = onToggleFavorite,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TvActionButton(
                        text = if (app.isPinnedToHome) "إزالة من صف الشاشة الرئيسية" else "إضافة لصف الشاشة الرئيسية",
                        onClick = onTogglePinToHome,
                        modifier = Modifier.weight(1f)
                    )
                    TvActionButton(
                        text = "إضافة اختصار في القائمة الجانبية",
                        onClick = {
                            onAddShortcutToNavMenu()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (app.isPinnedToHome) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TvActionButton(
                            text = "تحريك لليمين في الصف الرئيسي →",
                            onClick = onMoveRightInHomeRow,
                            modifier = Modifier.weight(1f)
                        )
                        TvActionButton(
                            text = "← تحريك لليسار في الصف الرئيسي",
                            onClick = onMoveLeftInHomeRow,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text(
                    text = "تصنيف التطبيق (${app.category.labelAr}):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftLavenderText,
                    modifier = Modifier.padding(top = 4.dp)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AppCategory.entries) { cat ->
                        TvFilterChip(
                            label = cat.labelAr,
                            selected = app.category == cat,
                            onClick = { onChangeCategory(cat) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TvActionButton(text = "إغلاق", onClick = onDismiss)
                }
            }
        }
    }
}

@Composable
fun TvActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    testTagName: String? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(
                brush = when {
                    isFocused -> Brush.horizontalGradient(
                        listOf(Color(0xFF8B5CF6), Color(0xFF2563EB))
                    )

                    isPrimary -> Brush.horizontalGradient(
                        listOf(Color(0xFF6D28D9), Color(0xFF1D4ED8))
                    )

                    else -> Brush.horizontalGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF151E34))
                    )
                },
                shape = shape
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) LuminousCyanGlow else Color(0x5564748B),
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .let { if (testTagName != null) it.testTag(testTagName) else it }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (isFocused || isPrimary) FontWeight.Bold else FontWeight.Medium
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TvFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(50)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(shape)
            .background(
                when {
                    selected -> Color(0xFF6D28D9)
                    isFocused -> Color(0xFF334155)
                    else -> Color(0x99151E38)
                }
            )
            .border(
                width = if (isFocused || selected) 1.5.dp else 1.dp,
                color = if (isFocused) LuminousCyanGlow else if (selected) LuminousPurpleGlow else Color(0x3364748B),
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected || isFocused) FontWeight.Bold else FontWeight.Normal
            ),
            color = Color.White
        )
    }
}

@Composable
private fun SmallTvActionPill(
    text: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isFocused) Color(0xFF4F46E5) else Color(0x661E293B))
            .border(
                width = 1.dp,
                color = if (isFocused) LuminousCyanGlow else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = SoftLavenderText
        )
    }
}

@Composable
fun EmptySectionCard(
    titleAr: String,
    subtitleAr: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x66111833))
            .border(1.dp, Color(0x3364748B), RoundedCornerShape(20.dp))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = titleAr,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Text(
                text = subtitleAr,
                style = MaterialTheme.typography.bodyMedium,
                color = SoftLavenderText
            )
        }
    }
}
