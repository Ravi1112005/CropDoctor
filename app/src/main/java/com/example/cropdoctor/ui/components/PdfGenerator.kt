package com.example.cropdoctor.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.cropdoctor.domain.DiagnosisResult
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.scale

fun createPdf(context: Context, result: DiagnosisResult, bitmap: Bitmap): File {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val textPaint = TextPaint()
    val pageMargin = 40f
    val contentWidth = pageInfo.pageWidth - (2 * pageMargin).toInt()

    var y = pageMargin

    // --- Title ---
    textPaint.textSize = 18f
    textPaint.isFakeBoldText = true
    textPaint.textAlign = android.graphics.Paint.Align.CENTER
    canvas.drawText("Diagnosis Report - CropDoctor", (pageInfo.pageWidth / 2).toFloat(), y, textPaint)
    y += 40f
    textPaint.textAlign = android.graphics.Paint.Align.LEFT
    textPaint.isFakeBoldText = false
    textPaint.textSize = 12f

    // --- Image ---
    try {
        textPaint.isFakeBoldText = true
        canvas.drawText("Image of Plant", pageMargin, y, textPaint)
        y += 20f
        textPaint.isFakeBoldText = false

        // --- CHANGE IS HERE: Use the passed 'bitmap' directly ---
        val originalBitmap = bitmap
        val desiredWidth = 200
        val desiredHieght = 200
        val scaledBitmap =
            originalBitmap.scale(desiredWidth, desiredHieght, false)

        canvas.drawBitmap(scaledBitmap, pageMargin, y, null)
        y += scaledBitmap.height + 20f
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // --- Details ---
    textPaint.isFakeBoldText = false
    canvas.drawText("Plant: ${result.plantName}", pageMargin, y, textPaint)
    y += 20f
    canvas.drawText("Disease: ${result.disease}", pageMargin, y, textPaint)
    y += 20f
    canvas.drawText("Confidence: ${String.format("%.1f", result.confidence * 100)}%", pageMargin, y, textPaint)
    y += 40f

    // --- Helper for drawing wrapped text ---
    fun drawWrappedText(title: String, content: String) {
        textPaint.isFakeBoldText = true
        canvas.drawText(title, pageMargin, y, textPaint)
        y += 20f
        textPaint.isFakeBoldText = false

        val textLayout = StaticLayout.Builder.obtain(content, 0, content.length, textPaint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
        canvas.save()
        canvas.translate(pageMargin, y)
        textLayout.draw(canvas)
        canvas.restore()
        y += textLayout.height + 20f
    }

    // --- Helper for drawing list ---
    fun drawList(title: String, items: List<String>) {
        textPaint.isFakeBoldText = true
        canvas.drawText(title, pageMargin, y, textPaint)
        y += 20f
        textPaint.isFakeBoldText = false
        items.forEach {
            val itemText = "• $it"
            val itemLayout = StaticLayout.Builder.obtain(itemText, 0, itemText.length, textPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1f)
                .setIncludePad(false)
                .build()

            canvas.save()
            canvas.translate(pageMargin, y)
            itemLayout.draw(canvas)
            canvas.restore()
            y += itemLayout.height + 5f
        }
        y += 15f
    }


    drawWrappedText("Symptoms", result.description)
    drawList("Treatment", result.treatment)
    drawList("Prevention", result.prevention)

    pdfDocument.finishPage(page)

    val file = File(context.cacheDir, "diagnosis_report.pdf")
    try {
        pdfDocument.writeTo(FileOutputStream(file))
    } catch (e: Exception) {
        e.printStackTrace()
    }
    pdfDocument.close()
    return file
}
