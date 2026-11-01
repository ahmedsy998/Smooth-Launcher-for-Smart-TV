package com.example.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClockDateConfig
import com.example.model.NetworkStatus
import com.example.model.TypographyConfig
import com.example.ui.theme.ConnectedCyanWhite
import com.example.ui.theme.DisconnectedRedSlash
import com.example.ui.theme.LuminousCyanGlow
import com.example.ui.theme.SoftLavenderText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TopStatusArea(
    clockDateConfig: ClockDateConfig,
    typographyConfig: TypographyConfig,
    networkStatus: NetworkStatus,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isWifiFocused by remember { mutableStateOf(false) }
    var showNetworkDetails by remember { mutableStateOf(false) }

    // Lifecycle-aware clock listener via system time tick broadcasts (zero polling overhead)
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                currentTimeMillis = System.currentTimeMillis()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        context.registerReceiver(receiver, filter)
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    val arabicLocale = remember { Locale("ar") }
    val timeText = remember(currentTimeMillis, clockDateConfig.is24HourFormat) {
        val pattern = if (clockDateConfig.is24HourFormat) "HH:mm" else "hh:mm a"
        // Format time digits cleanly in standard numerals as shown in reference ("20:45")
        SimpleDateFormat(pattern, Locale.US).format(Date(currentTimeMillis))
    }

    val dateTextAr = remember(currentTimeMillis) {
        SimpleDateFormat("EEEE، d MMMM", arabicLocale).format(Date(currentTimeMillis))
    }

    // Top Status Area: Clean and unobtrusive, displaying ONLY current time, Arabic date, and real-time Wi-Fi indicator.
    // No launcher logo, no application name, no Bluetooth icon, and no notification icon.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Real-time Internet Connectivity Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isWifiFocused) Color(0x3338BDF8) else Color.Transparent
                    )
                    .border(
                        width = if (isWifiFocused) 1.5.dp else 0.dp,
                        color = if (isWifiFocused) LuminousCyanGlow else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .onFocusChanged {
                        isWifiFocused = it.isFocused
                        if (!it.isFocused) showNetworkDetails = false
                    }
                    .focusable()
                    .clickable { showNetworkDetails = !showNetworkDetails }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .semantics {
                        contentDescription = networkStatus.labelAr
                    }
                    .testTag("wifi_connectivity_indicator")
            ) {
                WifiStatusCanvasIcon(
                    networkStatus = networkStatus,
                    modifier = Modifier.size(26.dp)
                )

                AnimatedVisibility(visible = isWifiFocused || showNetworkDetails || !networkStatus.isConnectedWithInternet) {
                    Text(
                        text = networkStatus.labelAr,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (networkStatus.isConnectedWithInternet) SoftLavenderText else DisconnectedRedSlash,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            if (clockDateConfig.showClock || clockDateConfig.showDate) {
                Spacer(modifier = Modifier.width(18.dp))

                // Subtle vertical divider line matching reference top status area
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(Color(0x3394A3B8))
                )

                Spacer(modifier = Modifier.width(18.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.testTag("clock_date_container")
                ) {
                    if (clockDateConfig.showClock) {
                        val effectiveClockSp = clockDateConfig.clockSizeSp * typographyConfig.globalFontScale
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = effectiveClockSp.sp,
                                lineHeight = (effectiveClockSp * 1.1f).sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            modifier = Modifier.testTag("status_clock_text")
                        )
                    }
                    if (clockDateConfig.showDate) {
                        val effectiveDateSp = clockDateConfig.dateSizeSp * typographyConfig.globalFontScale
                        Text(
                            text = dateTextAr,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = effectiveDateSp.sp,
                                lineHeight = (effectiveDateSp * 1.2f).sp
                            ),
                            color = SoftLavenderText,
                            modifier = Modifier.testTag("status_date_text")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WifiStatusCanvasIcon(
    networkStatus: NetworkStatus,
    modifier: Modifier = Modifier
) {
    val isConnected = networkStatus is NetworkStatus.ConnectedValidated
    val arcColor = if (isConnected) ConnectedCyanWhite else Color(0x99CBD5E1)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.095f
        val centerBottom = Offset(w * 0.5f, h * 0.82f)

        // Outer Wi-Fi arc
        val r1 = w * 0.72f
        drawArc(
            color = arcColor,
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(centerBottom.x - r1 / 2f, centerBottom.y - r1 / 2f),
            size = Size(r1, r1),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Middle Wi-Fi arc
        val r2 = w * 0.46f
        drawArc(
            color = arcColor,
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(centerBottom.x - r2 / 2f, centerBottom.y - r2 / 2f),
            size = Size(r2, r2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Bottom Wi-Fi dot
        drawCircle(
            color = arcColor,
            radius = strokeWidth * 0.72f,
            center = Offset(centerBottom.x, h * 0.78f)
        )

        // Disconnected or unvalidated network: draw prominent red diagonal slash
        if (!isConnected) {
            drawLine(
                color = Color(0xFF060A17),
                start = Offset(w * 0.16f, h * 0.16f),
                end = Offset(w * 0.86f, h * 0.86f),
                strokeWidth = strokeWidth * 2.1f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = DisconnectedRedSlash,
                start = Offset(w * 0.16f, h * 0.16f),
                end = Offset(w * 0.86f, h * 0.86f),
                strokeWidth = strokeWidth * 1.25f,
                cap = StrokeCap.Round
            )
        }
    }
}
