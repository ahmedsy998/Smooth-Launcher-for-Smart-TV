package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WallpaperLoader(private val context: Context) {

    private var cachedUri: String? = null
    private var cachedBitmap: ImageBitmap? = null

    suspend fun loadOptimizedWallpaper(
        uriString: String,
        maxWidth: Int = 1920,
        maxHeight: Int = 1080
    ): ImageBitmap? = withContext(Dispatchers.IO) {
        if (uriString.isBlank()) return@withContext null
        if (uriString == cachedUri && cachedBitmap != null) {
            return@withContext cachedBitmap
        }

        try {
            val uri = Uri.parse(uriString)
            // Persist read permission when possible
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            // 1. Decode bounds only
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return@withContext null
            }

            // 2. Calculate optimal inSampleSize to avoid full-resolution OOM or lag
            var sampleSize = 1
            while (
                (boundsOptions.outWidth / sampleSize) > maxWidth * 1.5 ||
                (boundsOptions.outHeight / sampleSize) > maxHeight * 1.5
            ) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return@withContext null

            val imageBitmap = decoded.asImageBitmap()
            cachedUri = uriString
            cachedBitmap = imageBitmap
            imageBitmap
        } catch (_: Exception) {
            null
        }
    }

    fun clearCache() {
        cachedUri = null
        cachedBitmap = null
    }
}
