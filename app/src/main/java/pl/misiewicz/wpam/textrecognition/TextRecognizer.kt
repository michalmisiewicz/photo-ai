package pl.misiewicz.wpam.textrecognition

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText

class TextRecognizer {

    private val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer

    fun analyze(image: Bitmap): Task<FirebaseVisionText> {
        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        return textRecognizer.processImage(firebaseVisionImage)
    }

    fun getText(result: FirebaseVisionText): String {
        return result.text.replace("\n", " ")
    }

    fun drawTextBoundaryOnBitmap(result: FirebaseVisionText, image: Bitmap?) {
        val canvas = Canvas(image)
        val rectPaint = Paint()
        rectPaint.color = Color.RED
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 4F
        val textPaint = Paint()
        textPaint.color = Color.RED
        textPaint.textSize = 120F

        var index = 0

        val textBlocks = ArrayList(result.textBlocks)
        textBlocks.sortWith(compareBy ({ it.cornerPoints!![0].y }, { it.cornerPoints!![0].x }))

        for (block in textBlocks) {
            for (line in block.lines) {

                canvas.drawRect(line.boundingBox, rectPaint)
                canvas.drawText(index.toString(), line.cornerPoints!![2].x.toFloat(), line.cornerPoints!![2].y.toFloat(), textPaint)
                index++
            }
        }
    }
}