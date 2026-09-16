package com.example.theme

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object DynamicColorExtractor {

    private val DEFAULT_CAR_ACCENT = Color(0xFF4DDFBD) // Electric Mint Cyan

    suspend fun extractVibrantColor(imagePath: String?): Color = withContext(Dispatchers.IO) {
        if (imagePath.isNullOrEmpty()) return@withContext DEFAULT_CAR_ACCENT

        val file = File(imagePath)
        if (!file.exists() || !file.canRead() || file.length() == 0L) {
            return@withContext DEFAULT_CAR_ACCENT
        }

        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return@withContext DEFAULT_CAR_ACCENT
            }

            // Downsample aggressively to 48x48 max for lightning speed
            val sampleSize = Math.max(1, Math.max(options.outWidth / 48, options.outHeight / 48))
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                ?: return@withContext DEFAULT_CAR_ACCENT

            var bestColor = DEFAULT_CAR_ACCENT
            var highestScore = -1f

            val width = bitmap.width
            val height = bitmap.height
            val step = Math.max(1, width / 24)

            val hsv = FloatArray(3)

            for (x in 0 until width step step) {
                for (y in 0 until height step step) {
                    val pixel = bitmap.getPixel(x, y)
                    AndroidColor.colorToHSV(pixel, hsv)
                    val saturation = hsv[1]
                    val value = hsv[2]

                    // Filter out pure black, pure white, and dull greys to maintain dashboard visibility
                    if (saturation > 0.35f && value in 0.30f..0.92f) {
                        val score = saturation * 2.0f + value
                        if (score > highestScore) {
                            highestScore = score
                            // Slightly boost brightness if too dark for night mode
                            val adjustedValue = value.coerceIn(0.65f, 0.95f)
                            hsv[2] = adjustedValue
                            val vibrantPixel = AndroidColor.HSVToColor(hsv)
                            bestColor = Color(vibrantPixel)
                        }
                    }
                }
            }

            bitmap.recycle()
            return@withContext bestColor
        } catch (e: Exception) {
            return@withContext DEFAULT_CAR_ACCENT
        }
    }
}
