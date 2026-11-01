package com.example.model

import android.graphics.drawable.Drawable

enum class BuiltInSection(
    val id: String,
    val defaultTitleAr: String,
    val defaultIconName: String
) {
    HOME("home", "الرئيسية", "home"),
    APPLICATIONS("applications", "التطبيقات", "apps"),
    MOVIES("movies", "الأفلام", "movies"),
    SERIES("series", "المسلسلات", "tv"),
    FAVORITES("favorites", "المفضلة", "star"),
    FILES("files", "الملفات", "folder"),
    SETTINGS("settings", "الإعدادات", "settings");

    companion object {
        fun fromId(id: String): BuiltInSection? = entries.find { it.id == id }
    }
}

enum class NavItemType {
    BUILT_IN_SECTION,
    APP_SHORTCUT,
    SYSTEM_SHORTCUT,
    MEDIA_SHORTCUT
}

enum class NavItemVisibility {
    VISIBLE,
    HIDDEN,
    REMOVED
}

data class NavMenuItem(
    val id: String,
    val defaultTitleAr: String,
    val customTitle: String = "",
    val iconName: String,
    val itemType: NavItemType,
    val targetValue: String,
    val visibility: NavItemVisibility = NavItemVisibility.VISIBLE,
    val orderIndex: Int
) {
    val displayTitle: String
        get() = customTitle.trim().ifEmpty { defaultTitleAr }
}

enum class AppCategory(val labelAr: String) {
    STREAMING_MOVIES("أفلام وسينما"),
    STREAMING_SERIES("مسلسلات وبث مباشر"),
    MUSIC_AUDIO("موسيقى وصوتيات"),
    SYSTEM_TOOLS("أدوات النظام"),
    GENERAL("تطبيقات عامة")
}

data class AppItem(
    val packageName: String,
    val activityName: String = "",
    val label: String,
    val icon: Drawable? = null,
    val banner: Drawable? = null,
    val isTvOptimized: Boolean = false,
    val category: AppCategory = AppCategory.GENERAL,
    val isFavorite: Boolean = false,
    val isPinnedToHome: Boolean = false,
    val homeOrder: Int = Int.MAX_VALUE,
    val brandAccentColorHex: Long? = null
)

enum class BannerTargetType(val labelAr: String) {
    OPEN_SECTION("فتح قسم في المشغل"),
    LAUNCH_APP("تشغيل تطبيق مثبت")
}

data class FeaturedBannerItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val actionLabel: String = "تشغيل الآن",
    val presetThemeId: Int = 0,
    val customImageUri: String = "",
    val targetType: BannerTargetType = BannerTargetType.OPEN_SECTION,
    val targetValue: String = BuiltInSection.MOVIES.id
)

enum class IconShapeOption(val id: String, val labelAr: String, val labelEn: String) {
    ORIGINAL("original", "الأيقونة الأصلية", "Original application icon"),
    SQUARE("square", "مربع", "Square"),
    ROUNDED_SQUARE("rounded_square", "مربع بحواف دائرية", "Rounded square"),
    CIRCLE("circle", "دائري", "Circle"),
    ROUNDED_RECT("rounded_rect", "بطاقة مستطيلة دائرية الحواف", "Rounded rectangular tile");

    companion object {
        fun fromId(id: String): IconShapeOption = entries.find { it.id == id } ?: ROUNDED_RECT
    }
}

enum class IconSizePreset(val id: String, val labelAr: String, val labelEn: String, val defaultDp: Float) {
    EXTRA_SMALL("xs", "صغير جداً", "Extra Small", 76f),
    SMALL("sm", "صغير", "Small", 92f),
    MEDIUM("md", "متوسط", "Medium", 110f),
    LARGE("lg", "كبير", "Large", 128f),
    EXTRA_LARGE("xl", "كبير جداً", "Extra Large", 148f);

    companion object {
        fun fromId(id: String): IconSizePreset = entries.find { it.id == id } ?: MEDIUM
    }
}

