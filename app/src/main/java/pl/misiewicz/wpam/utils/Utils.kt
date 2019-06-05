package pl.misiewicz.wpam.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.TypedValue
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStream


fun saveInLocalCache(context: Context, bitmap: Bitmap, filename: String): Uri? {
    val cacheLocation = context.externalCacheDir
    val outputFileUri = Uri.fromFile(File(cacheLocation!!.path, filename))
    writeBitmapToUri(context, bitmap, outputFileUri!!, Bitmap.CompressFormat.JPEG, 90)
    return outputFileUri
}


@Throws(FileNotFoundException::class)
fun writeBitmapToUri(
    context: Context,
    bitmap: Bitmap,
    uri: Uri,
    compressFormat: Bitmap.CompressFormat,
    compressQuality: Int
) {
    var outputStream: OutputStream? = null
    try {
        outputStream = context.contentResolver.openOutputStream(uri)
        bitmap.compress(compressFormat, compressQuality, outputStream)
    } finally {
        if (outputStream != null) {
            try {
                outputStream.close()
            } catch (ignored: IOException) { }
        }
    }
}

fun dpToPx(context: Context, dp: Int): Int {
    context.resources
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        context.resources.displayMetrics
    ).toInt()
}