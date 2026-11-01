package com.example.ui.screens

import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.components.AppTileCard
import com.example.ui.theme.LuminousCyanGlow
import com.example.ui.theme.LuminousPurpleGlow
import com.example.ui.theme.MidnightNavyElevated
import com.example.ui.theme.SoftLavenderText
import com.example.ui.theme.toComposeFontFamily
import com.example.viewmodel.LauncherUiState
import com.example.viewmodel.LauncherViewModel
import kotlin.math.roundToInt

@Composable
fun IconCustomizationPane(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel
) {
    val cfg = uiState.iconConfig

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("icon_customization_screen")
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تخصيص الأيقونات — Icon Customization",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "عدّل شكل الأيقونات، الحجم، المسافات، الشفافية، وتوهج الإطار مع معاينة فورية",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftLavenderText
                    )
                }
                TvActionButton(
                    text = "استعادة افتراضي الأيقونات",
                    onClick = { viewModel.resetIconConfig() }
                )
            }
        }

        // Live Preview Box
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x88111833))
                    .border(1.dp, Color(0x4438BDF8), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "معاينة فورية للأيقونات:",
                    style = MaterialTheme.typography.labelLarge,
                    color = LuminousCyanGlow
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(cfg.iconSpacingDp.dp)
                ) {
                    items(uiState.installedApps.take(4), key = { it.packageName }) { sampleApp ->
                        AppTileCard(
                            app = sampleApp,
                            iconConfig = cfg,
                            typographyConfig = uiState.typographyConfig,
                            performanceConfig = uiState.performanceConfig,
                            onClick = {}
                        )
                    }
                }
            }
        }

        // Icon Shapes
        item {
            Text(
                text = "شكل الأيقونة (Icon Shapes):",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(IconShapeOption.entries) { shapeOpt ->
                    TvFilterChip(
                        label = "${shapeOpt.labelAr} (${shapeOpt.labelEn})",
                        selected = cfg.shape == shapeOpt,
                        onClick = {
                            viewModel.updateIconConfig { it.copy(shape = shapeOpt) }
                        }
                    )
                }
            }
        }

        // Icon Size Presets
        item {
            Text(
                text = "أحجام الأيقونات الجاهزة (Icon Sizes):",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(IconSizePreset.entries) { preset ->
                    TvFilterChip(
                        label = "${preset.labelAr} (${preset.labelEn})",
                        selected = cfg.sizePreset == preset && (cfg.customSizeDp - preset.defaultDp).let { it in -2f..2f },
                        onClick = {
                            viewModel.updateIconConfig {
                                it.copy(sizePreset = preset, customSizeDp = preset.defaultDp)
                            }
                        }
                    )
                }
            }
        }

        // Precise Size Adjustment Slider
        item {
            SettingSliderControl(
                titleAr = "ضبط دقيق لحجم الأيقونة: ${cfg.customSizeDp.roundToInt()} dp",
                value = cfg.customSizeDp,
                valueRange = 68f..164f,
                stepDelta = 6f,
                onValueChange = { newVal ->
                    viewModel.updateIconConfig { it.copy(customSizeDp = newVal) }
                }
            )
        }

        // Show / Hide Labels & Label Font Size
        item {
            SettingToggleRow(
                titleAr = "إظهار أسماء التطبيقات أسفل الأيقونة",
                subtitleAr = "إظهار أو إخفاء تسمية التطبيق داخل البطاقة",
                checked = cfg.showLabels,
                onCheckedChange = { checked ->
                    viewModel.updateIconConfig { it.copy(showLabels = checked) }
                }
            )
        }

        item {
            SettingSliderControl(
                titleAr = "حجم خط اسم التطبيق: ${cfg.labelFontSizeSp.roundToInt()} sp",
                value = cfg.labelFontSizeSp,
                valueRange = 10f..20f,
                stepDelta = 1f,
                onValueChange = { newVal ->
                    viewModel.updateIconConfig { it.copy(labelFontSizeSp = newVal) }
                }
            )
        }

        // Space Between Icons, Corner Radius, Background Opacity
        item {
            SettingSliderControl(
                titleAr = "المسافة بين الأيقونات: ${cfg.iconSpacingDp.roundToInt()} dp",
                value = cfg.iconSpacingDp,
                valueRange = 6f..32f,
                stepDelta = 2f,
                onValueChange = { newVal ->
                    viewModel.updateIconConfig { it.copy(iconSpacingDp = newVal) }
                }
            )
        }

        item {
            SettingSliderControl(
                titleAr = "استدارة حواف البطاقة (Corner Radius): ${cfg.tileCornerRadiusDp.roundToInt()} dp",
                value = cfg.tileCornerRadiusDp,
                valueRange = 0f..40f,
                stepDelta = 2f,
                onValueChange = { newVal ->
                    viewModel.updateIconConfig { it.copy(tileCornerRadiusDp = newVal) }
                }
            )
        }

        item {
            SettingSliderControl(
                titleAr = "شفافية خلفية البطاقة: ${(cfg.tileBackgroundOpacity * 100).roundToInt()}%",
                value = cfg.tileBackgroundOpacity,
                valueRange = 0.2f..1.0f,
                stepDelta = 0.05f,
                onValueChange = { newVal ->
                    viewModel.updateIconConfig { it.copy(tileBackgroundOpacity = newVal) }
                }
            )
        }

        // Selected Item Glow & Focus Border Thickness
        item {
            SettingToggleRow(
                titleAr = "تفعيل التوهج المضيء حول العنصر المحدد (Selected Item Glow)",
                subtitleAr = "إضاءة نيون زرقاء وبنفسجية ناعمة خلف البطاقة عند التركيز بالريموت",
                checked = cfg.enableSelectedGlow,
                onCheckedChange = { checked ->
                    viewModel.updateIconConfig { it.copy(enableSelectedGlow = checked) }
                }
            )
        }

        item {
            SettingSliderControl(
                titleAr = "سُمك إطار التركيز (Focus Border Thickness): ${"%.1f".format(cfg.focusBorderThicknessDp)} dp",
                value = cfg.focusBorderThicknessDp,
                valueRange = 1f..5.5f,
                stepDelta = 0.5f,
                onValueChange = { newVal ->
                    viewModel.updateIconConfig { it.copy(focusBorderThicknessDp = newVal) }
                }
            )
        }
    }
}

