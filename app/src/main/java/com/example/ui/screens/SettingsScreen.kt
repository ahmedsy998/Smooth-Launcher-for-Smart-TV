package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.LauncherPreferencesRepository
import com.example.model.*
import com.example.ui.components.NavVectorIcon
import com.example.ui.theme.LuminousCyanGlow
import com.example.ui.theme.LuminousPurpleGlow
import com.example.ui.theme.MidnightNavyElevated
import com.example.ui.theme.SoftLavenderText
import com.example.viewmodel.LauncherUiState
import com.example.viewmodel.LauncherViewModel

@Composable
fun SettingsScreenContent(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    contentFocusRequester: FocusRequester,
    onPickBackgroundFromFiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_root"),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Settings Categories Sidebar (Right side in RTL)
        Column(
            modifier = Modifier
                .width(250.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xB80E1632))
                .border(1.dp, Color(0x336366F1), RoundedCornerShape(22.dp))
                .padding(12.dp)
                .testTag("settings_categories_list")
        ) {
            Text(
                text = "الإعدادات",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = SettingsSubScreen.entries,
                    key = { _, item -> item.id }
                ) { index, subScreen ->
                    val isSelected = uiState.activeSettingsSubScreen == subScreen
                    var isFocused by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when {
                                    isSelected -> Brush.horizontalGradient(
                                        listOf(Color(0xFF7E22CE), Color(0xFF2563EB))
                                    )

                                    isFocused -> Brush.horizontalGradient(
                                        listOf(Color(0x664C1D95), Color(0x661E3A8A))
                                    )

                                    else -> Brush.horizontalGradient(
                                        listOf(Color.Transparent, Color.Transparent)
                                    )
                                }
                            )
                            .border(
                                width = if (isFocused) 1.8.dp else if (isSelected) 1.dp else 0.dp,
                                color = if (isFocused) LuminousCyanGlow else if (isSelected) LuminousPurpleGlow else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .let {
                                if (index == 0) it.focusRequester(contentFocusRequester) else it
                            }
                            .onFocusChanged { isFocused = it.isFocused }
                            .focusable()
                            .clickable { viewModel.selectSettingsSubScreen(subScreen) }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                            .testTag("settings_tab_${subScreen.id}")
                    ) {
                        Text(
                            text = subScreen.titleAr,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subScreen.titleEn,
                            style = MaterialTheme.typography.labelMedium,
                            color = SoftLavenderText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Active Settings Detail Pane (Left side in RTL)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xAD0B1126))
                .border(1.dp, Color(0x33475569), RoundedCornerShape(22.dp))
                .padding(18.dp)
                .testTag("settings_detail_pane")
        ) {
            when (uiState.activeSettingsSubScreen) {
                SettingsSubScreen.GENERAL -> GeneralSettingsPane(
                    uiState = uiState,
                    viewModel = viewModel
                )

                SettingsSubScreen.CUSTOMIZE_MENU -> CustomizeHomeMenuPane(
                    uiState = uiState,
                    viewModel = viewModel
                )

                SettingsSubScreen.ICON_CUSTOMIZATION -> IconCustomizationPane(
                    uiState = uiState,
                    viewModel = viewModel
                )

                SettingsSubScreen.FONTS_TYPOGRAPHY -> FontsAndTypographyPane(
                    uiState = uiState,
                    viewModel = viewModel
                )

                SettingsSubScreen.BACKGROUND_APPEARANCE -> BackgroundAndAppearancePane(
                    uiState = uiState,
                    viewModel = viewModel,
                    onPickBackgroundFromFiles = onPickBackgroundFromFiles
                )

                SettingsSubScreen.CLOCK_DATE -> ClockAndDatePane(
                    uiState = uiState,
                    viewModel = viewModel
                )

                SettingsSubScreen.HOME_LAYOUT -> HomeScreenLayoutPane(
                    uiState = uiState,
                    viewModel = viewModel
                )

                SettingsSubScreen.REMOTE_NAVIGATION -> RemoteNavigationPane(
                    uiState = uiState,
                    viewModel = viewModel
                )

                SettingsSubScreen.PERFORMANCE -> PerformanceOptionsPane(
                    uiState = uiState,
                    viewModel = viewModel
                )

                SettingsSubScreen.RESTORE_DEFAULTS -> RestoreDefaultSettingsPane(
                    viewModel = viewModel
                )

                SettingsSubScreen.APP_INFO -> ApplicationInformationPane(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun CustomizeHomeMenuPane(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel
) {
    var itemToRename by remember { mutableStateOf<NavMenuItem?>(null) }
    var renameText by remember { mutableStateOf("") }
    var itemToConfirmRemove by remember { mutableStateOf<NavMenuItem?>(null) }
    var itemToRestoreWithPosition by remember { mutableStateOf<NavMenuItem?>(null) }
    var showAddShortcutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("customize_home_menu_screen")
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تخصيص القائمة الرئيسية — Customize Home Menu",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "تحكم كامل في ترتيب عناصر القائمة اليمنى، إخفائها، إزالتها، إعادة تسميتها أو إضافة اختصارات جديدة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftLavenderText
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TvActionButton(
                        text = "إضافة اختصار +",
                        isPrimary = true,
                        testTagName = "add_menu_shortcut_button",
                        onClick = { showAddShortcutDialog = true }
                    )
                    TvActionButton(
                        text = "استعادة القائمة الافتراضية (Restore Default Menu)",
                        testTagName = "restore_default_menu_button",
                        onClick = { viewModel.restoreDefaultMenu() }
                    )
                }
            }
        }

        // Visible Menu Items List
        item {
            Text(
                text = "العناصر الظاهرة في القائمة اليمنى (${uiState.visibleMenuItems.size}):",
                style = MaterialTheme.typography.titleLarge,
                color = LuminousCyanGlow
            )
        }

        itemsIndexed(
            items = uiState.visibleMenuItems,
            key = { _, item -> "vis_${item.id}" }
        ) { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x99141D3D))
                    .border(1.dp, Color(0x446366F1), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("custom_menu_row_${item.id}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "#${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = LuminousCyanGlow
                    )
                    NavVectorIcon(
                        iconName = item.iconName,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = item.displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        if (item.customTitle.isNotBlank()) {
                            Text(
                                text = "الاسم الأصلي: ${item.defaultTitleAr}",
                                style = MaterialTheme.typography.labelMedium,
                                color = SoftLavenderText
                            )
                        }
                    }
                }

                // Remote-friendly action controls: Move Up, Move Down, Rename, Hide, Remove
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TvActionButton(
                        text = "تحريك لأعلى ↑",
                        testTagName = "move_up_${item.id}",
                        onClick = { viewModel.moveMenuItemUp(item.id) }
                    )
                    TvActionButton(
                        text = "تحريك لأسفل ↓",
                        testTagName = "move_down_${item.id}",
                        onClick = { viewModel.moveMenuItemDown(item.id) }
                    )
                    TvActionButton(
                        text = "إعادة تسمية",
                        testTagName = "rename_${item.id}",
                        onClick = {
                            itemToRename = item
                            renameText = item.displayTitle
                        }
                    )
                    TvActionButton(
                        text = "إخفاء",
                        testTagName = "hide_${item.id}",
                        onClick = { viewModel.hideMenuItem(item.id) }
                    )
                    TvActionButton(
                        text = "إزالة",
                        testTagName = "remove_${item.id}",
                        onClick = {
                            if (uiState.remoteNavConfig.confirmBeforeRemovingMenuItem) {
                                itemToConfirmRemove = item
                            } else {
                                viewModel.removeMenuItem(item.id)
                            }
                        }
                    )
                }
            }
        }

        // Hidden Menu Items Section
        if (uiState.hiddenMenuItems.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "العناصر المخفية — Hidden Items (${uiState.hiddenMenuItems.size}):",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFFBBF24)
                )
            }

            items(
                items = uiState.hiddenMenuItems,
                key = { "hid_${it.id}" }
            ) { hiddenItem ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x661E293B))
                        .border(1.dp, Color(0x55FBBF24), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${hiddenItem.displayTitle} (مخفي مؤقتاً)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TvActionButton(
                            text = "استعادة في نهاية القائمة (Restore)",
                            onClick = {
                                viewModel.restoreMenuItemAtPosition(
                                    hiddenItem.id,
                                    uiState.visibleMenuItems.size
                                )
                            }
                        )
                        TvActionButton(
                            text = "استعادة مع اختيار الموضع...",
                            onClick = { itemToRestoreWithPosition = hiddenItem }
                        )
                    }
                }
            }
        }

        // Removed Default Sections List (can also be restored)
        if (uiState.removedMenuItems.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "الأقسام المزالة من القائمة (${uiState.removedMenuItems.size}):",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoftLavenderText
                )
            }

            items(
                items = uiState.removedMenuItems,
                key = { "rem_${it.id}" }
            ) { removedItem ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x441E293B))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = removedItem.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = SoftLavenderText
                    )
                    TvActionButton(
                        text = "إعادة الإضافة للقائمة (Restore)",
                        onClick = {
                            itemToRestoreWithPosition = removedItem
                        }
                    )
                }
            }
        }
    }

    // Rename Dialog
    itemToRename?.let { target ->
        Dialog(onDismissRequest = { itemToRename = null }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MidnightNavyElevated,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .border(1.5.dp, LuminousCyanGlow, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "إعادة تسمية العنصر (${target.defaultTitleAr})",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        label = { Text("الاسم المخصص في القائمة") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rename_menu_text_field")
                    )
                    // Quick preset suggestions for convenience on TV remote
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(target.defaultTitleAr, "فيديو", "تطبيقاتي", "وسائط", "سينما").forEach { suggestion ->
                            TvFilterChip(
                                label = suggestion,
                                selected = renameText == suggestion,
                                onClick = { renameText = suggestion }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                    ) {
                        TvActionButton(
                            text = "الاسم الافتراضي",
                            onClick = {
                                viewModel.renameMenuItem(target.id, "")
                                itemToRename = null
                            }
                        )
                        TvActionButton(
                            text = "حفظ الاسم",
                            isPrimary = true,
                            testTagName = "confirm_rename_menu_button",
                            onClick = {
                                viewModel.renameMenuItem(target.id, renameText)
                                itemToRename = null
                            }
                        )
                    }
                }
            }
        }
    }

    // Remove Confirmation Dialog
    itemToConfirmRemove?.let { target ->
        Dialog(onDismissRequest = { itemToConfirmRemove = null }) {
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
                        text = "تأكيد إزالة (${target.displayTitle}) من القائمة",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "سيتم إزالة هذا القسم من القائمة المرئية فقط ولن يتم حذف أي تطبيقات أو ملفات شخصية. يمكنك استعادته لاحقاً في أي وقت.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftLavenderText
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                    ) {
                        TvActionButton(
                            text = "إلغاء",
                            onClick = { itemToConfirmRemove = null }
                        )
                        TvActionButton(
                            text = "تأكيد الإزالة",
                            isPrimary = true,
                            testTagName = "confirm_remove_menu_button",
                            onClick = {
                                viewModel.removeMenuItem(target.id)
                                itemToConfirmRemove = null
                            }
                        )
                    }
                }
            }
        }
    }

    // Restore with Position Picker Dialog
    itemToRestoreWithPosition?.let { target ->
        Dialog(onDismissRequest = { itemToRestoreWithPosition = null }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MidnightNavyElevated,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .border(1.5.dp, LuminousCyanGlow, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "اختر موضع استعادة (${target.displayTitle}) في القائمة:",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    for (pos in 0..uiState.visibleMenuItems.size) {
                        val posLabel = when (pos) {
                            0 -> "في أعلى القائمة (الموضع #1)"
                            uiState.visibleMenuItems.size -> "في نهاية القائمة (الموضع #${pos + 1})"
                            else -> "الموضع #${pos + 1} (بعد ${uiState.visibleMenuItems[pos - 1].displayTitle})"
                        }
                        TvActionButton(
                            text = posLabel,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                viewModel.restoreMenuItemAtPosition(target.id, pos)
                                itemToRestoreWithPosition = null
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Supported Shortcut Dialog
    if (showAddShortcutDialog) {
        Dialog(onDismissRequest = { showAddShortcutDialog = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MidnightNavyElevated,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .heightIn(max = 500.dp)
                    .border(1.5.dp, LuminousPurpleGlow, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "إضافة اختصار مدعوم إلى القائمة الجانبية",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "اختصارات إعدادات النظام والأقسام المدعومة:",
                                style = MaterialTheme.typography.titleMedium,
                                color = LuminousCyanGlow
                            )
                        }

                        items(LauncherPreferencesRepository.supportedSystemShortcuts()) { sysShortcut ->
                            TvActionButton(
                                text = "+ ${sysShortcut.titleAr}",
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    viewModel.addSystemShortcutToMenu(sysShortcut)
                                    showAddShortcutDialog = false
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "إضافة اختصار لتطبيق مثبت على الجهاز:",
                                style = MaterialTheme.typography.titleMedium,
                                color = LuminousCyanGlow
                            )
                        }

                        items(uiState.installedApps, key = { it.packageName }) { app ->
                            TvActionButton(
                                text = "+ تطبيق: ${app.label}",
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    viewModel.addAppShortcutToMenu(app)
                                    showAddShortcutDialog = false
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TvActionButton(
                            text = "إغلاق",
                            onClick = { showAddShortcutDialog = false }
                        )
                    }
                }
            }
        }
    }
}
