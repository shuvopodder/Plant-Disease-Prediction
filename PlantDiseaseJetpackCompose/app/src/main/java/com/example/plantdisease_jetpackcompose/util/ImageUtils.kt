package com.example.plantdisease_jetpackcompose.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object ImageUtils {

    /**
     * Creates a URI for capturing a new image from the camera
     * @param context Application or Activity context
     * @return Uri where the camera image will be saved
     * @throws IllegalStateException if URI creation fails
     */
    fun createImageUri(context: Context): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: throw IllegalStateException("Failed to create image URI")
    }

    /**
     * Decodes a bitmap from URI with efficient memory usage
     * Uses subsampling to avoid OutOfMemoryError for large images
     *
     * @param context Application or Activity context
     * @param uri URI of the image to decode
     * @param reqWidth Required width of the output bitmap
     * @param reqHeight Required height of the output bitmap
     * @return Scaled bitmap with exact dimensions
     * @throws IOException if image cannot be read
     */
    suspend fun decodeSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap = withContext(Dispatchers.IO) {
        // First pass: Decode with inJustDecodeBounds=true to get dimensions
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }

        // Calculate inSampleSize for memory-efficient loading
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        // Second pass: Decode the actual bitmap with subsampling
        val sampledBitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        } ?: throw IOException("Failed to decode bitmap from URI: $uri")

        // Scale to exact dimensions
        Bitmap.createScaledBitmap(sampledBitmap, reqWidth, reqHeight, true).also {
            // Recycle the sampled bitmap if it's different from scaled bitmap
            if (it != sampledBitmap) {
                sampledBitmap.recycle()
            }
        }
    }

    /**
     * Calculates the optimal inSampleSize for loading a bitmap
     * This reduces memory usage while maintaining quality
     *
     * @param options BitmapFactory.Options with outWidth and outHeight set
     * @param reqWidth Required width
     * @param reqHeight Required height
     * @return inSampleSize value (power of 2)
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2
            // and keeps both height and width larger than requested
            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Rotates a bitmap by the specified degrees
     * Useful for correcting image orientation from camera
     *
     * @param bitmap Source bitmap
     * @param degrees Rotation angle (0, 90, 180, 270)
     * @return Rotated bitmap
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap

        val matrix = android.graphics.Matrix().apply {
            postRotate(degrees)
        }

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        ).also {
            if (it != bitmap) {
                bitmap.recycle()
            }
        }
    }

    /**
     * Gets the orientation of an image from its URI
     * Useful for auto-rotating images taken from camera
     *
     * @param context Application or Activity context
     * @param uri URI of the image
     * @return Rotation degrees (0, 90, 180, or 270)
     */
    fun getImageOrientation(context: Context, uri: Uri): Int {
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(MediaStore.Images.Media.ORIENTATION),
            null,
            null,
            null
        )

        var orientation = 0
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.Images.Media.ORIENTATION)
                if (columnIndex != -1) {
                    orientation = it.getInt(columnIndex)
                }
            }
        }

        return orientation
    }

    /**
     * Compresses a bitmap to JPEG format with quality control
     *
     * @param bitmap Source bitmap
     * @param quality Compression quality (0-100)
     * @return ByteArray of compressed image
     */
    fun compressBitmap(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    /**
     * Checks if the bitmap dimensions are valid
     *
     * @param bitmap Bitmap to validate
     * @param maxWidth Maximum allowed width
     * @param maxHeight Maximum allowed height
     * @return true if bitmap is within size limits
     */
    fun isValidBitmapSize(
        bitmap: Bitmap,
        maxWidth: Int = 4096,
        maxHeight: Int = 4096
    ): Boolean {
        return bitmap.width <= maxWidth && bitmap.height <= maxHeight
    }

    /**
     * Crops a bitmap to a square from the center
     * Useful for ML models that require square inputs
     *
     * @param bitmap Source bitmap
     * @return Square cropped bitmap
     */
    fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2

        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }
}

// ============================================================================
// Extension Functions for Convenience
// ============================================================================

/**
 * Extension function to easily decode URI to Bitmap
 */
suspend fun Uri.toBitmap(
    context: Context,
    width: Int = 224,
    height: Int = 224
): Bitmap {
    return ImageUtils.decodeSampledBitmapFromUri(context, this, width, height)
}

/**
 * Extension function to rotate bitmap
 */
fun Bitmap.rotate(degrees: Float): Bitmap {
    return ImageUtils.rotateBitmap(this, degrees)
}

/**
 * Extension function to crop bitmap to square
 */
fun Bitmap.toSquare(): Bitmap {
    return ImageUtils.cropToSquare(this)
}

/**
 * Extension function to compress bitmap
 */
fun Bitmap.toByteArray(quality: Int = 85): ByteArray {
    return ImageUtils.compressBitmap(this, quality)
}