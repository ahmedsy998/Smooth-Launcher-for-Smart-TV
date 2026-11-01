package com.example.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LauncherPreferencesRepository
import com.example.model.*
import com.example.service.AppDiscoveryManager
import com.example.service.NetworkMonitor
import com.example.service.WallpaperLoader
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LauncherUiState(
    val isLoadingApps: Boolean = true,
    val menuItems: List<NavMenuItem> = LauncherPreferencesRepository.defaultMenuItems(),
    val selectedMenuId: String = BuiltInSection.HOME.id,
    val activeSection: BuiltInSection = BuiltInSection.HOME,
    val activeSettingsSubScreen: SettingsSubScreen = SettingsSubScreen.CUSTOMIZE_MENU,
    val installedApps: List<AppItem> = emptyList(),
    val featuredBanners: List<FeaturedBannerItem> = LauncherPreferencesRepository.defaultFeaturedBanners(),
    val activeBannerIndex: Int = 0,
    val iconConfig: IconCustomizationConfig = IconCustomizationConfig(),
    val typographyConfig: TypographyConfig = TypographyConfig(),
    val backgroundConfig: BackgroundConfig = BackgroundConfig(),
    val activeWallpaperBitmap: ImageBitmap? = null,
    val previewWallpaperBitmap: ImageBitmap? = null,
    val clockDateConfig: ClockDateConfig = ClockDateConfig(),
    val homeLayoutConfig: HomeLayoutConfig = HomeLayoutConfig(),
    val remoteNavConfig: RemoteNavigationConfig = RemoteNavigationConfig(),
    val performanceConfig: PerformanceConfig = PerformanceConfig(),
    val networkStatus: NetworkStatus = NetworkStatus.ConnectedValidated,
    val currentFileDirPath: String = "",
    val localFiles: List<LocalFileItem> = emptyList(),
    val statusBannerMessageAr: String? = null
) {
    val visibleMenuItems: List<NavMenuItem>
        get() = menuItems
            .filter { it.visibility == NavItemVisibility.VISIBLE }
            .sortedBy { it.orderIndex }

    val hiddenMenuItems: List<NavMenuItem>
        get() = menuItems
            .filter { it.visibility == NavItemVisibility.HIDDEN }
            .sortedBy { it.orderIndex }

    val removedMenuItems: List<NavMenuItem>
        get() = menuItems
            .filter { it.visibility == NavItemVisibility.REMOVED }
            .sortedBy { it.orderIndex }

    val homeRowApps: List<AppItem>
        get() = installedApps
            .filter { it.isPinnedToHome }
            .sortedBy { it.homeOrder }
            .take(homeLayoutConfig.maxHomeRowApps)

    val favoriteApps: List<AppItem>
        get() = installedApps.filter { it.isFavorite }

    val moviesApps: List<AppItem>
        get() = installedApps.filter {
            it.category == AppCategory.STREAMING_MOVIES || it.category == AppCategory.STREAMING_SERIES
        }.ifEmpty { installedApps }

    val seriesApps: List<AppItem>
        get() = installedApps.filter {
            it.category == AppCategory.STREAMING_SERIES || it.category == AppCategory.STREAMING_MOVIES
        }.ifEmpty { installedApps }
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = LauncherPreferencesRepository(application)
    private val appDiscovery = AppDiscoveryManager(application)
    private val networkMonitor = NetworkMonitor(application)
    private val wallpaperLoader = WallpaperLoader(application)

    private val _uiState = MutableStateFlow(
        LauncherUiState(
            menuItems = repo.loadMenuItems(),
            featuredBanners = repo.loadFeaturedBanners(),
            iconConfig = repo.loadIconConfig(),
            typographyConfig = repo.loadTypographyConfig(),
            backgroundConfig = repo.loadBackgroundConfig(),
            clockDateConfig = repo.loadClockDateConfig(),
            homeLayoutConfig = repo.loadHomeLayoutConfig(),
            remoteNavConfig = repo.loadRemoteNavigationConfig(),
            performanceConfig = repo.loadPerformanceConfig()
        )
    )
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private var favoritePackages: Set<String> = repo.loadFavoritePackages()
    private var pinnedHomePackages: List<String> = repo.loadPinnedHomePackages()
    private var customAppCategories: Map<String, AppCategory> = repo.loadCustomAppCategories()

    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            appDiscovery.invalidateCache()
            refreshInstalledApps()
        }
    }

    init {
        // Ensure initial selected menu item is visible
        val initialVisible = _uiState.value.visibleMenuItems
        if (initialVisible.isNotEmpty() && initialVisible.none { it.id == _uiState.value.selectedMenuId }) {
            selectMenuItem(initialVisible.first())
        }

        // Observe real-time internet connectivity
        viewModelScope.launch {
            networkMonitor.observeNetworkStatus().collect { status ->
                _uiState.update { it.copy(networkStatus = status) }
            }
        }

        // Load saved custom background if any
        val savedCustomUri = _uiState.value.backgroundConfig.customImageUri
        if (savedCustomUri.isNotBlank()) {
            viewModelScope.launch {
                val bmp = wallpaperLoader.loadOptimizedWallpaper(savedCustomUri)
                if (bmp != null) {
                    _uiState.update { it.copy(activeWallpaperBitmap = bmp) }
                } else {
                    // Fallback gracefully if deleted or inaccessible
                    val resetBg = _uiState.value.backgroundConfig.copy(customImageUri = "")
                    repo.saveBackgroundConfig(resetBg)
                    _uiState.update {
                        it.copy(
                            backgroundConfig = resetBg,
                            activeWallpaperBitmap = null,
                            statusBannerMessageAr = "تعذر الوصول إلى صورة الخلفية المخصصة، تمت استعادة الخلفية الافتراضية"
                        )
                    }
                }
            }
        }

        // Discover installed applications
        refreshInstalledApps()
        loadDirectoryFiles(null)

        // Register package change receiver
        runCatching {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
            application.registerReceiver(packageChangeReceiver, filter)
        }
    }

    override fun onCleared() {
        super.onCleared()
        runCatching {
            getApplication<Application>().unregisterReceiver(packageChangeReceiver)
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingApps = true) }
            val apps = appDiscovery.discoverInstalledApps(
                favoritePackages = favoritePackages,
                pinnedHomePackages = pinnedHomePackages,
                customCategories = customAppCategories,
                maxHomeRowCount = _uiState.value.homeLayoutConfig.maxHomeRowApps
            )
            // If pinnedHomePackages was empty, initialize from discovered default pinned apps
            if (pinnedHomePackages.isEmpty() && apps.isNotEmpty()) {
                pinnedHomePackages = apps.filter { it.isPinnedToHome }
                    .sortedBy { it.homeOrder }
                    .map { it.packageName }
            }
            _uiState.update {
                it.copy(
                    isLoadingApps = false,
                    installedApps = apps
                )
            }
        }
    }

    fun selectMenuItem(item: NavMenuItem) {
        when (item.itemType) {
            NavItemType.BUILT_IN_SECTION -> {
                val section = BuiltInSection.fromId(item.targetValue) ?: BuiltInSection.HOME
                _uiState.update {
                    it.copy(
                        selectedMenuId = item.id,
                        activeSection = section
                    )
                }
            }

            NavItemType.MEDIA_SHORTCUT -> {
                val section = BuiltInSection.fromId(item.targetValue) ?: BuiltInSection.FILES
                _uiState.update {
                    it.copy(
                        selectedMenuId = item.id,
                        activeSection = section
                    )
                }
            }

            NavItemType.APP_SHORTCUT -> {
                _uiState.update { it.copy(selectedMenuId = item.id) }
                launchAppByPackage(item.targetValue)
            }

            NavItemType.SYSTEM_SHORTCUT -> {
                _uiState.update { it.copy(selectedMenuId = item.id) }
                val ok = appDiscovery.launchSystemSettingAction(item.targetValue)
                if (!ok) {
                    showStatusMessage("تعذر فتح إعدادات النظام المطلوبة على هذا الجهاز")
                }
            }
        }
    }

    fun navigateToSection(section: BuiltInSection, subScreen: SettingsSubScreen? = null) {
        val matchingMenu = _uiState.value.visibleMenuItems.find {
            it.itemType == NavItemType.BUILT_IN_SECTION && it.targetValue == section.id
        }
        _uiState.update {
            it.copy(
                selectedMenuId = matchingMenu?.id ?: section.id,
                activeSection = section,
                activeSettingsSubScreen = subScreen ?: it.activeSettingsSubScreen
            )
        }
    }

    fun selectSettingsSubScreen(subScreen: SettingsSubScreen) {
        _uiState.update {
            it.copy(
                activeSection = BuiltInSection.SETTINGS,
                activeSettingsSubScreen = subScreen
            )
        }
    }

    // --- Application Launching & Management ---

    fun launchApp(app: AppItem) {
        val launched = appDiscovery.launchApplication(app.packageName, app.activityName)
        if (!launched) {
            showStatusMessage("التطبيق (${app.label}) غير متاح أو تم إلغاء تثبيته")
            refreshInstalledApps()
        }
    }

    fun launchAppByPackage(packageName: String) {
        val app = _uiState.value.installedApps.find { it.packageName == packageName }
        val launched = appDiscovery.launchApplication(packageName, app?.activityName ?: "")
        if (!launched) {
            showStatusMessage("التطبيق المحدد غير مثبت حالياً على الجهاز")
            refreshInstalledApps()
        }
    }

    fun toggleFavoriteApp(packageName: String) {
        val updated = favoritePackages.toMutableSet()
        val added = if (updated.contains(packageName)) {
            updated.remove(packageName)
            false
        } else {
            updated.add(packageName)
            true
        }
        favoritePackages = updated
        repo.saveFavoritePackages(updated)
        val updatedApps = _uiState.value.installedApps.map {
            if (it.packageName == packageName) it.copy(isFavorite = added) else it
        }
        _uiState.update { it.copy(installedApps = updatedApps) }
        showStatusMessage(if (added) "تمت إضافة التطبيق إلى المفضلة" else "تمت إزالة التطبيق من المفضلة")
    }

    fun togglePinAppToHome(packageName: String) {
        val currentPinned = pinnedHomePackages.toMutableList()
        val isCurrentlyPinned = currentPinned.contains(packageName)
        if (isCurrentlyPinned) {
            currentPinned.remove(packageName)
        } else {
            currentPinned.add(packageName)
        }
        pinnedHomePackages = currentPinned
        repo.savePinnedHomePackages(currentPinned)
        applyPinnedOrderToApps(currentPinned)
        showStatusMessage(
            if (!isCurrentlyPinned) "تمت إضافة التطبيق إلى صف الشاشة الرئيسية"
            else "تمت إزالة التطبيق من صف الشاشة الرئيسية"
        )
    }

    fun moveHomeRowApp(packageName: String, moveLeftInRtl: Boolean) {
        val current = pinnedHomePackages.toMutableList()
        val index = current.indexOf(packageName)
        if (index == -1) return
        // In RTL, index 0 is on the right and higher index is to the left
        val targetIndex = if (moveLeftInRtl) index + 1 else index - 1
        if (targetIndex !in current.indices) return
        val item = current.removeAt(index)
        current.add(targetIndex, item)
        pinnedHomePackages = current
        repo.savePinnedHomePackages(current)
        applyPinnedOrderToApps(current)
    }

    fun setAppCategory(packageName: String, category: AppCategory) {
        val updated = customAppCategories.toMutableMap()
        updated[packageName] = category
        customAppCategories = updated
        repo.saveCustomAppCategories(updated)
        _uiState.update { state ->
            state.copy(
                installedApps = state.installedApps.map {
                    if (it.packageName == packageName) it.copy(category = category) else it
                }
            )
        }
        showStatusMessage("تم تحديث تصنيف التطبيق إلى: ${category.labelAr}")
    }

    private fun applyPinnedOrderToApps(pinnedOrder: List<String>) {
        _uiState.update { state ->
            val updatedApps = state.installedApps.map { app ->
                val idx = pinnedOrder.indexOf(app.packageName)
                if (idx >= 0) {
                    app.copy(isPinnedToHome = true, homeOrder = idx)
                } else {
                    app.copy(isPinnedToHome = false, homeOrder = Int.MAX_VALUE)
                }
            }
            state.copy(installedApps = updatedApps)
        }
    }

    // --- Featured Banner Carousel ---

    fun setBannerIndex(index: Int) {
        val banners = _uiState.value.featuredBanners
        if (banners.isEmpty()) return
        val safeIndex = ((index % banners.size) + banners.size) % banners.size
        _uiState.update { it.copy(activeBannerIndex = safeIndex) }
    }

    fun nextBanner() {
        setBannerIndex(_uiState.value.activeBannerIndex + 1)
    }

    fun previousBanner() {
        setBannerIndex(_uiState.value.activeBannerIndex - 1)
    }

    fun executeBannerAction(banner: FeaturedBannerItem) {
        when (banner.targetType) {
            BannerTargetType.OPEN_SECTION -> {
                val section = BuiltInSection.fromId(banner.targetValue) ?: BuiltInSection.MOVIES
                navigateToSection(section)
            }

            BannerTargetType.LAUNCH_APP -> {
                launchAppByPackage(banner.targetValue)
            }
        }
    }

    fun updateFeaturedBanner(updated: FeaturedBannerItem) {
        val list = _uiState.value.featuredBanners.map {
            if (it.id == updated.id) updated else it
        }
        repo.saveFeaturedBanners(list)
        _uiState.update { it.copy(featuredBanners = list) }
        showStatusMessage("تم تحديث محتوى البانر المميز")
    }

    fun resetFeaturedBanners() {
        val defaults = LauncherPreferencesRepository.defaultFeaturedBanners()
        repo.saveFeaturedBanners(defaults)
        _uiState.update { it.copy(featuredBanners = defaults, activeBannerIndex = 0) }
        showStatusMessage("تمت استعادة البانرات المميزة الافتراضية")
    }

    // --- 4. Editable Right-Side Navigation Menu ---

    fun moveMenuItemUp(itemId: String) {
        val visible = _uiState.value.visibleMenuItems.toMutableList()
        val idx = visible.indexOfFirst { it.id == itemId }
        if (idx <= 0) return
        val item = visible.removeAt(idx)
        visible.add(idx - 1, item)
        rebuildAndSaveMenuOrder(visible)
    }

    fun moveMenuItemDown(itemId: String) {
        val visible = _uiState.value.visibleMenuItems.toMutableList()
        val idx = visible.indexOfFirst { it.id == itemId }
        if (idx == -1 || idx >= visible.lastIndex) return
        val item = visible.removeAt(idx)
        visible.add(idx + 1, item)
        rebuildAndSaveMenuOrder(visible)
    }

    private fun rebuildAndSaveMenuOrder(orderedVisible: List<NavMenuItem>) {
        val nonVisible = _uiState.value.menuItems.filter { it.visibility != NavItemVisibility.VISIBLE }
        val combined = (orderedVisible + nonVisible).mapIndexed { index, navMenuItem ->
            navMenuItem.copy(orderIndex = index)
        }
        repo.saveMenuItems(combined)
        _uiState.update { it.copy(menuItems = combined) }
    }

    fun hideMenuItem(itemId: String) {
        val currentVisible = _uiState.value.visibleMenuItems
        if (currentVisible.size <= 1) {
            showStatusMessage("يجب إبقاء عنصر واحد على الأقل ظاهراً في القائمة")
            return
        }
        updateMenuItemVisibility(itemId, NavItemVisibility.HIDDEN)
        showStatusMessage("تم إخفاء العنصر — يمكنك استعادته في أي وقت من قائمة العناصر المخفية")
    }

    fun removeMenuItem(itemId: String) {
        val currentVisible = _uiState.value.visibleMenuItems
        if (currentVisible.size <= 1) {
            showStatusMessage("يجب إبقاء عنصر واحد على الأقل ظاهراً في القائمة")
            return
        }
        val target = _uiState.value.menuItems.find { it.id == itemId } ?: return
        val updated = if (target.itemType == NavItemType.BUILT_IN_SECTION) {
            _uiState.value.menuItems.map {
                if (it.id == itemId) it.copy(visibility = NavItemVisibility.REMOVED) else it
            }
        } else {
            // Custom added shortcut can be deleted or marked removed
            _uiState.value.menuItems.filterNot { it.id == itemId }
        }
        val normalized = updated.mapIndexed { index, item -> item.copy(orderIndex = index) }
        repo.saveMenuItems(normalized)
        _uiState.update { state ->
            val nextSelected = if (state.selectedMenuId == itemId) {
                normalized.firstOrNull { it.visibility == NavItemVisibility.VISIBLE }?.id
                    ?: BuiltInSection.HOME.id
            } else state.selectedMenuId
            state.copy(menuItems = normalized, selectedMenuId = nextSelected)
        }
        showStatusMessage("تمت إزالة العنصر من القائمة المرئية دون المساس بالتطبيقات أو الملفات")
    }

    fun restoreMenuItemAtPosition(itemId: String, targetVisiblePosition: Int) {
        val all = _uiState.value.menuItems
        val itemToRestore = all.find { it.id == itemId } ?: return
        val currentVisible = _uiState.value.visibleMenuItems.toMutableList()
        val safePosition = targetVisiblePosition.coerceIn(0, currentVisible.size)
        currentVisible.add(safePosition, itemToRestore.copy(visibility = NavItemVisibility.VISIBLE))

        val remainingNonVisible = all.filter {
            it.id != itemId && it.visibility != NavItemVisibility.VISIBLE
        }
        val combined = (currentVisible + remainingNonVisible).mapIndexed { index, item ->
            item.copy(orderIndex = index)
        }
        repo.saveMenuItems(combined)
        _uiState.update { it.copy(menuItems = combined) }
        showStatusMessage("تمت استعادة (${itemToRestore.displayTitle}) إلى القائمة الرئيسية")
    }

    fun renameMenuItem(itemId: String, newCustomLabel: String) {
        val trimmed = newCustomLabel.trim()
        val updated = _uiState.value.menuItems.map {
            if (it.id == itemId) it.copy(customTitle = trimmed) else it
        }
        repo.saveMenuItems(updated)
        _uiState.update { it.copy(menuItems = updated) }
        showStatusMessage("تم حفظ الاسم المخصص للقائمة بنجاح")
    }

    fun addAppShortcutToMenu(app: AppItem) {
        val shortcutId = "shortcut_app_${app.packageName}"
        val existing = _uiState.value.menuItems.find { it.id == shortcutId }
        if (existing != null) {
            restoreMenuItemAtPosition(shortcutId, _uiState.value.visibleMenuItems.size)
            return
        }
        val newItem = NavMenuItem(
            id = shortcutId,
            defaultTitleAr = app.label,
            customTitle = "",
            iconName = "app_shortcut",
            itemType = NavItemType.APP_SHORTCUT,
            targetValue = app.packageName,
            visibility = NavItemVisibility.VISIBLE,
            orderIndex = _uiState.value.visibleMenuItems.size
        )
        val visible = _uiState.value.visibleMenuItems + newItem
        rebuildAndSaveMenuOrder(visible)
        showStatusMessage("تمت إضافة اختصار (${app.label}) إلى القائمة الجانبية")
    }

    fun addSystemShortcutToMenu(shortcut: SystemShortcutOption) {
        val existing = _uiState.value.menuItems.find { it.id == shortcut.id }
        if (existing != null) {
            restoreMenuItemAtPosition(shortcut.id, _uiState.value.visibleMenuItems.size)
            return
        }
        val newItem = NavMenuItem(
            id = shortcut.id,
            defaultTitleAr = shortcut.titleAr,
            customTitle = "",
            iconName = shortcut.iconName,
            itemType = shortcut.itemType,
            targetValue = shortcut.targetAction,
            visibility = NavItemVisibility.VISIBLE,
            orderIndex = _uiState.value.visibleMenuItems.size
        )
        val visible = _uiState.value.visibleMenuItems + newItem
        rebuildAndSaveMenuOrder(visible)
        showStatusMessage("تمت إضافة اختصار (${shortcut.titleAr}) إلى القائمة الجانبية")
    }

    fun restoreDefaultMenu() {
        val defaults = LauncherPreferencesRepository.defaultMenuItems()
        repo.saveMenuItems(defaults)
        _uiState.update {
            it.copy(
                menuItems = defaults,
                selectedMenuId = BuiltInSection.HOME.id
            )
        }
        showStatusMessage("تمت استعادة القائمة الافتراضية بنجاح")
    }

    private fun updateMenuItemVisibility(itemId: String, visibility: NavItemVisibility) {
        val updated = _uiState.value.menuItems.map {
            if (it.id == itemId) it.copy(visibility = visibility) else it
        }
        repo.saveMenuItems(updated)
        _uiState.update { state ->
            val nextSelected = if (state.selectedMenuId == itemId && visibility != NavItemVisibility.VISIBLE) {
                updated.firstOrNull { it.visibility == NavItemVisibility.VISIBLE }?.id
                    ?: BuiltInSection.HOME.id
            } else state.selectedMenuId
            state.copy(menuItems = updated, selectedMenuId = nextSelected)
        }
    }

    // --- 5. Icon Customization ---

    fun updateIconConfig(transform: (IconCustomizationConfig) -> IconCustomizationConfig) {
        val updated = transform(_uiState.value.iconConfig)
        repo.saveIconConfig(updated)
        _uiState.update { it.copy(iconConfig = updated) }
    }

    fun resetIconConfig() {
        val defaults = IconCustomizationConfig()
        repo.saveIconConfig(defaults)
        _uiState.update { it.copy(iconConfig = defaults) }
        showStatusMessage("تمت استعادة إعدادات الأيقونات الافتراضية")
    }

    // --- 6. Arabic Typography & Font Management ---

    fun updateTypographyConfig(transform: (TypographyConfig) -> TypographyConfig) {
        val updated = transform(_uiState.value.typographyConfig)
        repo.saveTypographyConfig(updated)
        _uiState.update { it.copy(typographyConfig = updated) }
    }

    fun resetTypographyConfig() {
        val defaults = TypographyConfig()
        repo.saveTypographyConfig(defaults)
        _uiState.update { it.copy(typographyConfig = defaults) }
        showStatusMessage("تمت استعادة خط Cairo وإعدادات الطباعة الافتراضية")
    }

    // --- 7. Background Customization ---

    fun selectBuiltInBackground(preset: BuiltInBackgroundPreset) {
        wallpaperLoader.clearCache()
        val updated = _uiState.value.backgroundConfig.copy(
            preset = preset,
            customImageUri = "",
            pendingPreviewUri = ""
        )
        repo.saveBackgroundConfig(updated)
        _uiState.update {
            it.copy(
                backgroundConfig = updated,
                activeWallpaperBitmap = null,
                previewWallpaperBitmap = null
            )
        }
        showStatusMessage("تم تطبيق الخلفية: ${preset.titleAr}")
    }

    fun previewCustomBackgroundUri(uriString: String) {
        if (uriString.isBlank()) return
        viewModelScope.launch {
            val bitmap = wallpaperLoader.loadOptimizedWallpaper(uriString)
            if (bitmap != null) {
                _uiState.update {
                    it.copy(
                        backgroundConfig = it.backgroundConfig.copy(pendingPreviewUri = uriString),
                        previewWallpaperBitmap = bitmap
                    )
                }
                showStatusMessage("معاينة الصورة جاهزة — اضغط «تطبيق الخلفية المختارة» لاعتمادها")
            } else {
                showStatusMessage("تعذر قراءة ملف الصورة المختار. يرجى اختيار صورة بصيغة JPEG أو PNG أو WebP")
            }
        }
    }

    fun applyPreviewedCustomBackground() {
        val previewUri = _uiState.value.backgroundConfig.pendingPreviewUri
        val previewBmp = _uiState.value.previewWallpaperBitmap
        if (previewUri.isBlank() || previewBmp == null) return
        val updated = _uiState.value.backgroundConfig.copy(
            customImageUri = previewUri,
            pendingPreviewUri = ""
        )
        repo.saveBackgroundConfig(updated)
        _uiState.update {
            it.copy(
                backgroundConfig = updated,
                activeWallpaperBitmap = previewBmp,
                previewWallpaperBitmap = null
            )
        }
        showStatusMessage("تم تطبيق صورة الخلفية المخصصة وحفظها بنجاح")
    }

    fun cancelCustomBackgroundPreview() {
        _uiState.update {
            it.copy(
                backgroundConfig = it.backgroundConfig.copy(pendingPreviewUri = ""),
                previewWallpaperBitmap = null
            )
        }
    }

    fun updateBackgroundScaleMode(mode: BackgroundScaleMode) {
        val updated = _uiState.value.backgroundConfig.copy(scaleMode = mode)
        repo.saveBackgroundConfig(updated)
        _uiState.update { it.copy(backgroundConfig = updated) }
    }

    fun resetBackgroundToDefault() {
        wallpaperLoader.clearCache()
        val defaults = BackgroundConfig()
        repo.saveBackgroundConfig(defaults)
        _uiState.update {
            it.copy(
                backgroundConfig = defaults,
                activeWallpaperBitmap = null,
                previewWallpaperBitmap = null
            )
        }
        showStatusMessage("تمت استعادة الخلفية الكحلية المرجعية الافتراضية")
    }

    // --- 9. Clock & Date Settings ---

    fun updateClockDateConfig(transform: (ClockDateConfig) -> ClockDateConfig) {
        val updated = transform(_uiState.value.clockDateConfig)
        repo.saveClockDateConfig(updated)
        _uiState.update { it.copy(clockDateConfig = updated) }
    }

    // --- 11. Home Screen Layout, Remote Navigation, Performance & Reset ---

    fun updateHomeLayoutConfig(transform: (HomeLayoutConfig) -> HomeLayoutConfig) {
        val updated = transform(_uiState.value.homeLayoutConfig)
        repo.saveHomeLayoutConfig(updated)
        _uiState.update { it.copy(homeLayoutConfig = updated) }
    }

    fun updateRemoteNavConfig(transform: (RemoteNavigationConfig) -> RemoteNavigationConfig) {
        val updated = transform(_uiState.value.remoteNavConfig)
        repo.saveRemoteNavigationConfig(updated)
        _uiState.update { it.copy(remoteNavConfig = updated) }
    }

    fun updatePerformanceConfig(transform: (PerformanceConfig) -> PerformanceConfig) {
        val updated = transform(_uiState.value.performanceConfig)
        repo.savePerformanceConfig(updated)
        _uiState.update { it.copy(performanceConfig = updated) }
    }

    fun restoreAllDefaultSettings() {
        repo.resetAllToDefaults()
        wallpaperLoader.clearCache()
        favoritePackages = emptySet()
        pinnedHomePackages = emptyList()
        customAppCategories = emptyMap()

        _uiState.update {
            LauncherUiState(
                isLoadingApps = false,
                menuItems = LauncherPreferencesRepository.defaultMenuItems(),
                selectedMenuId = BuiltInSection.HOME.id,
                activeSection = BuiltInSection.SETTINGS,
                activeSettingsSubScreen = SettingsSubScreen.RESTORE_DEFAULTS,
                installedApps = it.installedApps,
                featuredBanners = LauncherPreferencesRepository.defaultFeaturedBanners(),
                activeBannerIndex = 0,
                iconConfig = IconCustomizationConfig(),
                typographyConfig = TypographyConfig(),
                backgroundConfig = BackgroundConfig(),
                activeWallpaperBitmap = null,
                previewWallpaperBitmap = null,
                clockDateConfig = ClockDateConfig(),
                homeLayoutConfig = HomeLayoutConfig(),
                remoteNavConfig = RemoteNavigationConfig(),
                performanceConfig = PerformanceConfig(),
                networkStatus = it.networkStatus,
                currentFileDirPath = it.currentFileDirPath,
                localFiles = it.localFiles,
                statusBannerMessageAr = "تمت استعادة جميع إعدادات المشغل إلى الوضع الافتراضي الأصلي"
            )
        }
        refreshInstalledApps()
    }

    // --- Local Files Section ---

    fun loadDirectoryFiles(path: String?) {
        viewModelScope.launch {
            val (resolvedPath, items) = appDiscovery.listLocalStorageFiles(path)
            _uiState.update {
                it.copy(
                    currentFileDirPath = resolvedPath,
                    localFiles = items
                )
            }
        }
    }

    fun openHomeLauncherSettings() {
        appDiscovery.openHomeLauncherChooser()
    }

    fun openAndroidSystemSettings(action: String) {
        appDiscovery.launchSystemSettingAction(action)
    }

    fun showStatusMessage(messageAr: String) {
        _uiState.update { it.copy(statusBannerMessageAr = messageAr) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusBannerMessageAr = null) }
    }
}
