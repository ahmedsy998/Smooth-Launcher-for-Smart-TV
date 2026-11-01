package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ArabicFontOption
import com.example.model.FontWeightOption
import com.example.model.TypographyConfig

val CairoFontFamily = FontFamily(
    Font(R.font.cairo, FontWeight.Normal),
    Font(R.font.cairo, FontWeight.Medium),
    Font(R.font.cairo, FontWeight.SemiBold),
    Font(R.font.cairo, FontWeight.Bold)
)

val AmiriFontFamily = FontFamily(
    Font(R.font.amiri, FontWeight.Normal),
    Font(R.font.amiri, FontWeight.Medium),
    Font(R.font.amiri, FontWeight.SemiBold),
    Font(R.font.amiri, FontWeight.Bold)
)

val TajawalFontFamily = FontFamily(
    Font(R.font.tajawal, FontWeight.Normal),
    Font(R.font.tajawal, FontWeight.Medium),
    Font(R.font.tajawal, FontWeight.SemiBold),
    Font(R.font.tajawal, FontWeight.Bold)
)

val IbmPlexSansArabicFontFamily = FontFamily(
    Font(R.font.ibm_plex_sans_arabic, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_arabic, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_arabic, FontWeight.SemiBold),
    Font(R.font.ibm_plex_sans_arabic, FontWeight.Bold)
)

val NotoSansArabicFontFamily = FontFamily(
    Font(R.font.noto_sans_arabic, FontWeight.Normal),
    Font(R.font.noto_sans_arabic, FontWeight.Medium),
    Font(R.font.noto_sans_arabic, FontWeight.SemiBold),
    Font(R.font.noto_sans_arabic, FontWeight.Bold)
)

val NotoNaskhArabicFontFamily = FontFamily(
    Font(R.font.noto_naskh_arabic, FontWeight.Normal),
    Font(R.font.noto_naskh_arabic, FontWeight.Medium),
    Font(R.font.noto_naskh_arabic, FontWeight.SemiBold),
    Font(R.font.noto_naskh_arabic, FontWeight.Bold)
)

val ReadexProFontFamily = FontFamily(
    Font(R.font.readex_pro, FontWeight.Normal),
    Font(R.font.readex_pro, FontWeight.Medium),
    Font(R.font.readex_pro, FontWeight.SemiBold),
    Font(R.font.readex_pro, FontWeight.Bold)
)

val AlmaraiFontFamily = FontFamily(
    Font(R.font.almarai, FontWeight.Normal),
    Font(R.font.almarai, FontWeight.Medium),
    Font(R.font.almarai, FontWeight.SemiBold),
    Font(R.font.almarai, FontWeight.Bold)
)

fun ArabicFontOption.toComposeFontFamily(): FontFamily = when (this) {
    ArabicFontOption.CAIRO -> CairoFontFamily
    ArabicFontOption.AMIRI -> AmiriFontFamily
    ArabicFontOption.TAJAWAL -> TajawalFontFamily
    ArabicFontOption.IBM_PLEX_SANS_ARABIC -> IbmPlexSansArabicFontFamily
    ArabicFontOption.NOTO_SANS_ARABIC -> NotoSansArabicFontFamily
    ArabicFontOption.NOTO_NASKH_ARABIC -> NotoNaskhArabicFontFamily
    ArabicFontOption.READEX_PRO -> ReadexProFontFamily
    ArabicFontOption.ALMARAI -> AlmaraiFontFamily
}

fun FontWeightOption.toComposeFontWeight(): FontWeight = when (this) {
    FontWeightOption.NORMAL -> FontWeight.Normal
    FontWeightOption.MEDIUM -> FontWeight.Medium
    FontWeightOption.SEMI_BOLD -> FontWeight.SemiBold
    FontWeightOption.BOLD -> FontWeight.Bold
}

fun buildLauncherTypography(config: TypographyConfig): Typography {
    val family = config.fontOption.toComposeFontFamily()
    val baseWeight = config.fontWeightOption.toComposeFontWeight()
    val boldWeight = if (baseWeight == FontWeight.Bold) FontWeight.Bold else FontWeight.Bold
    val scale = config.globalFontScale

    return Typography(
        displayLarge = TextStyle(
            fontFamily = family,
            fontWeight = boldWeight,
            fontSize = (34f * scale).sp,
            lineHeight = (42f * scale).sp
        ),
        displayMedium = TextStyle(
            fontFamily = family,
            fontWeight = boldWeight,
            fontSize = (28f * scale).sp,
            lineHeight = (36f * scale).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = family,
            fontWeight = boldWeight,
            fontSize = (24f * scale).sp,
            lineHeight = (32f * scale).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = boldWeight,
            fontSize = (20f * scale).sp,
            lineHeight = (28f * scale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = boldWeight,
            fontSize = (18f * scale).sp,
            lineHeight = (26f * scale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = baseWeight,
            fontSize = (15f * scale).sp,
            lineHeight = (22f * scale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = baseWeight,
            fontSize = (14f * scale).sp,
            lineHeight = (21f * scale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = baseWeight,
            fontSize = (13f * scale).sp,
            lineHeight = (19f * scale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = baseWeight,
            fontSize = (13f * scale).sp,
            lineHeight = (18f * scale).sp
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = baseWeight,
            fontSize = (11f * scale).sp,
            lineHeight = (16f * scale).sp
        )
    )
}

val Typography = buildLauncherTypography(TypographyConfig())
