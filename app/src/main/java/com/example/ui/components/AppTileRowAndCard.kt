package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.LuminousCyanGlow
import com.example.ui.theme.LuminousPurpleGlow

fun Drawable.toComposeImageBitmap(maxPx: Int = 192): ImageBitmap {
    if (this is BitmapDrawable && this.bitmap != null) {
        return this.bitmap.asImageBitmap()
    }
    val width = intrinsicWidth.takeIf { it > 0 }?.coerceAtMost(maxPx) ?: 128
    val height = intrinsicHeight.takeIf { it > 0 }?.coerceAtMost(maxPx) ?: 128
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap.asImageBitmap()
}

@Composable
fun ApplicationBottomRow(
    apps: List<AppItem>,
    iconConfig: IconCustomizationConfig,
    typographyConfig: TypographyConfig,
    performanceConfig: PerformanceConfig,
    firstAppFocusRequester: FocusRequester,
    onLaunchApp: (AppItem) -> Unit,
    onLongPressApp: (AppItem) -> Unit,
    onOpenAllApplications: () -> Unit,
    onMoveFocusUpToBanner: () -> Unit,
    onMoveFocusToRightNav: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_application_row"),
        horizontalArrangement = Arrangement.spacedBy(iconConfig.iconSpacingDp.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
    ) {
        itemsIndexed(
            items = apps,
            key = { _, app -> app.packageName }
        ) { index, app ->
            AppTileCard(
                app = app,
                iconConfig = iconConfig,
                typographyConfig = typographyConfig,
                performanceConfig = performanceConfig,
                modifier = if (index == 0) {
                    Modifier.focusRequester(firstAppFocusRequester)
                } else {
                    Modifier
                },
                onClick = { onLaunchApp(app) },
                onLongClick = { onLongPressApp(app) },
                onMoveUp = onMoveFocusUpToBanner,
                onMoveRightEdge = if (index == 0) onMoveFocusToRightNav else null
            )
        }

        // Dedicated "كل التطبيقات" (All Applications) Tile matching reference image
        item(key = "all_applications_tile") {
            AllApplicationsTileCard(
                iconConfig = iconConfig,
                typographyConfig = typographyConfig,
                performanceConfig = performanceConfig,
                modifier = if (apps.isEmpty()) {
                    Modifier.focusRequester(firstAppFocusRequester)
                } else {
                    Modifier
                },
                onClick = onOpenAllApplications,
                onMoveUp = onMoveFocusUpToBanner,
                onMoveRightEdge = if (apps.isEmpty()) onMoveFocusToRightNav else null
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTileCard(
    app: AppItem,
    iconConfig: IconCustomizationConfig,
    typographyConfig: TypographyConfig,
    performanceConfig: PerformanceConfig,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onMoveUp: (() -> Unit)? = null,
    onMoveRightEdge: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val imageBitmap = remember(app.packageName, app.icon) {
        app.icon?.toComposeImageBitmap()
    }

    val baseHeightDp: Dp = iconConfig.customSizeDp.dp
    val tileWidthDp: Dp = when (iconConfig.shape) {
        IconShapeOption.ROUNDED_RECT -> (iconConfig.customSizeDp * 1.48f).dp
        IconShapeOption.ORIGINAL,
        IconShapeOption.SQUARE,
        IconShapeOption.ROUNDED_SQUARE,
        IconShapeOption.CIRCLE -> iconConfig.customSizeDp.dp
    }

    val tileShape: Shape = when (iconConfig.shape) {
        IconShapeOption.ORIGINAL -> RoundedCornerShape(iconConfig.tileCornerRadiusDp.dp)
        IconShapeOption.SQUARE -> RectangleShape
        IconShapeOption.ROUNDED_SQUARE -> RoundedCornerShape(iconConfig.tileCornerRadiusDp.dp)
        IconShapeOption.CIRCLE -> CircleShape
        IconShapeOption.ROUNDED_RECT -> RoundedCornerShape(iconConfig.tileCornerRadiusDp.dp)
    }

    val scale by animateFloatAsState(
        targetValue = if (isFocused && !performanceConfig.minimizeAnimations && !performanceConfig.lowEffectsMode) 1.06f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "appTileScale"
    )

    val bgAlpha = iconConfig.tileBackgroundOpacity.coerceIn(0.15f, 1f)
    val baseBrandColor = app.brandAccentColorHex?.let { Color(it) } ?: Color(0xFF151E3B)

    val tileBrush = if (iconConfig.shape == IconShapeOption.ORIGINAL && !isFocused) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF131B36).copy(alpha = bgAlpha * 0.55f),
                Color(0xFF0C1226).copy(alpha = bgAlpha * 0.55f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                baseBrandColor.copy(alpha = bgAlpha),
                Color(0xFF0E152D).copy(alpha = bgAlpha)
            )
        )
    }

    val showGlow = isFocused && iconConfig.enableSelectedGlow && !performanceConfig.lowEffectsMode
    val labelSizeSp = (iconConfig.labelFontSizeSp * typographyConfig.globalFontScale).sp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(tileWidthDp)
            .height(baseHeightDp)
            .scale(scale)
            .drawBehind {
                if (showGlow) {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x66A855F7),
                                Color(0x3338BDF8),
                                Color.Transparent
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.maxDimension * 0.72f
                        ),
                        cornerRadius = CornerRadius(24f, 24f)
                    )
                }
            }
            .clip(tileShape)
            .background(brush = tileBrush, shape = tileShape)
            .border(
                width = if (isFocused) iconConfig.focusBorderThicknessDp.dp else 1.dp,
                brush = if (isFocused) {
                    Brush.linearGradient(
                        colors = listOf(LuminousCyanGlow, LuminousPurpleGlow)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(Color(0x3364748B), Color(0x1864748B))
                    )
                },
                shape = tileShape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionUp -> {
                            if (onMoveUp != null) {
                                onMoveUp()
                                true
                            } else false
                        }

                        Key.DirectionRight -> {
                            if (onMoveRightEdge != null) {
                                onMoveRightEdge()
                                true
                            } else false
                        }

                        Key.Menu, Key.Bookmark -> {
                            onLongClick()
                            true
                        }

                        else -> false
                    }
                } else false
            }
            .focusable()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("app_tile_${app.packageName}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            val iconBoxSize = if (iconConfig.showLabels) {
                (iconConfig.customSizeDp * 0.46f).coerceIn(32f, 76f).dp
            } else {
                (iconConfig.customSizeDp * 0.64f).coerceIn(40f, 96f).dp
            }

            if (imageBitmap != null) {
                val iconClipShape = when (iconConfig.shape) {
                    IconShapeOption.CIRCLE -> CircleShape
                    IconShapeOption.SQUARE -> RectangleShape
                    else -> RoundedCornerShape(10.dp)
                }
                Image(
                    bitmap = imageBitmap,
                    contentDescription = app.label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(iconBoxSize)
                        .clip(iconClipShape)
                )
            } else {
                NavVectorIcon(
                    iconName = "apps",
                    tint = Color.White,
                    modifier = Modifier.size(iconBoxSize * 0.75f)
                )
            }

            if (iconConfig.showLabels) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = labelSizeSp,
                        fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (app.isFavorite) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFBBF24))
            )
        }
    }
}

