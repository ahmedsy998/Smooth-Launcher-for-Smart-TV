package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.FeaturedBannerItem
import com.example.model.HomeLayoutConfig
import com.example.model.IconCustomizationConfig
import com.example.model.PerformanceConfig
import com.example.ui.theme.LuminousCyanGlow
import com.example.ui.theme.LuminousPurpleGlow
import com.example.ui.theme.SoftLavenderText
import kotlinx.coroutines.delay

@Composable
fun FeaturedBannerCarousel(
    banners: List<FeaturedBannerItem>,
    activeIndex: Int,
    homeLayoutConfig: HomeLayoutConfig,
    iconConfig: IconCustomizationConfig,
    performanceConfig: PerformanceConfig,
    bannerFocusRequester: FocusRequester,
    onSelectBannerIndex: (Int) -> Unit,
    onNextBanner: () -> Unit,
    onPreviousBanner: () -> Unit,
    onExecuteBannerAction: (FeaturedBannerItem) -> Unit,
    onMoveFocusToRightNav: () -> Unit,
    onMoveFocusDownToApps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentBanner = banners.getOrElse(activeIndex) {
        banners.firstOrNull() ?: FeaturedBannerItem(
            id = "default",
            title = "اكتشف المحتوى المميز",
            subtitle = "أفلام • مسلسلات • تطبيقات"
        )
    }

    var isFocused by remember { mutableStateOf(false) }

    // Optional automatic banner rotation (disabled by default for static performance)
    LaunchedEffect(homeLayoutConfig.autoRotateBanners, homeLayoutConfig.bannerRotationIntervalSec, banners.size) {
        if (homeLayoutConfig.autoRotateBanners && banners.size > 1) {
            while (true) {
                delay(homeLayoutConfig.bannerRotationIntervalSec * 1000L)
                onNextBanner()
            }
        }
    }

    val bannerShape = RoundedCornerShape(26.dp)
    val scale by animateFloatAsState(
        targetValue = if (isFocused && !performanceConfig.minimizeAnimations && !performanceConfig.lowEffectsMode) 1.012f else 1f,
        animationSpec = tween(durationMillis = 160),
        label = "bannerScale"
    )

    val borderThickness = if (isFocused) {
        iconConfig.focusBorderThicknessDp.dp
    } else {
        1.6.dp
    }

    val borderBrush = if (isFocused) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFE879F9),
                LuminousPurpleGlow,
                LuminousCyanGlow,
                Color(0xFF60A5FA)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xCC9333EA),
                Color(0x663B82F6),
                Color(0xCC38BDF8)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(bannerShape)
            .border(
                width = borderThickness,
                brush = borderBrush,
                shape = bannerShape
            )
            .focusRequester(bannerFocusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionLeft -> {
                            if (banners.size > 1) {
                                onNextBanner()
                                true
                            } else false
                        }

                        Key.DirectionRight -> {
                            // Allow cycling banners or returning to right-side vertical navigation
                            onMoveFocusToRightNav()
                            true
                        }

                        Key.DirectionDown -> {
                            onMoveFocusDownToApps()
                            true
                        }

                        Key.Enter, Key.DirectionCenter -> {
                            onExecuteBannerAction(currentBanner)
                            true
                        }

                        else -> false
                    }
                } else {
                    false
                }
            }
            .focusable()
            .clickable { onExecuteBannerAction(currentBanner) }
            .testTag("featured_content_banner")
    ) {
        // Static Banner Background Image (Default Slide 0 uses the extracted Reference Castle Sunset artwork)
        when (currentBanner.presetThemeId % 3) {
            0 -> {
                Image(
                    painter = painterResource(id = R.drawable.img_banner_castle),
                    contentDescription = currentBanner.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            1 -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1A103C),
                                Color(0xFF311B6B),
                                Color(0xFF0E2954)
                            )
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x66A855F7), Color.Transparent),
                            center = Offset(size.width * 0.25f, size.height * 0.35f),
                            radius = size.width * 0.45f
                        ),
                        center = Offset(size.width * 0.25f, size.height * 0.35f),
                        radius = size.width * 0.45f
                    )
                }
            }

            else -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0B192C),
                                Color(0xFF1E3E62),
                                Color(0xFF2E1A47)
                            )
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x5538BDF8), Color.Transparent),
                            center = Offset(size.width * 0.72f, size.height * 0.4f),
                            radius = size.width * 0.42f
                        ),
                        center = Offset(size.width * 0.72f, size.height * 0.4f),
                        radius = size.width * 0.42f
                    )
                }
            }
        }

        // Reference top-left violet glow and bottom cinematic vignette for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x187C3AED),
                            Color(0x10080C1C),
                            Color(0xCC070B19)
                        )
                    )
                )
        )

        // Optional Title, Subtitle, and Play Action
        if (homeLayoutConfig.showBannerTextAndAction) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.End)
            ) {
                // Title and subtitle (Arabic RTL aligned next to play button)
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = currentBanner.title,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("banner_title_text")
                    )
                    Text(
                        text = currentBanner.subtitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = SoftLavenderText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("banner_subtitle_text")
                    )
                }

                // Circular Frosted Glass Play Button matching reference image
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (isFocused) {
                                    listOf(Color(0xDD7C3AED), Color(0xDD2563EB))
                                } else {
                                    listOf(Color(0x66334155), Color(0x881E293B))
                                }
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (isFocused) LuminousCyanGlow else Color(0xAA94A3B8),
                            shape = CircleShape
                        )
                        .testTag("banner_play_button")
                ) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.28f, size.height * 0.16f)
                            lineTo(size.width * 0.84f, size.height * 0.50f)
                            lineTo(size.width * 0.28f, size.height * 0.84f)
                            close()
                        }
                        drawPath(path = path, color = Color.White)
                    }
                }
            }
        }

        // Small Carousel Position Indicator at bottom center (matching reference pill + dots)
        if (banners.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .testTag("banner_carousel_indicators"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                banners.forEachIndexed { index, _ ->
                    val isCurrent = index == activeIndex
                    Box(
                        modifier = Modifier
                            .height(7.dp)
                            .width(if (isCurrent) 22.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCurrent) {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFA855F7), Color(0xFF6366F1))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0x7794A3B8), Color(0x7794A3B8))
                                    )
                                }
                            )
                            .clickable { onSelectBannerIndex(index) }
                    )
                }
            }
        }
    }
}
