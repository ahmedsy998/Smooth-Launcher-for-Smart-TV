package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.LauncherPreferencesRepository
import com.example.model.*
import com.example.viewmodel.LauncherViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var repo: LauncherPreferencesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repo = LauncherPreferencesRepository(context)
        repo.resetAllToDefaults()
    }

    @Test
    fun `app_name string resource matches Smooth TV Launcher`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Smooth TV Launcher", appName)
    }

    @Test
    fun `default right-side navigation menu has all 7 required Arabic items in order`() {
        val items = repo.loadMenuItems()
        assertEquals(7, items.size)
        assertEquals("الرئيسية", items[0].displayTitle)
        assertEquals("التطبيقات", items[1].displayTitle)
        assertEquals("الأفلام", items[2].displayTitle)
        assertEquals("المسلسلات", items[3].displayTitle)
        assertEquals("المفضلة", items[4].displayTitle)
        assertEquals("الملفات", items[5].displayTitle)
        assertEquals("الإعدادات", items[6].displayTitle)
    }

    @Test
    fun `menu items can be reordered, renamed, hidden, removed, restored, and persisted across restart`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = LauncherViewModel(app)

        // 1. Move Applications up to index 0
        vm.moveMenuItemUp(BuiltInSection.APPLICATIONS.id)
        assertEquals(BuiltInSection.APPLICATIONS.id, vm.uiState.value.visibleMenuItems[0].id)
        assertEquals(BuiltInSection.HOME.id, vm.uiState.value.visibleMenuItems[1].id)

        // 2. Rename Movies to "فيديو"
        vm.renameMenuItem(BuiltInSection.MOVIES.id, "فيديو")
        val moviesItem = vm.uiState.value.visibleMenuItems.find { it.id == BuiltInSection.MOVIES.id }
        assertNotNull(moviesItem)
        assertEquals("فيديو", moviesItem?.displayTitle)

        // 3. Hide Series and verify it moves to hiddenMenuItems
        vm.hideMenuItem(BuiltInSection.SERIES.id)
        assertTrue(vm.uiState.value.visibleMenuItems.none { it.id == BuiltInSection.SERIES.id })
        assertTrue(vm.uiState.value.hiddenMenuItems.any { it.id == BuiltInSection.SERIES.id })

        // 4. Remove Favorites from visible menu
        vm.removeMenuItem(BuiltInSection.FAVORITES.id)
        assertTrue(vm.uiState.value.visibleMenuItems.none { it.id == BuiltInSection.FAVORITES.id })
        assertTrue(vm.uiState.value.removedMenuItems.any { it.id == BuiltInSection.FAVORITES.id })

        // 5. Restore hidden Series at position 1
        vm.restoreMenuItemAtPosition(BuiltInSection.SERIES.id, 1)
        assertEquals(BuiltInSection.SERIES.id, vm.uiState.value.visibleMenuItems[1].id)

        // 6. Verify persistence with a fresh Repository instance (simulating restart)
        val freshRepo = LauncherPreferencesRepository(context)
        val reloaded = freshRepo.loadMenuItems()
        val reloadedVisible = reloaded.filter { it.visibility == NavItemVisibility.VISIBLE }
        assertEquals(BuiltInSection.APPLICATIONS.id, reloadedVisible[0].id)
        assertEquals(BuiltInSection.SERIES.id, reloadedVisible[1].id)
        assertEquals("فيديو", reloaded.find { it.id == BuiltInSection.MOVIES.id }?.displayTitle)

        // 7. Restore default menu
        vm.restoreDefaultMenu()
        assertEquals(7, vm.uiState.value.visibleMenuItems.size)
        assertEquals(BuiltInSection.HOME.id, vm.uiState.value.visibleMenuItems.first().id)
    }

    @Test
    fun `icon, typography, background, and clock customizations persist across restarts`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = LauncherViewModel(app)

        // Change icon shape, size, opacity, and glow
        vm.updateIconConfig {
            it.copy(
                shape = IconShapeOption.CIRCLE,
                sizePreset = IconSizePreset.LARGE,
                customSizeDp = 132f,
                showLabels = false,
                tileBackgroundOpacity = 0.65f,
                enableSelectedGlow = false,
                focusBorderThicknessDp = 4.0f
            )
        }

        // Change Arabic font from default Cairo to Readex Pro
        assertEquals(ArabicFontOption.CAIRO, vm.uiState.value.typographyConfig.fontOption)
        vm.updateTypographyConfig {
            it.copy(
                fontOption = ArabicFontOption.READEX_PRO,
                fontWeightOption = FontWeightOption.BOLD,
                globalFontScale = 1.15f
            )
        }

        // Change background preset and scale mode
        vm.selectBuiltInBackground(BuiltInBackgroundPreset.NIGHT_CITY_SCENE)
        vm.updateBackgroundScaleMode(BackgroundScaleMode.FILL_SCREEN)

        // Change clock format and visibility
        vm.updateClockDateConfig {
            it.copy(is24HourFormat = false, showDate = false, clockSizeSp = 32f)
        }

        // Reload from fresh repository
        val freshRepo = LauncherPreferencesRepository(context)
        val savedIcon = freshRepo.loadIconConfig()
        assertEquals(IconShapeOption.CIRCLE, savedIcon.shape)
        assertEquals(132f, savedIcon.customSizeDp, 0.01f)
        assertFalse(savedIcon.showLabels)

        val savedTypo = freshRepo.loadTypographyConfig()
        assertEquals(ArabicFontOption.READEX_PRO, savedTypo.fontOption)
        assertEquals(FontWeightOption.BOLD, savedTypo.fontWeightOption)

        val savedBg = freshRepo.loadBackgroundConfig()
        assertEquals(BuiltInBackgroundPreset.NIGHT_CITY_SCENE, savedBg.preset)
        assertEquals(BackgroundScaleMode.FILL_SCREEN, savedBg.scaleMode)

        val savedClock = freshRepo.loadClockDateConfig()
        assertFalse(savedClock.is24HourFormat)
        assertFalse(savedClock.showDate)
        assertEquals(32f, savedClock.clockSizeSp, 0.01f)
    }
}