@Composable
fun FontsAndTypographyPane(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel
) {
    val typo = uiState.typographyConfig

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("fonts_typography_screen")
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الخطوط والطباعة العربية — Fonts and Typography",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "جميع الخطوط العربية الـ 8 مضمنة محلياً داخل التطبيق وتعمل فوراً دون إنترنت أو إعادة تشغيل",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftLavenderText
                    )
                }
                TvActionButton(
                    text = "استعادة خط Cairo الافتراضي",
                    onClick = { viewModel.resetTypographyConfig() }
                )
            }
        }

        item {
            Text(
                text = "اختر الخط العربي للواجهة (Cairo هو الافتراضي):",
                style = MaterialTheme.typography.titleLarge,
                color = LuminousCyanGlow
            )
        }

        items(ArabicFontOption.entries, key = { it.id }) { fontOpt ->
            val isSelected = typo.fontOption == fontOpt
            var isFocused by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        when {
                            isSelected -> Color(0x556D28D9)
                            isFocused -> Color(0x442563EB)
                            else -> Color(0x77111833)
                        }
                    )
                    .border(
                        width = if (isFocused || isSelected) 1.8.dp else 1.dp,
                        color = if (isFocused) LuminousCyanGlow else if (isSelected) LuminousPurpleGlow else Color(0x3364748B),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .onFocusChanged { isFocused = it.isFocused }
                    .focusable()
                    .clickable {
                        viewModel.updateTypographyConfig { it.copy(fontOption = fontOpt) }
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("font_option_${fontOpt.id}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = fontOpt.displayNameAr,
                        fontFamily = fontOpt.toComposeFontFamily(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                    Text(
                        text = fontOpt.sampleTextAr,
                        fontFamily = fontOpt.toComposeFontFamily(),
                        fontSize = 14.sp,
                        color = SoftLavenderText
                    )
                }
                Text(
                    text = if (isSelected) "مفعل حالياً ✓" else "تطبيق الخط",
                    color = if (isSelected) LuminousCyanGlow else SoftLavenderText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Font Weight Options
        item {
            Text(
                text = "سُمك الخط (Font Weights):",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(FontWeightOption.entries) { weightOpt ->
                    TvFilterChip(
                        label = weightOpt.labelAr,
                        selected = typo.fontWeightOption == weightOpt,
                        onClick = {
                            viewModel.updateTypographyConfig { it.copy(fontWeightOption = weightOpt) }
                        }
                    )
                }
            }
        }

        // Global Font Scale, App Label, Clock/Date, and Navigation Menu Typography Controls
        item {
            SettingSliderControl(
                titleAr = "مقياس حجم الخط العام للواجهة: ${(typo.globalFontScale * 100).roundToInt()}%",
                value = typo.globalFontScale,
                valueRange = 0.85f..1.30f,
                stepDelta = 0.05f,
                onValueChange = { scale ->
                    viewModel.updateTypographyConfig { it.copy(globalFontScale = scale) }
                }
            )
        }

        item {
            SettingSliderControl(
                titleAr = "حجم خط القائمة الجانبية: ${typo.navMenuFontSizeSp.roundToInt()} sp",
                value = typo.navMenuFontSizeSp,
                valueRange = 12f..21f,
                stepDelta = 1f,
                onValueChange = { newSize ->
                    viewModel.updateTypographyConfig { it.copy(navMenuFontSizeSp = newSize) }
                }
            )
        }

        item {
            SettingSliderControl(
                titleAr = "حجم خط أسماء التطبيقات: ${uiState.iconConfig.labelFontSizeSp.roundToInt()} sp",
                value = uiState.iconConfig.labelFontSizeSp,
                valueRange = 10f..20f,
                stepDelta = 1f,
                onValueChange = { newSize ->
                    viewModel.updateIconConfig { it.copy(labelFontSizeSp = newSize) }
                    viewModel.updateTypographyConfig { it.copy(appLabelSizeSp = newSize) }
                }
            )
        }

        item {
            SettingSliderControl(
                titleAr = "حجم خط الساعة: ${uiState.clockDateConfig.clockSizeSp.roundToInt()} sp",
                value = uiState.clockDateConfig.clockSizeSp,
                valueRange = 18f..38f,
                stepDelta = 2f,
                onValueChange = { newSize ->
                    viewModel.updateClockDateConfig { it.copy(clockSizeSp = newSize) }
                    viewModel.updateTypographyConfig { it.copy(clockFontSizeSp = newSize) }
                }
            )
        }
    }
}

@Composable
fun BackgroundAndAppearancePane(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    onPickBackgroundFromFiles: () -> Unit
) {
    val bgCfg = uiState.backgroundConfig

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("background_appearance_screen")
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الخلفية والمظهر — Background and Appearance",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "اختر من الخلفيات الثابتة المدمجة أو استورد صورة مخصصة من ملفات الجهاز (JPEG / PNG / WebP)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftLavenderText
                    )
                }

                TvActionButton(
                    text = "استعادة الخلفية الافتراضية (Reset to Default Background)",
                    testTagName = "reset_default_background_button",
                    onClick = { viewModel.resetBackgroundToDefault() }
                )
            }
        }

        // Prominent Button: Choose Background from Files
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x99172146))
                    .border(1.5.dp, LuminousCyanGlow, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "استيراد صورة خلفية مخصصة من الجهاز",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "يدعم صور JPEG و PNG و WebP مع تحجيم ذكي وتوفير للذاكرة العشوائية",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftLavenderText
                    )
                }

                TvActionButton(
                    text = "Choose Background from Files — اختيار خلفية من الملفات",
                    isPrimary = true,
                    testTagName = "choose_background_from_files_button",
                    onClick = onPickBackgroundFromFiles
                )
            }
        }

        // Preview before applying custom image
        if (uiState.previewWallpaperBitmap != null) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xCC111833))
                        .border(2.dp, LuminousPurpleGlow, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "معاينة الصورة المختارة قبل التطبيق:",
                        style = MaterialTheme.typography.titleLarge,
                        color = LuminousCyanGlow
                    )
                    val previewScale = when (bgCfg.scaleMode) {
                        BackgroundScaleMode.FILL_SCREEN -> ContentScale.FillBounds
                        BackgroundScaleMode.FIT_IMAGE -> ContentScale.Fit
                        BackgroundScaleMode.CENTER_CROP -> ContentScale.Crop
                    }
                    Image(
                        bitmap = uiState.previewWallpaperBitmap,
                        contentDescription = "معاينة الخلفية المختارة",
                        contentScale = previewScale,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TvActionButton(
                            text = "تطبيق الخلفية المختارة ✓",
                            isPrimary = true,
                            onClick = { viewModel.applyPreviewedCustomBackground() }
                        )
                        TvActionButton(
                            text = "إلغاء المعاينة",
                            onClick = { viewModel.cancelCustomBackgroundPreview() }
                        )
                    }
                }
            }
        }

        // Scaling Options: Fill Screen, Fit Image, Center Crop
        item {
            Text(
                text = "خيارات عرض الخلفية (Display Options):",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(BackgroundScaleMode.entries) { mode ->
                    TvFilterChip(
                        label = "${mode.labelAr} (${mode.labelEn})",
                        selected = bgCfg.scaleMode == mode,
                        onClick = { viewModel.updateBackgroundScaleMode(mode) }
                    )
                }
            }
        }

        // Built-in Static Backgrounds
        item {
            Text(
                text = "الخلفيات الثابتة المدمجة (Built-in Static Backgrounds):",
                style = MaterialTheme.typography.titleLarge,
                color = LuminousCyanGlow
            )
        }

        items(BuiltInBackgroundPreset.entries, key = { it.id }) { preset ->
            val isSelected = bgCfg.customImageUri.isBlank() && bgCfg.preset == preset
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) Color(0x554F46E5) else Color(0x77111833))
                    .border(
                        width = if (isSelected) 1.8.dp else 1.dp,
                        color = if (isSelected) LuminousCyanGlow else Color(0x3364748B),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = preset.titleAr,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = preset.subtitleAr,
                        style = MaterialTheme.typography.labelMedium,
                        color = SoftLavenderText
                    )
                }
                TvActionButton(
                    text = if (isSelected) "مفعلة حالياً ✓" else "تطبيق الخلفية",
                    isPrimary = isSelected,
                    onClick = { viewModel.selectBuiltInBackground(preset) }
                )
            }
        }
    }
}