data class IconCustomizationConfig(
    val shape: IconShapeOption = IconShapeOption.ROUNDED_RECT,
    val sizePreset: IconSizePreset = IconSizePreset.MEDIUM,
    val customSizeDp: Float = 110f,
    val showLabels: Boolean = true,
    val labelFontSizeSp: Float = 13f,
    val iconSpacingDp: Float = 14f,
    val tileCornerRadiusDp: Float = 18f,
    val tileBackgroundOpacity: Float = 0.88f,
    val enableSelectedGlow: Boolean = true,
    val focusBorderThicknessDp: Float = 2.5f
)

enum class ArabicFontOption(
    val id: String,
    val displayNameAr: String,
    val displayNameEn: String,
    val sampleTextAr: String = "أهلاً بك في مشغل التلفاز السلس — تجربة عربية متكاملة"
) {
    CAIRO("cairo", "القاهرة (Cairo — الافتراضي)", "Cairo"),
    AMIRI("amiri", "أميري (Amiri)", "Amiri"),
    TAJAWAL("tajawal", "تجول (Tajawal)", "Tajawal"),
    IBM_PLEX_SANS_ARABIC("ibm_plex_sans_arabic", "آي بي إم بليكس عربي (IBM Plex Sans Arabic)", "IBM Plex Sans Arabic"),
    NOTO_SANS_ARABIC("noto_sans_arabic", "نوتو سانس عربي (Noto Sans Arabic)", "Noto Sans Arabic"),
    NOTO_NASKH_ARABIC("noto_naskh_arabic", "نوتو نسخ عربي (Noto Naskh Arabic)", "Noto Naskh Arabic"),
    READEX_PRO("readex_pro", "ريدكس برو (Readex Pro)", "Readex Pro"),
    ALMARAI("almarai", "المراعي (Almarai)", "Almarai");

    companion object {
        fun fromId(id: String): ArabicFontOption = entries.find { it.id == id } ?: CAIRO
    }
}

enum class FontWeightOption(val id: String, val labelAr: String, val weightValue: Int) {
    NORMAL("normal", "عادي (Regular)", 400),
    MEDIUM("medium", "متوسط (Medium)", 500),
    SEMI_BOLD("semibold", "شبه عريض (SemiBold)", 600),
    BOLD("bold", "عريض (Bold)", 700);

    companion object {
        fun fromId(id: String): FontWeightOption = entries.find { it.id == id } ?: MEDIUM
    }
}

data class TypographyConfig(
    val fontOption: ArabicFontOption = ArabicFontOption.CAIRO,
    val fontWeightOption: FontWeightOption = FontWeightOption.MEDIUM,
    val globalFontScale: Float = 1.0f,
    val appLabelSizeSp: Float = 13f,
    val clockFontSizeSp: Float = 26f,
    val dateFontSizeSp: Float = 13f,
    val navMenuFontSizeSp: Float = 15f
)

enum class BuiltInBackgroundPreset(
    val id: String,
    val titleAr: String,
    val subtitleAr: String
) {
    DARK_NAVY_REFERENCE("dark_navy", "كحلي ليلي متدرج (التصميم المرجعي)", "Dark navy gradients"),
    BLUE_PURPLE_AURORA("blue_purple", "تدرجات الأزرق والبنفسجي السينمائية", "Blue and purple gradients"),
    NATURAL_LANDSCAPE("natural_landscape", "مشهد طبيعي جبلي عند الغروب", "Natural landscapes"),
    NIGHT_CITY_SCENE("night_city", "أفق المدينة الليلي المضيء", "Night city scenes"),
    MINIMAL_ABSTRACT("minimal_abstract", "تجريدي هادئ بسيط", "Minimal abstract backgrounds");

    companion object {
        fun fromId(id: String): BuiltInBackgroundPreset =
            entries.find { it.id == id } ?: DARK_NAVY_REFERENCE
    }
}

enum class BackgroundScaleMode(val id: String, val labelAr: String, val labelEn: String) {
    FILL_SCREEN("fill_screen", "ملء الشاشة", "Fill Screen"),
    FIT_IMAGE("fit_image", "احتواء الصورة بالكامل", "Fit Image"),
    CENTER_CROP("center_crop", "قص وتوسيط متناسق", "Center Crop");

    companion object {
        fun fromId(id: String): BackgroundScaleMode =
            entries.find { it.id == id } ?: CENTER_CROP
    }
}