@Composable
fun AllApplicationsTileCard(
    iconConfig: IconCustomizationConfig,
    typographyConfig: TypographyConfig,
    performanceConfig: PerformanceConfig,
    onClick: () -> Unit,
    onMoveUp: (() -> Unit)? = null,
    onMoveRightEdge: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val baseHeightDp: Dp = iconConfig.customSizeDp.dp
    val tileWidthDp: Dp = when (iconConfig.shape) {
        IconShapeOption.ROUNDED_RECT -> (iconConfig.customSizeDp * 1.48f).dp
        else -> iconConfig.customSizeDp.dp
    }

    val tileShape: Shape = when (iconConfig.shape) {
        IconShapeOption.SQUARE -> RectangleShape
        IconShapeOption.CIRCLE -> CircleShape
        else -> RoundedCornerShape(iconConfig.tileCornerRadiusDp.dp)
    }

    val scale by animateFloatAsState(
        targetValue = if (isFocused && !performanceConfig.minimizeAnimations && !performanceConfig.lowEffectsMode) 1.06f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "allAppsTileScale"
    )

    val bgAlpha = iconConfig.tileBackgroundOpacity.coerceIn(0.15f, 1f)
    val labelSizeSp = (iconConfig.labelFontSizeSp * typographyConfig.globalFontScale).sp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(tileWidthDp)
            .height(baseHeightDp)
            .scale(scale)
            .clip(tileShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1D2646).copy(alpha = bgAlpha),
                        Color(0xFF111830).copy(alpha = bgAlpha)
                    )
                ),
                shape = tileShape
            )
            .border(
                width = if (isFocused) iconConfig.focusBorderThicknessDp.dp else 1.dp,
                brush = if (isFocused) {
                    Brush.linearGradient(colors = listOf(LuminousCyanGlow, LuminousPurpleGlow))
                } else {
                    Brush.linearGradient(colors = listOf(Color(0x4464748B), Color(0x2264748B)))
                },
                shape = tileShape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionUp -> {
                            if (onMoveUp != null) {
                                onMoveUp()
                                true
                            } else false
                        }

                        Key.DirectionRight -> {
                            if (onMoveRightEdge != null) {
                                onMoveRightEdge()
                                true
                            } else false
                        }

                        else -> false
                    }
                } else false
            }
            .focusable()
            .clickable { onClick() }
            .padding(10.dp)
            .testTag("all_applications_tile")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            NavVectorIcon(
                iconName = "apps",
                tint = Color.White,
                modifier = Modifier.size((iconConfig.customSizeDp * 0.34f).coerceIn(24f, 44f).dp)
            )
            if (iconConfig.showLabels) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "كل التطبيقات",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = labelSizeSp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