@Composable
fun ClockAndDatePane(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel
) {
    val clk = uiState.clockDateConfig

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("clock_date_settings_screen")
    ) {
        item {
            Text(
                text = "إعدادات الساعة والتاريخ — Clock and Date",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                text = "تحكم في تنسيق الوقت (12 أو 24 ساعة)، إظهار أو إخفاء الوقت والتاريخ العربي، وحجم الخط",
                style = MaterialTheme.typography.bodyMedium,
                color = SoftLavenderText
            )
        }

        item {
            SettingToggleRow(
                titleAr = "استخدام تنسيق 24 ساعة (مثال: 20:45)",
                subtitleAr = "عند التعطيل سيتم عرض الوقت بنظام 12 ساعة (صباحاً/مساءً)",
                checked = clk.is24HourFormat,
                onCheckedChange = { checked ->
                    viewModel.updateClockDateConfig { it.copy(is24HourFormat = checked) }
                }
            )
        }

        item {
            SettingToggleRow(
                titleAr = "إظهار الساعة في الشريط العلوي",
                subtitleAr = "إظهار أو إخفاء الوقت الحالي في أعلى الشاشة",
                checked = clk.showClock,
                onCheckedChange = { checked ->
                    viewModel.updateClockDateConfig { it.copy(showClock = checked) }
                }
            )
        }

        item {
            SettingToggleRow(
                titleAr = "إظهار التاريخ بالتنسيق العربي",
                subtitleAr = "إظهار أو إخفاء اليوم والتاريخ بالعربية أسفل الساعة",
                checked = clk.showDate,
                onCheckedChange = { checked ->
                    viewModel.updateClockDateConfig { it.copy(showDate = checked) }
                }
            )
        }

        item {
            SettingSliderControl(
                titleAr = "حجم خط الساعة: ${clk.clockSizeSp.roundToInt()} sp",
                value = clk.clockSizeSp,
                valueRange = 16f..40f,
                stepDelta = 2f,
                onValueChange = { newVal ->
                    viewModel.updateClockDateConfig { it.copy(clockSizeSp = newVal) }
                }
            )
        }

        item {
            SettingSliderControl(
                titleAr = "حجم خط التاريخ العربي: ${clk.dateSizeSp.roundToInt()} sp",
                value = clk.dateSizeSp,
                valueRange = 10f..22f,
                stepDelta = 1f,
                onValueChange = { newVal ->
                    viewModel.updateClockDateConfig { it.copy(dateSizeSp = newVal) }
                }
            )
        }
    }
}