data class BackgroundConfig(
    val preset: BuiltInBackgroundPreset = BuiltInBackgroundPreset.DARK_NAVY_REFERENCE,
    val customImageUri: String = "",
    val pendingPreviewUri: String = "",
    val scaleMode: BackgroundScaleMode = BackgroundScaleMode.CENTER_CROP
)

data class ClockDateConfig(
    val is24HourFormat: Boolean = true,
    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val clockSizeSp: Float = 26f,
    val dateSizeSp: Float = 13f
)

data class HomeLayoutConfig(
    val autoRotateBanners: Boolean = false,
    val bannerRotationIntervalSec: Int = 8,
    val showBannerTextAndAction: Boolean = true,
    val maxHomeRowApps: Int = 6,
    val compactNavMenu: Boolean = true
)

data class RemoteNavigationConfig(
    val rememberLastSettingsFocus: Boolean = true,
    val wrapHorizontalLists: Boolean = false,
    val confirmBeforeRemovingMenuItem: Boolean = true
)

data class PerformanceConfig(
    val lowEffectsMode: Boolean = false,
    val disableDecorativeAccents: Boolean = false,
    val minimizeAnimations: Boolean = false
)

sealed class NetworkStatus(val labelAr: String, val isConnectedWithInternet: Boolean) {
    data object ConnectedValidated : NetworkStatus("متصل بالإنترنت (Wi-Fi فعال)", true)
    data object ConnectedNoInternet : NetworkStatus("متصل بالشبكة بدون وصول للإنترنت", false)
    data object Disconnected : NetworkStatus("غير متصل بالإنترنت", false)
}

enum class SettingsSubScreen(val id: String, val titleAr: String, val titleEn: String, val descriptionAr: String) {
    GENERAL("general", "الإعدادات العامة", "General Settings", "إعدادات المشغل الأساسية واللغة والتشغيل التلقائي"),
    CUSTOMIZE_MENU("customize_menu", "تخصيص القائمة الرئيسية", "Customize Home Menu", "ترتيب، إخفاء، إزالة، إعادة تسمية وإضافة اختصارات للقائمة الجانبية"),
    ICON_CUSTOMIZATION("icon_customization", "تخصيص الأيقونات", "Icon Customization", "تغيير شكل الأيقونات، الحجم، التباعد، الشفافية وتوهج التركيز"),
    FONTS_TYPOGRAPHY("fonts_typography", "الخطوط والطباعة", "Fonts and Typography", "اختيار الخط العربي (Cairo و7 خطوط أخرى)، السُمك وأحجام النصوص"),
    BACKGROUND_APPEARANCE("background_appearance", "الخلفية والمظهر", "Background and Appearance", "خلفيات مدمجة أو استيراد صورة مخصصة من الملفات مع المعاينة"),
    CLOCK_DATE("clock_date", "الساعة والتاريخ", "Clock and Date", "نظام 12/24 ساعة، إظهار أو إخفاء الوقت والتاريخ وتعديل الحجم"),
    HOME_LAYOUT("home_layout", "تخطيط الشاشة الرئيسية", "Home Screen Layout", "إدارة البانر السينمائي المميز وتطبيقات الصف السفلي"),
    REMOTE_NAVIGATION("remote_navigation", "التحكم عن بعد", "Remote Navigation", "ضبط استجابة أزرار الريموت، حفظ موقع التركيز والتنقل"),
    PERFORMANCE("performance", "خيارات الأداء", "Performance Options", "وضع التأثيرات المنخفضة للأجهزة الضعيفة وتقليل الحركات"),
    RESTORE_DEFAULTS("restore_defaults", "استعادة الإعدادات الافتراضية", "Restore Default Settings", "إعادة ضبط القائمة أو جميع إعدادات المشغل إلى الوضع الأصلي"),
    APP_INFO("app_info", "معلومات التطبيق", "Application Information", "تفاصيل الإصدار، التوافق مع Android TV وإرشادات التعيين كمشغل افتراضي")
}

data class SystemShortcutOption(
    val id: String,
    val titleAr: String,
    val iconName: String,
    val itemType: NavItemType,
    val targetAction: String
)

data class LocalFileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long,
    val fileTypeLabelAr: String
)
