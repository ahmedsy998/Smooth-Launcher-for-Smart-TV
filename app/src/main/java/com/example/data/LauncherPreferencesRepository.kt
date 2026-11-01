package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

class LauncherPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "smooth_tv_launcher_prefs"
        private const val KEY_MENU_ITEMS = "nav_menu_items_json"
        private const val KEY_BANNERS = "featured_banners_json"
        private const val KEY_FAVORITE_PACKAGES = "favorite_packages_set"
        private const val KEY_PINNED_HOME_PACKAGES = "pinned_home_packages_json"
        private const val KEY_CUSTOM_APP_CATEGORIES = "custom_app_categories_json"
        private const val KEY_ICON_CONFIG = "icon_customization_json"
        private const val KEY_TYPOGRAPHY_CONFIG = "typography_config_json"
        private const val KEY_BACKGROUND_CONFIG = "background_config_json"
        private const val KEY_CLOCK_CONFIG = "clock_date_config_json"
        private const val KEY_HOME_LAYOUT_CONFIG = "home_layout_config_json"
        private const val KEY_REMOTE_NAV_CONFIG = "remote_nav_config_json"
        private const val KEY_PERFORMANCE_CONFIG = "performance_config_json"

        fun defaultMenuItems(): List<NavMenuItem> = listOf(
            NavMenuItem(
                id = BuiltInSection.HOME.id,
                defaultTitleAr = BuiltInSection.HOME.defaultTitleAr,
                iconName = BuiltInSection.HOME.defaultIconName,
                itemType = NavItemType.BUILT_IN_SECTION,
                targetValue = BuiltInSection.HOME.id,
                visibility = NavItemVisibility.VISIBLE,
                orderIndex = 0
            ),
            NavMenuItem(
                id = BuiltInSection.APPLICATIONS.id,
                defaultTitleAr = BuiltInSection.APPLICATIONS.defaultTitleAr,
                iconName = BuiltInSection.APPLICATIONS.defaultIconName,
                itemType = NavItemType.BUILT_IN_SECTION,
                targetValue = BuiltInSection.APPLICATIONS.id,
                visibility = NavItemVisibility.VISIBLE,
                orderIndex = 1
            ),
            NavMenuItem(
                id = BuiltInSection.MOVIES.id,
                defaultTitleAr = BuiltInSection.MOVIES.defaultTitleAr,
                iconName = BuiltInSection.MOVIES.defaultIconName,
                itemType = NavItemType.BUILT_IN_SECTION,
                targetValue = BuiltInSection.MOVIES.id,
                visibility = NavItemVisibility.VISIBLE,
                orderIndex = 2
            ),
            NavMenuItem(
                id = BuiltInSection.SERIES.id,
                defaultTitleAr = BuiltInSection.SERIES.defaultTitleAr,
                iconName = BuiltInSection.SERIES.defaultIconName,
                itemType = NavItemType.BUILT_IN_SECTION,
                targetValue = BuiltInSection.SERIES.id,
                visibility = NavItemVisibility.VISIBLE,
                orderIndex = 3
            ),
            NavMenuItem(
                id = BuiltInSection.FAVORITES.id,
                defaultTitleAr = BuiltInSection.FAVORITES.defaultTitleAr,
                iconName = BuiltInSection.FAVORITES.defaultIconName,
                itemType = NavItemType.BUILT_IN_SECTION,
                targetValue = BuiltInSection.FAVORITES.id,
                visibility = NavItemVisibility.VISIBLE,
                orderIndex = 4
            ),
            NavMenuItem(
                id = BuiltInSection.FILES.id,
                defaultTitleAr = BuiltInSection.FILES.defaultTitleAr,
                iconName = BuiltInSection.FILES.defaultIconName,
                itemType = NavItemType.BUILT_IN_SECTION,
                targetValue = BuiltInSection.FILES.id,
                visibility = NavItemVisibility.VISIBLE,
                orderIndex = 5
            ),
            NavMenuItem(
                id = BuiltInSection.SETTINGS.id,
                defaultTitleAr = BuiltInSection.SETTINGS.defaultTitleAr,
                iconName = BuiltInSection.SETTINGS.defaultIconName,
                itemType = NavItemType.BUILT_IN_SECTION,
                targetValue = BuiltInSection.SETTINGS.id,
                visibility = NavItemVisibility.VISIBLE,
                orderIndex = 6
            )
        )

        fun defaultFeaturedBanners(): List<FeaturedBannerItem> = listOf(
            FeaturedBannerItem(
                id = "banner_1",
                title = "اكتشف المحتوى المميز",
                subtitle = "أفلام • مسلسلات • تطبيقات",
                actionLabel = "استكشاف الأفلام",
                presetThemeId = 0,
                targetType = BannerTargetType.OPEN_SECTION,
                targetValue = BuiltInSection.MOVIES.id
            ),
            FeaturedBannerItem(
                id = "banner_2",
                title = "عالم المسلسلات والعروض الحصرية",
                subtitle = "شاهد أحدث الحلقات عبر تطبيقات البث المثبتة على جهازك",
                actionLabel = "فتح المسلسلات",
                presetThemeId = 1,
                targetType = BannerTargetType.OPEN_SECTION,
                targetValue = BuiltInSection.SERIES.id
            ),
            FeaturedBannerItem(
                id = "banner_3",
                title = "مكتبة التطبيقات والملفات المحلية",
                subtitle = "وصول فوري إلى جميع تطبيقات التلفاز والوسائط المخزنة",
                actionLabel = "كل التطبيقات",
                presetThemeId = 2,
                targetType = BannerTargetType.OPEN_SECTION,
                targetValue = BuiltInSection.APPLICATIONS.id
            )
        )

        fun supportedSystemShortcuts(): List<SystemShortcutOption> = listOf(
            SystemShortcutOption(
                id = "shortcut_wifi_settings",
                titleAr = "إعدادات الشبكة والواي فاي",
                iconName = "wifi",
                itemType = NavItemType.SYSTEM_SHORTCUT,
                targetAction = Settings.ACTION_WIFI_SETTINGS
            ),
            SystemShortcutOption(
                id = "shortcut_display_settings",
                titleAr = "إعدادات الشاشة والعرض",
                iconName = "display",
                itemType = NavItemType.SYSTEM_SHORTCUT,
                targetAction = Settings.ACTION_DISPLAY_SETTINGS
            ),
            SystemShortcutOption(
                id = "shortcut_sound_settings",
                titleAr = "إعدادات الصوت",
                iconName = "sound",
                itemType = NavItemType.SYSTEM_SHORTCUT,
                targetAction = Settings.ACTION_SOUND_SETTINGS
            ),
            SystemShortcutOption(
                id = "shortcut_manage_apps",
                titleAr = "إدارة تطبيقات النظام",
                iconName = "apps",
                itemType = NavItemType.SYSTEM_SHORTCUT,
                targetAction = Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS
            ),
            SystemShortcutOption(
                id = "shortcut_system_settings",
                titleAr = "إعدادات نظام أندرويد",
                iconName = "settings",
                itemType = NavItemType.SYSTEM_SHORTCUT,
                targetAction = Settings.ACTION_SETTINGS
            ),
            SystemShortcutOption(
                id = "shortcut_media_movies",
                titleAr = "اختصار وسائط الفيديو المحلية",
                iconName = "movies",
                itemType = NavItemType.MEDIA_SHORTCUT,
                targetAction = BuiltInSection.FILES.id
            )
        )
    }

    fun loadMenuItems(): List<NavMenuItem> {
        val raw = prefs.getString(KEY_MENU_ITEMS, null) ?: return defaultMenuItems()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<NavMenuItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    NavMenuItem(
                        id = obj.getString("id"),
                        defaultTitleAr = obj.getString("defaultTitleAr"),
                        customTitle = obj.optString("customTitle", ""),
                        iconName = obj.optString("iconName", "apps"),
                        itemType = runCatching {
                            NavItemType.valueOf(obj.optString("itemType", NavItemType.BUILT_IN_SECTION.name))
                        }.getOrDefault(NavItemType.BUILT_IN_SECTION),
                        targetValue = obj.optString("targetValue", obj.getString("id")),
                        visibility = runCatching {
                            NavItemVisibility.valueOf(obj.optString("visibility", NavItemVisibility.VISIBLE.name))
                        }.getOrDefault(NavItemVisibility.VISIBLE),
                        orderIndex = obj.optInt("orderIndex", i)
                    )
                )
            }
            // Guarantee Settings is always reachable or restorable if all items were hidden
            if (list.isEmpty()) defaultMenuItems() else list.sortedBy { it.orderIndex }
        } catch (_: Exception) {
            defaultMenuItems()
        }
    }

    fun saveMenuItems(items: List<NavMenuItem>) {
        val array = JSONArray()
        items.forEachIndexed { index, item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("defaultTitleAr", item.defaultTitleAr)
                put("customTitle", item.customTitle)
                put("iconName", item.iconName)
                put("itemType", item.itemType.name)
                put("targetValue", item.targetValue)
                put("visibility", item.visibility.name)
                put("orderIndex", index)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_MENU_ITEMS, array.toString()).apply()
    }

    fun loadFeaturedBanners(): List<FeaturedBannerItem> {
        val raw = prefs.getString(KEY_BANNERS, null) ?: return defaultFeaturedBanners()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<FeaturedBannerItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    FeaturedBannerItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        subtitle = obj.getString("subtitle"),
                        actionLabel = obj.optString("actionLabel", "تشغيل الآن"),
                        presetThemeId = obj.optInt("presetThemeId", i % 3),
                        customImageUri = obj.optString("customImageUri", ""),
                        targetType = runCatching {
                            BannerTargetType.valueOf(obj.optString("targetType", BannerTargetType.OPEN_SECTION.name))
                        }.getOrDefault(BannerTargetType.OPEN_SECTION),
                        targetValue = obj.optString("targetValue", BuiltInSection.MOVIES.id)
                    )
                )
            }
            if (list.isEmpty()) defaultFeaturedBanners() else list
        } catch (_: Exception) {
            defaultFeaturedBanners()
        }
    }

    fun saveFeaturedBanners(banners: List<FeaturedBannerItem>) {
        val array = JSONArray()
        banners.forEach { b ->
            val obj = JSONObject().apply {
                put("id", b.id)
                put("title", b.title)
                put("subtitle", b.subtitle)
                put("actionLabel", b.actionLabel)
                put("presetThemeId", b.presetThemeId)
                put("customImageUri", b.customImageUri)
                put("targetType", b.targetType.name)
                put("targetValue", b.targetValue)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_BANNERS, array.toString()).apply()
    }

    fun loadFavoritePackages(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITE_PACKAGES, emptySet())?.toSet() ?: emptySet()
    }

    fun saveFavoritePackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITE_PACKAGES, packages).apply()
    }

    fun loadPinnedHomePackages(): List<String> {
        val raw = prefs.getString(KEY_PINNED_HOME_PACKAGES, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            List(array.length()) { i -> array.getString(i) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun savePinnedHomePackages(packages: List<String>) {
        val array = JSONArray()
        packages.forEach { array.put(it) }
        prefs.edit().putString(KEY_PINNED_HOME_PACKAGES, array.toString()).apply()
    }

    fun loadCustomAppCategories(): Map<String, AppCategory> {
        val raw = prefs.getString(KEY_CUSTOM_APP_CATEGORIES, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val result = mutableMapOf<String, AppCategory>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val pkg = keys.next()
                val catName = obj.optString(pkg)
                runCatching { AppCategory.valueOf(catName) }.getOrNull()?.let {
                    result[pkg] = it
                }
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun saveCustomAppCategories(map: Map<String, AppCategory>) {
        val obj = JSONObject()
        map.forEach { (pkg, cat) -> obj.put(pkg, cat.name) }
        prefs.edit().putString(KEY_CUSTOM_APP_CATEGORIES, obj.toString()).apply()
    }

    fun loadIconConfig(): IconCustomizationConfig {
        val raw = prefs.getString(KEY_ICON_CONFIG, null) ?: return IconCustomizationConfig()
        return try {
            val obj = JSONObject(raw)
            val preset = IconSizePreset.fromId(obj.optString("sizePreset", IconSizePreset.MEDIUM.id))
            IconCustomizationConfig(
                shape = IconShapeOption.fromId(obj.optString("shape", IconShapeOption.ROUNDED_RECT.id)),
                sizePreset = preset,
                customSizeDp = obj.optDouble("customSizeDp", preset.defaultDp.toDouble()).toFloat()
                    .coerceIn(64f, 180f),
                showLabels = obj.optBoolean("showLabels", true),
                labelFontSizeSp = obj.optDouble("labelFontSizeSp", 13.0).toFloat().coerceIn(9f, 22f),
                iconSpacingDp = obj.optDouble("iconSpacingDp", 14.0).toFloat().coerceIn(4f, 36f),
                tileCornerRadiusDp = obj.optDouble("tileCornerRadiusDp", 18.0).toFloat().coerceIn(0f, 44f),
                tileBackgroundOpacity = obj.optDouble("tileBackgroundOpacity", 0.88).toFloat().coerceIn(0.15f, 1.0f),
                enableSelectedGlow = obj.optBoolean("enableSelectedGlow", true),
                focusBorderThicknessDp = obj.optDouble("focusBorderThicknessDp", 2.5).toFloat().coerceIn(1f, 6f)
            )
        } catch (_: Exception) {
            IconCustomizationConfig()
        }
    }

    fun saveIconConfig(config: IconCustomizationConfig) {
        val obj = JSONObject().apply {
            put("shape", config.shape.id)
            put("sizePreset", config.sizePreset.id)
            put("customSizeDp", config.customSizeDp.toDouble())
            put("showLabels", config.showLabels)
            put("labelFontSizeSp", config.labelFontSizeSp.toDouble())
            put("iconSpacingDp", config.iconSpacingDp.toDouble())
            put("tileCornerRadiusDp", config.tileCornerRadiusDp.toDouble())
            put("tileBackgroundOpacity", config.tileBackgroundOpacity.toDouble())
            put("enableSelectedGlow", config.enableSelectedGlow)
            put("focusBorderThicknessDp", config.focusBorderThicknessDp.toDouble())
        }
        prefs.edit().putString(KEY_ICON_CONFIG, obj.toString()).apply()
    }

    fun loadTypographyConfig(): TypographyConfig {
        val raw = prefs.getString(KEY_TYPOGRAPHY_CONFIG, null) ?: return TypographyConfig()
        return try {
            val obj = JSONObject(raw)
            TypographyConfig(
                fontOption = ArabicFontOption.fromId(obj.optString("fontOption", ArabicFontOption.CAIRO.id)),
                fontWeightOption = FontWeightOption.fromId(obj.optString("fontWeightOption", FontWeightOption.MEDIUM.id)),
                globalFontScale = obj.optDouble("globalFontScale", 1.0).toFloat().coerceIn(0.8f, 1.35f),
                appLabelSizeSp = obj.optDouble("appLabelSizeSp", 13.0).toFloat().coerceIn(9f, 22f),
                clockFontSizeSp = obj.optDouble("clockFontSizeSp", 26.0).toFloat().coerceIn(16f, 42f),
                dateFontSizeSp = obj.optDouble("dateFontSizeSp", 13.0).toFloat().coerceIn(10f, 22f),
                navMenuFontSizeSp = obj.optDouble("navMenuFontSizeSp", 15.0).toFloat().coerceIn(11f, 22f)
            )
        } catch (_: Exception) {
            TypographyConfig()
        }
    }

    fun saveTypographyConfig(config: TypographyConfig) {
        val obj = JSONObject().apply {
            put("fontOption", config.fontOption.id)
            put("fontWeightOption", config.fontWeightOption.id)
            put("globalFontScale", config.globalFontScale.toDouble())
            put("appLabelSizeSp", config.appLabelSizeSp.toDouble())
            put("clockFontSizeSp", config.clockFontSizeSp.toDouble())
            put("dateFontSizeSp", config.dateFontSizeSp.toDouble())
            put("navMenuFontSizeSp", config.navMenuFontSizeSp.toDouble())
        }
        prefs.edit().putString(KEY_TYPOGRAPHY_CONFIG, obj.toString()).apply()
    }

    fun loadBackgroundConfig(): BackgroundConfig {
        val raw = prefs.getString(KEY_BACKGROUND_CONFIG, null) ?: return BackgroundConfig()
        return try {
            val obj = JSONObject(raw)
            BackgroundConfig(
                preset = BuiltInBackgroundPreset.fromId(obj.optString("preset", BuiltInBackgroundPreset.DARK_NAVY_REFERENCE.id)),
                customImageUri = obj.optString("customImageUri", ""),
                pendingPreviewUri = "",
                scaleMode = BackgroundScaleMode.fromId(obj.optString("scaleMode", BackgroundScaleMode.CENTER_CROP.id))
            )
        } catch (_: Exception) {
            BackgroundConfig()
        }
    }

    fun saveBackgroundConfig(config: BackgroundConfig) {
        val obj = JSONObject().apply {
            put("preset", config.preset.id)
            put("customImageUri", config.customImageUri)
            put("scaleMode", config.scaleMode.id)
        }
        prefs.edit().putString(KEY_BACKGROUND_CONFIG, obj.toString()).apply()
    }

    fun loadClockDateConfig(): ClockDateConfig {
        val raw = prefs.getString(KEY_CLOCK_CONFIG, null) ?: return ClockDateConfig()
        return try {
            val obj = JSONObject(raw)
            ClockDateConfig(
                is24HourFormat = obj.optBoolean("is24HourFormat", true),
                showClock = obj.optBoolean("showClock", true),
                showDate = obj.optBoolean("showDate", true),
                clockSizeSp = obj.optDouble("clockSizeSp", 26.0).toFloat().coerceIn(16f, 42f),
                dateSizeSp = obj.optDouble("dateSizeSp", 13.0).toFloat().coerceIn(10f, 22f)
            )
        } catch (_: Exception) {
            ClockDateConfig()
        }
    }

    fun saveClockDateConfig(config: ClockDateConfig) {
        val obj = JSONObject().apply {
            put("is24HourFormat", config.is24HourFormat)
            put("showClock", config.showClock)
            put("showDate", config.showDate)
            put("clockSizeSp", config.clockSizeSp.toDouble())
            put("dateSizeSp", config.dateSizeSp.toDouble())
        }
        prefs.edit().putString(KEY_CLOCK_CONFIG, obj.toString()).apply()
    }

    fun loadHomeLayoutConfig(): HomeLayoutConfig {
        val raw = prefs.getString(KEY_HOME_LAYOUT_CONFIG, null) ?: return HomeLayoutConfig()
        return try {
            val obj = JSONObject(raw)
            HomeLayoutConfig(
                autoRotateBanners = obj.optBoolean("autoRotateBanners", false),
                bannerRotationIntervalSec = obj.optInt("bannerRotationIntervalSec", 8).coerceIn(4, 30),
                showBannerTextAndAction = obj.optBoolean("showBannerTextAndAction", true),
                maxHomeRowApps = obj.optInt("maxHomeRowApps", 6).coerceIn(3, 12),
                compactNavMenu = obj.optBoolean("compactNavMenu", true)
            )
        } catch (_: Exception) {
            HomeLayoutConfig()
        }
    }

    fun saveHomeLayoutConfig(config: HomeLayoutConfig) {
        val obj = JSONObject().apply {
            put("autoRotateBanners", config.autoRotateBanners)
            put("bannerRotationIntervalSec", config.bannerRotationIntervalSec)
            put("showBannerTextAndAction", config.showBannerTextAndAction)
            put("maxHomeRowApps", config.maxHomeRowApps)
            put("compactNavMenu", config.compactNavMenu)
        }
        prefs.edit().putString(KEY_HOME_LAYOUT_CONFIG, obj.toString()).apply()
    }

    fun loadRemoteNavigationConfig(): RemoteNavigationConfig {
        val raw = prefs.getString(KEY_REMOTE_NAV_CONFIG, null) ?: return RemoteNavigationConfig()
        return try {
            val obj = JSONObject(raw)
            RemoteNavigationConfig(
                rememberLastSettingsFocus = obj.optBoolean("rememberLastSettingsFocus", true),
                wrapHorizontalLists = obj.optBoolean("wrapHorizontalLists", false),
                confirmBeforeRemovingMenuItem = obj.optBoolean("confirmBeforeRemovingMenuItem", true)
            )
        } catch (_: Exception) {
            RemoteNavigationConfig()
        }
    }

    fun saveRemoteNavigationConfig(config: RemoteNavigationConfig) {
        val obj = JSONObject().apply {
            put("rememberLastSettingsFocus", config.rememberLastSettingsFocus)
            put("wrapHorizontalLists", config.wrapHorizontalLists)
            put("confirmBeforeRemovingMenuItem", config.confirmBeforeRemovingMenuItem)
        }
        prefs.edit().putString(KEY_REMOTE_NAV_CONFIG, obj.toString()).apply()
    }

    fun loadPerformanceConfig(): PerformanceConfig {
        val raw = prefs.getString(KEY_PERFORMANCE_CONFIG, null) ?: return PerformanceConfig()
        return try {
            val obj = JSONObject(raw)
            PerformanceConfig(
                lowEffectsMode = obj.optBoolean("lowEffectsMode", false),
                disableDecorativeAccents = obj.optBoolean("disableDecorativeAccents", false),
                minimizeAnimations = obj.optBoolean("minimizeAnimations", false)
            )
        } catch (_: Exception) {
            PerformanceConfig()
        }
    }

    fun savePerformanceConfig(config: PerformanceConfig) {
        val obj = JSONObject().apply {
            put("lowEffectsMode", config.lowEffectsMode)
            put("disableDecorativeAccents", config.disableDecorativeAccents)
            put("minimizeAnimations", config.minimizeAnimations)
        }
        prefs.edit().putString(KEY_PERFORMANCE_CONFIG, obj.toString()).apply()
    }

    fun resetAllToDefaults() {
        prefs.edit().clear().apply()
    }
}