@Composable
fun HomeScreenLayoutPane(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel
) {
    val layoutCfg = uiState.homeLayoutConfig
    var bannerToEdit by remember { mutableStateOf<FeaturedBannerItem?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_layout_settings_screen")
    ) {
        item {
            Text(
                text = "تخطيط الشاشة الرئيسية والبانر المميز — Home Screen Layout",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }

        item {
            SettingToggleRow(
                titleAr = "التبديل التلقائي لشرائح البانر المميز",
                subtitleAr = "معطل افتراضياً للحفاظ على ثبات الشاشة وأعلى كفاءة للأداء",
                checked = layoutCfg.autoRotateBanners,
                onCheckedChange = { checked ->
                    viewModel.updateHomeLayoutConfig { it.copy(autoRotateBanners = checked) }
                }
            )
        }

        item {
            SettingToggleRow(
                titleAr = "إظهار العنوان وزر التشغيل داخل البانر المميز",
                subtitleAr = "إظهار نص (اكتشف المحتوى المميز) وزر التشغيل الدائري",
                checked = layoutCfg.showBannerTextAndAction,
                onCheckedChange = { checked ->
                    viewModel.updateHomeLayoutConfig { it.copy(showBannerTextAndAction = checked) }
                }
            )
        }

        item {
            SettingToggleRow(
                titleAr = "قائمة جانبية يمنى مدمجة (Compact Right Menu)",
                subtitleAr = "توفير أكبر مساحة ممكنة لمنطقة المحتوى الرئيسية والبانر",
                checked = layoutCfg.compactNavMenu,
                onCheckedChange = { checked ->
                    viewModel.updateHomeLayoutConfig { it.copy(compactNavMenu = checked) }
                }
            )
        }

        item {
            SettingSliderControl(
                titleAr = "الحد الأقصى لتطبيقات الصف الرئيسي: ${layoutCfg.maxHomeRowApps} تطبيقات",
                value = layoutCfg.maxHomeRowApps.toFloat(),
                valueRange = 3f..10f,
                stepDelta = 1f,
                onValueChange = { newVal ->
                    viewModel.updateHomeLayoutConfig { it.copy(maxHomeRowApps = newVal.roundToInt()) }
                }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تخصيص شرائح البانر المميز (${uiState.featuredBanners.size}):",
                    style = MaterialTheme.typography.titleLarge,
                    color = LuminousCyanGlow
                )
                TvActionButton(
                    text = "استعادة البانرات الافتراضية",
                    onClick = { viewModel.resetFeaturedBanners() }
                )
            }
        }

        items(uiState.featuredBanners, key = { it.id }) { banner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x77141D3D))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = banner.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(text = banner.subtitle, style = MaterialTheme.typography.labelMedium, color = SoftLavenderText)
                }
                TvActionButton(
                    text = "تعديل البانر",
                    onClick = { bannerToEdit = banner }
                )
            }
        }
    }

    bannerToEdit?.let { target ->
        var editTitle by remember(target) { mutableStateOf(target.title) }
        var editSubtitle by remember(target) { mutableStateOf(target.subtitle) }
        var editTargetType by remember(target) { mutableStateOf(target.targetType) }
        var editTargetVal by remember(target) { mutableStateOf(target.targetValue) }

        Dialog(onDismissRequest = { bannerToEdit = null }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MidnightNavyElevated,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .border(1.5.dp, LuminousCyanGlow, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "تعديل محتوى وإجراء البانر",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("عنوان البانر") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSubtitle,
                        onValueChange = { editSubtitle = it },
                        label = { Text("الوصف الفرعي") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "عند الضغط على زر التشغيل في البانر:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftLavenderText
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(BuiltInSection.entries) { sec ->
                            TvFilterChip(
                                label = "فتح قسم: ${sec.defaultTitleAr}",
                                selected = editTargetType == BannerTargetType.OPEN_SECTION && editTargetVal == sec.id,
                                onClick = {
                                    editTargetType = BannerTargetType.OPEN_SECTION
                                    editTargetVal = sec.id
                                }
                            )
                        }
                        items(uiState.installedApps.take(6)) { app ->
                            TvFilterChip(
                                label = "تشغيل: ${app.label}",
                                selected = editTargetType == BannerTargetType.LAUNCH_APP && editTargetVal == app.packageName,
                                onClick = {
                                    editTargetType = BannerTargetType.LAUNCH_APP
                                    editTargetVal = app.packageName
                                }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                    ) {
                        TvActionButton(text = "إلغاء", onClick = { bannerToEdit = null })
                        TvActionButton(
                            text = "حفظ التعديلات",
                            isPrimary = true,
                            onClick = {
                                viewModel.updateFeaturedBanner(
                                    target.copy(
                                        title = editTitle.trim().ifEmpty { target.title },
                                        subtitle = editSubtitle.trim().ifEmpty { target.subtitle },
                                        targetType = editTargetType,
                                        targetValue = editTargetVal
                                    )
                                )
                                bannerToEdit = null
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeneralSettingsPane(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("general_settings_screen")
    ) {
        item {
            Text(
                text = "الإعدادات العامة — General Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                text = "إعدادات النظام الأساسية وتحديث قائمة التطبيقات وتعيين المشغل الافتراضي",
                style = MaterialTheme.typography.bodyMedium,
                color = SoftLavenderText
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TvActionButton(
                    text = "تحديث قائمة التطبيقات المثبتة الآن",
                    isPrimary = true,
                    onClick = {
                        viewModel.refreshInstalledApps()
                        viewModel.showStatusMessage("تم فحص وتحديث التطبيقات المثبتة على الجهاز")
                    }
                )
                TvActionButton(
                    text = "تعيين كمشغل افتراضي للتلفاز",
                    onClick = { viewModel.openHomeLauncherSettings() }
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TvActionButton(
                    text = "فتح إعدادات شبكة الواي فاي في أندرويد",
                    onClick = { viewModel.openAndroidSystemSettings(Settings.ACTION_WIFI_SETTINGS) }
                )
                TvActionButton(
                    text = "فتح إعدادات الشاشة والعرض",
                    onClick = { viewModel.openAndroidSystemSettings(Settings.ACTION_DISPLAY_SETTINGS) }
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x66141D3D))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "حالة الاتصال الحالية بالإنترنت:",
                    style = MaterialTheme.typography.titleMedium,
                    color = LuminousCyanGlow
                )
                Text(
                    text = uiState.networkStatus.labelAr,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Text(
                    text = "عدد التطبيقات المكتشفة على الجهاز: ${uiState.installedApps.size} تطبيق",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftLavenderText
                )
            }
        }
    }
}

@Composable
fun RemoteNavigationPane(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel
) {
    val navCfg = uiState.remoteNavConfig

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("remote_navigation_settings_screen")
    ) {
        item {
            Text(
                text = "التحكم عن بعد — Remote Navigation",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                text = "ضبط سلوك أزرار الاتجاهات (D-Pad) وزر الرجوع والتأكيد في ريموت التلفاز",
                style = MaterialTheme.typography.bodyMedium,
                color = SoftLavenderText
            )
        }

        item {
            SettingToggleRow(
                titleAr = "حفظ واستعادة موقع التركيز عند العودة من الإعدادات",
                subtitleAr = "يعيد المؤشر تلقائياً إلى العنصر النشط دون قفزات غير متوقعة",
                checked = navCfg.rememberLastSettingsFocus,
                onCheckedChange = { checked ->
                    viewModel.updateRemoteNavConfig { it.copy(rememberLastSettingsFocus = checked) }
                }
            )
        }

        item {
            SettingToggleRow(
                titleAr = "إظهار نافذة تأكيد خفيفة قبل إزالة عنصر من القائمة الرئيسية",
                subtitleAr = "يمنع إزالة عناصر القائمة عن طريق الخطأ أثناء استخدام الريموت",
                checked = navCfg.confirmBeforeRemovingMenuItem,
                onCheckedChange = { checked ->
                    viewModel.updateRemoteNavConfig { it.copy(confirmBeforeRemovingMenuItem = checked) }
                }
            )
        }
    }
}

@Composable
fun PerformanceOptionsPane(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel
) {
    val perf = uiState.performanceConfig

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("performance_options_screen")
    ) {
        item {
            Text(
                text = "خيارات الأداء — Performance Options",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                text = "تحسين استهلاك الذاكرة والمعالج لأجهزة التلفاز وصناديق Android TV ذات المواصفات المحدودة",
                style = MaterialTheme.typography.bodyMedium,
                color = SoftLavenderText
            )
        }

        item {
            SettingToggleRow(
                titleAr = "وضع التأثيرات المنخفضة للأجهزة الضعيفة (Low-Effects Mode)",
                subtitleAr = "يعطل التوهجات الشعاعية والمنحنيات الزخرفية والحركات لتوفير أقصى سرعة استجابة",
                checked = perf.lowEffectsMode,
                onCheckedChange = { checked ->
                    viewModel.updatePerformanceConfig {
                        it.copy(
                            lowEffectsMode = checked,
                            disableDecorativeAccents = checked,
                            minimizeAnimations = checked
                        )
                    }
                }
            )
        }

        item {
            SettingToggleRow(
                titleAr = "تعطيل الزخارف والمنحنيات المضيئة في الخلفية",
                subtitleAr = "الاكتفاء بالتدرج الكحلي الصافي بدون طبقات رسومية إضافية",
                checked = perf.disableDecorativeAccents,
                onCheckedChange = { checked ->
                    viewModel.updatePerformanceConfig { it.copy(disableDecorativeAccents = checked) }
                }
            )
        }

        item {
            SettingToggleRow(
                titleAr = "تقليل الحركات والانتقالات (Minimize Animations)",
                subtitleAr = "انتقال فوري للتركيز بين الأيقونات والقوائم بدون تأثيرات تكبير",
                checked = perf.minimizeAnimations,
                onCheckedChange = { checked ->
                    viewModel.updatePerformanceConfig { it.copy(minimizeAnimations = checked) }
                }
            )
        }
    }
}

@Composable
fun RestoreDefaultSettingsPane(
    viewModel: LauncherViewModel
) {
    var showConfirmResetAll by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("restore_defaults_screen")
    ) {
        Text(
            text = "استعادة الإعدادات الافتراضية — Restore Default Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        Text(
            text = "يمكنك استعادة القائمة الجانبية فقط أو إعادة ضبط جميع تخصيصات المشغل (الأيقونات، الخطوط، الخلفيات، والترتيب) إلى الوضع الأصلي.",
            style = MaterialTheme.typography.bodyLarge,
            color = SoftLavenderText
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TvActionButton(
                text = "استعادة القائمة الافتراضية فقط (Restore Default Menu)",
                onClick = { viewModel.restoreDefaultMenu() }
            )
            TvActionButton(
                text = "استعادة جميع الإعدادات الافتراضية للمشغل",
                isPrimary = true,
                testTagName = "reset_all_settings_button",
                onClick = { showConfirmResetAll = true }
            )
        }
    }

    if (showConfirmResetAll) {
        Dialog(onDismissRequest = { showConfirmResetAll = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MidnightNavyElevated,
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "تأكيد استعادة كافة الإعدادات الافتراضية",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "سيتم إعادة القائمة الرئيسية، شكل وحجم الأيقونات، خط Cairo، والخلفية المرجعية إلى حالتها الأصلية.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftLavenderText
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                    ) {
                        TvActionButton(text = "إلغاء", onClick = { showConfirmResetAll = false })
                        TvActionButton(
                            text = "تأكيد الاستعادة الشاملة",
                            isPrimary = true,
                            onClick = {
                                viewModel.restoreAllDefaultSettings()
                                showConfirmResetAll = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationInformationPane(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("application_info_screen")
    ) {
        item {
            Text(
                text = "معلومات التطبيق — Smooth TV Launcher",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x77141D3D))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "الاسم: Smooth TV Launcher (مشغل التلفاز السلس)", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text(text = "الإصدار: 1.0 (أصلي، خفيف الحجم، داعم كامل للغة العربية RTL)", color = SoftLavenderText)
                Text(text = "الخط الافتراضي: Cairo + 7 خطوط عربية مضمنة محلياً بدون إنترنت", color = SoftLavenderText)
                Text(text = "عدد التطبيقات المثبتة المكتشفة: ${uiState.installedApps.size}", color = LuminousCyanGlow)
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x661E293B))
                    .border(1.dp, Color(0x4438BDF8), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ملاحظة هامة حول التعيين كمشغل افتراضي على أجهزة التلفاز:",
                    style = MaterialTheme.typography.titleMedium,
                    color = LuminousCyanGlow
                )
                Text(
                    text = "تم تكوين Smooth TV Launcher بجميع فلاتر Android TV القياسية (HOME, DEFAULT, LEANBACK_LAUNCHER). تسمح معظم أجهزة Android TV وصناديق البث باختياره مباشرة كمشغل افتراضي، بينما تقيد بعض الشركات المصنعة استبدال المشغل المدمج تلقائياً إلا عبر إعدادات التطبيقات الافتراضية في النظام.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftLavenderText
                )
                Spacer(modifier = Modifier.height(4.dp))
                TvActionButton(
                    text = "فتح إعدادات المشغل الافتراضي للنظام",
                    onClick = { viewModel.openHomeLauncherSettings() }
                )
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    titleAr: String,
    subtitleAr: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) Color(0x553B82F6) else Color(0x77141D3D))
            .border(
                width = if (isFocused) 1.8.dp else 1.dp,
                color = if (isFocused) LuminousCyanGlow else Color(0x3364748B),
                shape = RoundedCornerShape(14.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titleAr,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = subtitleAr,
                style = MaterialTheme.typography.labelMedium,
                color = SoftLavenderText
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingSliderControl(
    titleAr: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    stepDelta: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x77141D3D))
            .border(1.dp, Color(0x3364748B), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = titleAr,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            // Remote-friendly - / + buttons alongside interactive Slider
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TvActionButton(
                    text = "− تصغير",
                    onClick = {
                        onValueChange((value - stepDelta).coerceIn(valueRange.start, valueRange.endInclusive))
                    }
                )
                TvActionButton(
                    text = "+ تكبير",
                    onClick = {
                        onValueChange((value + stepDelta).coerceIn(valueRange.start, valueRange.endInclusive))
                    }
                )
            }
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
