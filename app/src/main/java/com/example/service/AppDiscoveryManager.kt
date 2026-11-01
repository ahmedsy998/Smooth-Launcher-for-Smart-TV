package com.example.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Environment
import android.provider.Settings
import com.example.model.AppCategory
import com.example.model.AppItem
import com.example.model.LocalFileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class AppDiscoveryManager(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val iconCache = ConcurrentHashMap<String, Drawable>()

    private val priorityStreamingKeywords = listOf(
        "youtube", "netflix", "amazon.avod", "primevideo", "disney",
        "spotify", "shahid", "watchit", "osn", "plex", "kodi", "vlc", "stremio", "twitch"
    )

    suspend fun discoverInstalledApps(
        favoritePackages: Set<String>,
        pinnedHomePackages: List<String>,
        customCategories: Map<String, AppCategory>,
        maxHomeRowCount: Int
    ): List<AppItem> = withContext(Dispatchers.IO) {
        val discoveredMap = LinkedHashMap<String, ResolveInfo>()
        val tvPackages = mutableSetOf<String>()

        // 1. Query Android TV Leanback Launcher activities first
        val leanbackIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val leanbackList = runCatching {
            packageManager.queryIntentActivities(leanbackIntent, 0)
        }.getOrDefault(emptyList())

        for (info in leanbackList) {
            val pkg = info.activityInfo?.packageName ?: continue
            if (pkg == context.packageName) continue
            discoveredMap[pkg] = info
            tvPackages.add(pkg)
        }

        // 2. Query standard Launcher activities
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val mainList = runCatching {
            packageManager.queryIntentActivities(mainIntent, 0)
        }.getOrDefault(emptyList())

        for (info in mainList) {
            val pkg = info.activityInfo?.packageName ?: continue
            if (pkg == context.packageName) continue
            if (!discoveredMap.containsKey(pkg)) {
                discoveredMap[pkg] = info
            }
        }

        val rawItems = discoveredMap.mapNotNull { (pkg, resolveInfo) ->
            runCatching {
                val activityInfo = resolveInfo.activityInfo
                val label = resolveInfo.loadLabel(packageManager)?.toString()?.trim()
                    .takeUnless { it.isNullOrEmpty() } ?: pkg
                val icon = iconCache.getOrPut(pkg) {
                    resolveInfo.loadIcon(packageManager)
                        ?: activityInfo.loadIcon(packageManager)
                }
                val banner = runCatching {
                    activityInfo.loadBanner(packageManager)
                        ?: packageManager.getApplicationBanner(pkg)
                }.getOrNull()

                val defaultCat = classifyAppCategory(pkg, label)
                val category = customCategories[pkg] ?: defaultCat

                AppItem(
                    packageName = pkg,
                    activityName = activityInfo.name ?: "",
                    label = label,
                    icon = icon,
                    banner = banner,
                    isTvOptimized = tvPackages.contains(pkg),
                    category = category,
                    isFavorite = favoritePackages.contains(pkg),
                    isPinnedToHome = false,
                    homeOrder = Int.MAX_VALUE,
                    brandAccentColorHex = detectBrandTintHex(pkg, label)
                )
            }.getOrNull()
        }

        // Sort alphabetically with priority streaming apps boosted for clean initial discovery
        val sorted = rawItems.sortedWith(
            compareBy<AppItem> { priorityRank(it.packageName, it.label) }
                .thenBy { it.label.lowercase() }
        )

        // Determine which apps are pinned to the Home Application Row
        val validPinned = pinnedHomePackages.filter { pkg -> sorted.any { it.packageName == pkg } }
        val effectivePinned = if (validPinned.isNotEmpty()) {
            validPinned
        } else {
            // Auto-populate row with installed device apps (streaming/media first, then general)
            sorted.take(maxHomeRowCount).map { it.packageName }
        }

        sorted.map { item ->
            val pinIndex = effectivePinned.indexOf(item.packageName)
            if (pinIndex >= 0) {
                item.copy(isPinnedToHome = true, homeOrder = pinIndex)
            } else {
                item
            }
        }
    }

    private fun priorityRank(pkg: String, label: String): Int {
        val combined = "$pkg $label".lowercase()
        val idx = priorityStreamingKeywords.indexOfFirst { combined.contains(it) }
        return if (idx >= 0) idx else 100
    }

    private fun classifyAppCategory(pkg: String, label: String): AppCategory {
        val text = "$pkg $label".lowercase()
        return when {
            text.contains("youtube") || text.contains("netflix") || text.contains("cinema") ||
                text.contains("movie") || text.contains("vlc") || text.contains("kodi") ||
                text.contains("plex") || text.contains("video") || text.contains("player") -> AppCategory.STREAMING_MOVIES

            text.contains("series") || text.contains("tv") || text.contains("prime") ||
                text.contains("disney") || text.contains("shahid") || text.contains("osn") ||
                text.contains("watch") || text.contains("live") || text.contains("iptv") -> AppCategory.STREAMING_SERIES

            text.contains("spotify") || text.contains("music") || text.contains("audio") ||
                text.contains("sound") || text.contains("anghami") || text.contains("radio") -> AppCategory.MUSIC_AUDIO

            text.contains("settings") || text.contains("file") || text.contains("store") ||
                text.contains("vending") || text.contains("browser") || text.contains("chrome") ||
                text.contains("system") -> AppCategory.SYSTEM_TOOLS

            else -> AppCategory.GENERAL
        }
    }

    private fun detectBrandTintHex(pkg: String, label: String): Long? {
        val text = "$pkg $label".lowercase()
        return when {
            text.contains("youtube") -> 0xFFE50914
            text.contains("netflix") -> 0xFF141414
            text.contains("prime") || text.contains("avod") -> 0xFF0079FF
            text.contains("disney") -> 0xFF0B1C58
            text.contains("spotify") || text.contains("music") -> 0xFF063A24
            else -> null
        }
    }

    fun launchApplication(packageName: String, activityName: String = ""): Boolean {
        return try {
            val leanbackIntent = packageManager.getLeanbackLaunchIntentForPackage(packageName)
            val standardIntent = packageManager.getLaunchIntentForPackage(packageName)
            val explicitIntent = if (activityName.isNotEmpty()) {
                Intent(Intent.ACTION_MAIN).apply {
                    component = ComponentName(packageName, activityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                }
            } else null

            val chosenIntent = (leanbackIntent ?: standardIntent ?: explicitIntent)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            } ?: return false

            context.startActivity(chosenIntent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun launchSystemSettingAction(action: String): Boolean {
        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    fun openHomeLauncherChooser(): Boolean {
        return try {
            val homeSettingsIntent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (homeSettingsIntent.resolveActivity(packageManager) != null) {
                context.startActivity(homeSettingsIntent)
                true
            } else {
                launchSystemSettingAction(Settings.ACTION_SETTINGS)
            }
        } catch (_: Exception) {
            launchSystemSettingAction(Settings.ACTION_SETTINGS)
        }
    }

    suspend fun listLocalStorageFiles(directoryPath: String? = null): Pair<String, List<LocalFileItem>> =
        withContext(Dispatchers.IO) {
            val rootFile = try {
                if (!directoryPath.isNullOrBlank()) {
                    val requested = File(directoryPath)
                    if (requested.exists() && requested.canRead()) requested
                    else Environment.getExternalStorageDirectory()
                } else {
                    val ext = Environment.getExternalStorageDirectory()
                    if (ext != null && ext.exists() && ext.canRead()) ext else context.filesDir
                }
            } catch (_: Exception) {
                context.filesDir
            }

            val children = runCatching {
                rootFile.listFiles()?.map { f ->
                    val ext = f.extension.lowercase()
                    val typeLabel = when {
                        f.isDirectory -> "مجلد"
                        ext in listOf("mp4", "mkv", "avi", "mov", "webm") -> "ملف فيديو"
                        ext in listOf("mp3", "wav", "flac", "aac", "m4a") -> "ملف صوتي"
                        ext in listOf("jpg", "jpeg", "png", "webp", "bmp") -> "صورة"
                        ext == "apk" -> "حزمة تطبيق أندرويد"
                        else -> "ملف (${ext.ifEmpty { "عام" }})"
                    }
                    LocalFileItem(
                        name = f.name,
                        path = f.absolutePath,
                        isDirectory = f.isDirectory,
                        sizeBytes = if (f.isDirectory) 0L else f.length(),
                        lastModified = f.lastModified(),
                        fileTypeLabelAr = typeLabel
                    )
                }?.sortedWith(compareByDescending<LocalFileItem> { it.isDirectory }.thenBy { it.name.lowercase() })
                    ?: emptyList()
            }.getOrDefault(emptyList())

            rootFile.absolutePath to children
        }

    fun invalidateCache() {
        iconCache.clear()
    }
}
