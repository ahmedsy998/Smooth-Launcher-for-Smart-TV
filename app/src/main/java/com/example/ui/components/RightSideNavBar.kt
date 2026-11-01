package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NavMenuItem
import com.example.model.PerformanceConfig
import com.example.model.TypographyConfig
import com.example.ui.theme.LuminousCyanGlow
import com.example.ui.theme.LuminousPurpleGlow
import com.example.ui.theme.SoftLavenderText
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RightSideNavBar(
    menuItems: List<NavMenuItem>,
    selectedMenuId: String,
    compactWidth: Boolean,
    typographyConfig: TypographyConfig,
    performanceConfig: PerformanceConfig,
    firstNavFocusRequester: FocusRequester,
    onSelectMenuItem: (NavMenuItem) -> Unit,
    onMoveFocusToContent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navWidth = if (compactWidth) 192.dp else 220.dp
    val containerShape = RoundedCornerShape(26.dp)

    Column(
        modifier = modifier
            .width(navWidth)
            .fillMaxHeight()
            .clip(containerShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xD90E1530),
                        Color(0xCC090E22)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x446366F1),
                        Color(0x1838BDF8),
                        Color(0x337C3AED)
                    )
                ),
                shape = containerShape
            )
            .padding(vertical = 12.dp, horizontal = 10.dp)
            .testTag("right_side_navigation_menu"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            items(
                items = menuItems,
                key = { it.id }
            ) { item ->
                val isFirst = menuItems.firstOrNull()?.id == item.id
                val isSelected = item.id == selectedMenuId

                RightNavMenuItemRow(
                    item = item,
                    isSelected = isSelected,
                    typographyConfig = typographyConfig,
                    performanceConfig = performanceConfig,
                    modifier = if (isFirst) {
                        Modifier.focusRequester(firstNavFocusRequester)
                    } else {
                        Modifier
                    },
                    onClick = { onSelectMenuItem(item) },
                    onNavigateLeftToContent = onMoveFocusToContent
                )
            }
        }
    }
}

