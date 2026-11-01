package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import com.example.model.BackgroundConfig
import com.example.model.BackgroundScaleMode
import com.example.model.BuiltInBackgroundPreset
import com.example.model.PerformanceConfig

@Composable
fun ReferenceBackgroundCanvas(
    backgroundConfig: BackgroundConfig,
    customWallpaperBitmap: ImageBitmap?,
    performanceConfig: PerformanceConfig,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060A17))
    ) {
        if (customWallpaperBitmap != null && backgroundConfig.customImageUri.isNotBlank()) {
            val contentScale = when (backgroundConfig.scaleMode) {
                BackgroundScaleMode.FILL_SCREEN -> ContentScale.FillBounds
                BackgroundScaleMode.FIT_IMAGE -> ContentScale.Fit
                BackgroundScaleMode.CENTER_CROP -> ContentScale.Crop
            }
            Image(
                bitmap = customWallpaperBitmap,
                contentDescription = "خلفية الشاشة المخصصة",
                contentScale = contentScale,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            )
            // Subtle dark overlay for legibility over bright custom photos
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x55050814))
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val hideDecor = performanceConfig.disableDecorativeAccents || performanceConfig.lowEffectsMode

                when (backgroundConfig.preset) {
                    BuiltInBackgroundPreset.DARK_NAVY_REFERENCE -> {
                        // Base dark navy & midnight-blue gradient matching reference image
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF070C1F),
                                    Color(0xFF0B132B),
                                    Color(0xFF090D20)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(w, h)
                            )
                        )

                        if (!hideDecor) {
                            // Subtle top-left / upper-middle sweeping blue curve from reference
                            val topWavePath = Path().apply {
                                moveTo(0f, h * 0.25f)
                                cubicTo(
                                    w * 0.16f, h * 0.22f,
                                    w * 0.28f, h * 0.11f,
                                    w * 0.42f, 0f
                                )
                                lineTo(0f, 0f)
                                close()
                            }
                            drawPath(
                                path = topWavePath,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0x441D3587),
                                        Color(0x66254EDB),
                                        Color(0x11101B42)
                                    ),
                                    start = Offset(0f, h * 0.25f),
                                    end = Offset(w * 0.42f, 0f)
                                )
                            )

                            val topEdgeHighlight = Path().apply {
                                moveTo(w * 0.14f, h * 0.20f)
                                quadraticTo(
                                    w * 0.27f, h * 0.13f,
                                    w * 0.42f, 0f
                                )
                            }
                            drawPath(
                                path = topEdgeHighlight,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x8838BDF8),
                                        Color(0xAA60A5FA)
                                    )
                                ),
                                style = Stroke(width = 2.2f)
                            )

                            // Ambient purple-blue glow behind banner right side
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x387C3AED),
                                        Color(0x143B82F6),
                                        Color.Transparent
                                    ),
                                    center = Offset(w * 0.82f, h * 0.56f),
                                    radius = w * 0.32f
                                ),
                                center = Offset(w * 0.82f, h * 0.56f),
                                radius = w * 0.32f
                            )

                            // Bottom left sweeping purple-blue wave from reference image
                            val bottomLeftWave = Path().apply {
                                moveTo(0f, h * 0.94f)
                                cubicTo(
                                    w * 0.15f, h * 0.85f,
                                    w * 0.34f, h * 0.90f,
                                    w * 0.53f, h
                                )
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(
                                path = bottomLeftWave,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xAA3B1C8C),
                                        Color(0x99253998),
                                        Color(0x330F1738)
                                    ),
                                    start = Offset(w * 0.08f, h * 0.88f),
                                    end = Offset(w * 0.52f, h)
                                )
                            )

                            val bottomLeftRim = Path().apply {
                                moveTo(0f, h * 0.94f)
                                cubicTo(
                                    w * 0.15f, h * 0.85f,
                                    w * 0.34f, h * 0.90f,
                                    w * 0.53f, h
                                )
                            }
                            drawPath(
                                path = bottomLeftRim,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0x446366F1),
                                        Color(0xFFD946EF),
                                        Color(0x996366F1),
                                        Color.Transparent
                                    )
                                ),
                                style = Stroke(width = 2.8f)
                            )

                            // Bottom right sweeping cyan-blue wave from reference image
                            val bottomRightWave = Path().apply {
                                moveTo(w * 0.68f, h)
                                cubicTo(
                                    w * 0.78f, h * 0.91f,
                                    w * 0.90f, h * 0.89f,
                                    w, h * 0.94f
                                )
                                lineTo(w, h)
                                close()
                            }
                            drawPath(
                                path = bottomRightWave,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0x881E3A8A),
                                        Color(0xAA2563EB),
                                        Color(0x550F172A)
                                    ),
                                    start = Offset(w * 0.7f, h * 0.9f),
                                    end = Offset(w, h)
                                )
                            )

                            val bottomRightRim = Path().apply {
                                moveTo(w * 0.68f, h)
                                cubicTo(
                                    w * 0.78f, h * 0.91f,
                                    w * 0.90f, h * 0.89f,
                                    w, h * 0.94f
                                )
                            }
                            drawPath(
                                path = bottomRightRim,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF38BDF8),
                                        Color(0x8860A5FA)
                                    )
                                ),
                                style = Stroke(width = 2.5f)
                            )
                        }
                    }

                    BuiltInBackgroundPreset.BLUE_PURPLE_AURORA -> {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F1035),
                                    Color(0xFF231557),
                                    Color(0xFF0B2447)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(w, h)
                            )
                        )
                        if (!hideDecor) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0x558B5CF6), Color.Transparent),
                                    center = Offset(w * 0.25f, h * 0.28f),
                                    radius = w * 0.45f
                                ),
                                center = Offset(w * 0.25f, h * 0.28f),
                                radius = w * 0.45f
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0x4406B6D4), Color.Transparent),
                                    center = Offset(w * 0.78f, h * 0.72f),
                                    radius = w * 0.4f
                                ),
                                center = Offset(w * 0.78f, h * 0.72f),
                                radius = w * 0.4f
                            )
                        }
                    }

                    BuiltInBackgroundPreset.NATURAL_LANDSCAPE -> {
                        // Rich twilight mountain horizon
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0B132B),
                                    Color(0xFF1C2541),
                                    Color(0xFF3A2D56),
                                    Color(0xFF121629)
                                )
                            )
                        )
                        if (!hideDecor) {
                            // Distant mountain range silhouette
                            val mountain1 = Path().apply {
                                moveTo(0f, h * 0.72f)
                                lineTo(w * 0.22f, h * 0.52f)
                                lineTo(w * 0.45f, h * 0.66f)
                                lineTo(w * 0.72f, h * 0.46f)
                                lineTo(w, h * 0.64f)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(mountain1, color = Color(0xFF141B33))

                            val mountain2 = Path().apply {
                                moveTo(0f, h * 0.82f)
                                lineTo(w * 0.35f, h * 0.63f)
                                lineTo(w * 0.62f, h * 0.78f)
                                lineTo(w * 0.88f, h * 0.61f)
                                lineTo(w, h * 0.74f)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(mountain2, color = Color(0xFF0A0F20))
                        }
                    }

                    BuiltInBackgroundPreset.NIGHT_CITY_SCENE -> {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF050814),
                                    Color(0xFF0E1738),
                                    Color(0xFF17224D),
                                    Color(0xFF070B1A)
                                )
                            )
                        )
                        if (!hideDecor) {
                            // Subtle architectural skyline silhouettes along bottom
                            val widths = floatArrayOf(0.06f, 0.08f, 0.05f, 0.09f, 0.07f, 0.08f, 0.06f, 0.10f, 0.07f, 0.09f)
                            val heights = floatArrayOf(0.22f, 0.31f, 0.18f, 0.35f, 0.25f, 0.29f, 0.19f, 0.33f, 0.24f, 0.27f)
                            var curX = w * 0.05f
                            for (i in widths.indices) {
                                val bw = w * widths[i]
                                val bh = h * heights[i]
                                drawRect(
                                    color = Color(0x55080D1E),
                                    topLeft = Offset(curX, h - bh),
                                    size = Size(bw * 0.88f, bh)
                                )
                                curX += bw
                            }
                        }
                    }

                    BuiltInBackgroundPreset.MINIMAL_ABSTRACT -> {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF080C18),
                                    Color(0xFF0F172A),
                                    Color(0xFF090D16)
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}