@Composable
private fun RightNavMenuItemRow(
    item: NavMenuItem,
    isSelected: Boolean,
    typographyConfig: TypographyConfig,
    performanceConfig: PerformanceConfig,
    onClick: () -> Unit,
    onNavigateLeftToContent: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused && !performanceConfig.minimizeAnimations && !performanceConfig.lowEffectsMode) 1.03f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "navItemScale"
    )

    val pillShape = RoundedCornerShape(18.dp)
    val isHighlighted = isSelected || isFocused

    // Reference image active item styling: vibrant purple-to-blue gradient with luminous border
    val backgroundModifier = when {
        isSelected && isFocused -> Modifier.background(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF9333EA),
                    Color(0xFF4F46E5),
                    Color(0xFF2563EB)
                )
            ),
            shape = pillShape
        )

        isSelected -> Modifier.background(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF7E22CE),
                    Color(0xFF3B82F6),
                    Color(0xFF1D4ED8)
                )
            ),
            shape = pillShape
        )

        isFocused -> Modifier.background(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0x994C1D95),
                    Color(0x991E3A8A)
                )
            ),
            shape = pillShape
        )

        else -> Modifier.background(Color.Transparent, shape = pillShape)
    }

    val borderModifier = when {
        isFocused -> Modifier.border(
            width = 2.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFF0ABFC),
                    LuminousCyanGlow
                )
            ),
            shape = pillShape
        )

        isSelected -> Modifier.border(
            width = 1.2.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    LuminousPurpleGlow,
                    Color(0x8838BDF8)
                )
            ),
            shape = pillShape
        )

        else -> Modifier
    }

    val fontSizeSp = (typographyConfig.navMenuFontSizeSp * typographyConfig.globalFontScale).sp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .scale(scale)
            .clip(pillShape)
            .then(backgroundModifier)
            .then(borderModifier)
            .onFocusChanged { state ->
                isFocused = state.isFocused
            }
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionLeft) {
                    onNavigateLeftToContent()
                    true
                } else {
                    false
                }
            }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 11.dp)
            .testTag("nav_item_${item.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Clean consistent vector icon matching reference image
        NavVectorIcon(
            iconName = item.iconName,
            tint = if (isHighlighted) Color.White else SoftLavenderText,
            modifier = Modifier.size(22.dp)
        )

        Text(
            text = item.displayTitle,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = fontSizeSp,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isHighlighted) Color.White else Color(0xFFE2E8F0),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun NavVectorIcon(
    iconName: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.095f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (iconName) {
            "home" -> {
                val path = Path().apply {
                    moveTo(w * 0.14f, h * 0.46f)
                    lineTo(w * 0.5f, h * 0.14f)
                    lineTo(w * 0.86f, h * 0.46f)
                    lineTo(w * 0.86f, h * 0.86f)
                    lineTo(w * 0.62f, h * 0.86f)
                    lineTo(w * 0.62f, h * 0.60f)
                    lineTo(w * 0.38f, h * 0.60f)
                    lineTo(w * 0.38f, h * 0.86f)
                    lineTo(w * 0.14f, h * 0.86f)
                    close()
                }
                drawPath(path = path, color = tint, style = stroke)
            }

            "apps" -> {
                val boxSize = w * 0.31f
                val gap = w * 0.14f
                val start = w * 0.12f
                val r = CornerRadius(w * 0.07f, w * 0.07f)
                for (row in 0..1) {
                    for (col in 0..1) {
                        drawRoundRect(
                            color = tint,
                            topLeft = Offset(
                                start + col * (boxSize + gap),
                                start + row * (boxSize + gap)
                            ),
                            size = Size(boxSize, boxSize),
                            cornerRadius = r,
                            style = stroke
                        )
                    }
                }
            }

            "movies" -> {
                val path = Path().apply {
                    moveTo(w * 0.24f, h * 0.16f)
                    lineTo(w * 0.84f, h * 0.50f)
                    lineTo(w * 0.24f, h * 0.84f)
                    close()
                }
                drawPath(path = path, color = tint, style = stroke)
            }

            "tv" -> {
                // TV screen with antenna
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.12f, h * 0.30f),
                    size = Size(w * 0.76f, h * 0.54f),
                    cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
                    style = stroke
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.34f, h * 0.12f),
                    end = Offset(w * 0.48f, h * 0.30f),
                    strokeWidth = w * 0.09f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.66f, h * 0.12f),
                    end = Offset(w * 0.52f, h * 0.30f),
                    strokeWidth = w * 0.09f,
                    cap = StrokeCap.Round
                )
            }

            "star" -> {
                val path = Path()
                val cx = w * 0.5f
                val cy = h * 0.52f
                val outerR = w * 0.40f
                val innerR = w * 0.19f
                for (i in 0 until 10) {
                    val angle = Math.toRadians((i * 36.0) - 90.0)
                    val r = if (i % 2 == 0) outerR else innerR
                    val x = (cx + r * cos(angle)).toFloat()
                    val y = (cy + r * sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path = path, color = tint, style = stroke)
            }

            "folder" -> {
                val path = Path().apply {
                    moveTo(w * 0.12f, h * 0.24f)
                    lineTo(w * 0.40f, h * 0.24f)
                    lineTo(w * 0.50f, h * 0.36f)
                    lineTo(w * 0.88f, h * 0.36f)
                    lineTo(w * 0.88f, h * 0.80f)
                    lineTo(w * 0.12f, h * 0.80f)
                    close()
                }
                drawPath(path = path, color = tint, style = stroke)
            }

            "settings" -> {
                val cx = w * 0.5f
                val cy = h * 0.5f
                drawCircle(
                    color = tint,
                    radius = w * 0.16f,
                    center = Offset(cx, cy),
                    style = stroke
                )
                drawCircle(
                    color = tint,
                    radius = w * 0.32f,
                    center = Offset(cx, cy),
                    style = stroke
                )
                for (i in 0 until 8) {
                    val angle = Math.toRadians(i * 45.0)
                    val x1 = (cx + (w * 0.32f) * cos(angle)).toFloat()
                    val y1 = (cy + (h * 0.32f) * sin(angle)).toFloat()
                    val x2 = (cx + (w * 0.42f) * cos(angle)).toFloat()
                    val y2 = (cy + (h * 0.42f) * sin(angle)).toFloat()
                    drawLine(
                        color = tint,
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = w * 0.095f,
                        cap = StrokeCap.Round
                    )
                }
            }

            else -> {
                // Shortcut / App badge icon
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.16f, h * 0.16f),
                    size = Size(w * 0.68f, h * 0.68f),
                    cornerRadius = CornerRadius(w * 0.18f, w * 0.18f),
                    style = stroke
                )
                drawCircle(
                    color = tint,
                    radius = w * 0.12f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
            }
        }
    }
}
