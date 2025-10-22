package com.example.cropdoctor.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toColorLong
import com.example.cropdoctor.domain.DiagnosisResult
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt
import com.example.cropdoctor.ui.theme.DarkGreen

fun createPdf(context: Context, result: DiagnosisResult, bitmap: Bitmap): File {
    val pdfDocument = PdfDocument()
    var currentPageNumber = 1
    var page: PdfDocument.Page? = null
    var canvas: android.graphics.Canvas? = null
    val textPaint = TextPaint()
    val linePaint = Paint().apply {
        color = "#4CAF50".toColorInt() // Green theme for agriculture
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    val fillPaint = Paint().apply {
        color = "#E8F5E8".toColorInt() // Light green background
    }
    val pageMargin = 40f
    val contentWidth = 595 - (2 * pageMargin).toInt() // A4 width: 595pt
    val sectionPadding = 15f
    val pageHeight = 842f // A4 height: 842pt

    fun startNewPage() {
        if (page != null) {
            pdfDocument.finishPage(page!!)
        }
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNumber).create()
        page = pdfDocument.startPage(pageInfo)
        canvas = page!!.canvas
        currentPageNumber++
    }

    fun getCurrentY(): Float = pageMargin // Reset y to top margin on new page
    var y = getCurrentY()
    fun getCurrentCanvas(): android.graphics.Canvas = canvas!!

    fun shouldStartNewPage(requiredHeight: Float): Boolean {
        return y + requiredHeight > pageHeight - (pageMargin + 50f) // Reserve space for footer
    }

    fun drawSectionOnPage(drawAction: (Float) -> Float): Float {
        var currentY = y
        if (shouldStartNewPage(100f)) { // Minimum section height check
            startNewPage()
            currentY = getCurrentY()
        }
        y = drawAction(currentY)
        return y
    }

    // --- Header (Always on first page) ---
    startNewPage()
    y = pageMargin
    val headerRect = RectF(pageMargin, y - 10f, 595f - pageMargin, y + 60f)
    getCurrentCanvas().drawRoundRect(headerRect, 10f, 10f, fillPaint)
    textPaint.textSize = 24f
    textPaint.color = DarkGreen.toColorLong().toColorInt()
    textPaint.isFakeBoldText = true
    textPaint.textAlign = Paint.Align.CENTER
    getCurrentCanvas().drawText("Diagnosis Report - CropDoctor", (595f / 2), y + 25f, textPaint)
    y += 70f
    textPaint.textAlign = Paint.Align.LEFT
    textPaint.isFakeBoldText = false
    textPaint.color = Color.BLACK
    textPaint.textSize = 12f

    // --- Plant Image Section ---
    y = drawSectionOnPage { startY ->
        var currentY = startY
        if (shouldStartNewPage(250f)) {
            startNewPage()
            currentY = getCurrentY()
        }

        val imageSectionRect = RectF(pageMargin, currentY - 10f, 595f - pageMargin, currentY + 250f)
        getCurrentCanvas().drawRoundRect(imageSectionRect, 8f, 8f, fillPaint)
        textPaint.isFakeBoldText = true
        textPaint.textSize = 14f
        textPaint.color = "#2E7D32".toColorInt()
        getCurrentCanvas().drawText("Captured Plant Image", pageMargin + sectionPadding, currentY + 15f, textPaint)
        currentY += 30f
        textPaint.isFakeBoldText = false
        textPaint.textSize = 10f
        textPaint.color = Color.GRAY
        getCurrentCanvas().drawText("Visual analysis of the affected area", pageMargin + sectionPadding, currentY, textPaint)
        currentY += 25f
        textPaint.color = Color.BLACK
        textPaint.textSize = 12f

        try {
            val originalBitmap = bitmap
            val desiredWidth = 300
            val desiredHeight = 300
            val scaledBitmap = originalBitmap.scale(desiredWidth, desiredHeight, false)

            // Center the image
            val imageX = (595f - scaledBitmap.width) / 2f
            getCurrentCanvas().drawBitmap(scaledBitmap, imageX, currentY, null)
            currentY += scaledBitmap.height + 20f
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: Draw a placeholder rectangle
            val placeholderRect = RectF(pageMargin + 50f, currentY, pageMargin + 250f, currentY + 200f)
            getCurrentCanvas().drawRoundRect(placeholderRect, 8f, 8f, fillPaint)
            getCurrentCanvas().drawText("Image not available", pageMargin + 60f, currentY + 100f, textPaint)
            currentY += 220f
        }

        currentY + 10f
    }

    // --- Key Details Card ---
    y = drawSectionOnPage { startY ->
        var currentY = startY
        val detailsRect = RectF(pageMargin, currentY - 10f, 595f - pageMargin, currentY + 120f)
        getCurrentCanvas().drawRoundRect(detailsRect, 8f, 8f, fillPaint)
        textPaint.isFakeBoldText = true
        textPaint.textSize = 14f
        textPaint.color = "#2E7D32".toColorInt()
        getCurrentCanvas().drawText("Quick Diagnosis Summary", pageMargin + sectionPadding, currentY + 15f, textPaint)
        currentY += 30f
        textPaint.isFakeBoldText = false
        textPaint.textSize = 12f
        textPaint.color = Color.BLACK

        // Draw details in a grid-like fashion
        val detailY = currentY
        val detailX1 = pageMargin + sectionPadding
        val detailX2 = (595f / 2)
        getCurrentCanvas().drawText("Plant: ${result.plantName}", detailX1, detailY, textPaint)
        getCurrentCanvas().drawText("Disease: ${result.disease}", detailX2, detailY, textPaint)
        currentY += 20f
        getCurrentCanvas().drawText("Confidence: ${String.format("%.1f", result.confidence * 100)}%", detailX1, currentY, textPaint)
        // Add a confidence bar for innovation
        val barWidth = (result.confidence * 200f).toFloat()
        val barRect = RectF(detailX2 + 10f, currentY - 15f, detailX2 + 10f + barWidth, currentY + 5f)
        getCurrentCanvas().drawRoundRect(barRect, 2f, 2f, Paint().apply { color =
            "#4CAF50".toColorInt() })
        currentY + 40f
    }

    // --- Helper for drawing wrapped text in a card (dynamic height) ---
    fun drawWrappedTextCard(title: String, content: String) {
        // Pre-calculate height
        textPaint.isFakeBoldText = false
        textPaint.textSize = 11f
        textPaint.color = Color.BLACK
        val tempLayout = StaticLayout.Builder.obtain(content, 0, content.length, textPaint, contentWidth - 40)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(4f, 1.2f)
            .setIncludePad(false)
            .build()
        val textHeight = tempLayout.height.toFloat()

        val headerHeight = 50f
        val totalHeight = headerHeight + textHeight + 20f
        y = drawSectionOnPage { startY ->
            var currentY = startY
            if (shouldStartNewPage(totalHeight)) {
                startNewPage()
                currentY = getCurrentY()
            }

            val cardRect = RectF(pageMargin, currentY - 10f, 595f - pageMargin, currentY + totalHeight)
            getCurrentCanvas().drawRoundRect(cardRect, 8f, 8f, fillPaint)
            textPaint.isFakeBoldText = true
            textPaint.textSize = 14f
            textPaint.color = "#2E7D32".toColorInt()
            getCurrentCanvas().drawText(title, pageMargin + sectionPadding, currentY + 15f, textPaint)
            currentY += 30f
            textPaint.isFakeBoldText = false
            textPaint.textSize = 11f
            textPaint.color = Color.BLACK

            val textLayout = StaticLayout.Builder.obtain(content, 0, content.length, textPaint, contentWidth - 40)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(4f, 1.2f)
                .setIncludePad(false)
                .build()
            getCurrentCanvas().save()
            getCurrentCanvas().translate(pageMargin + sectionPadding, currentY)
            textLayout.draw(getCurrentCanvas())
            getCurrentCanvas().restore()
            currentY + textLayout.height + 20f
        }
    }

    // --- Helper for drawing list in a card (dynamic height) ---
    fun drawListCard(title: String, items: List<String>) {
        // Pre-calculate height
        textPaint.isFakeBoldText = false
        textPaint.textSize = 11f
        textPaint.color = Color.BLACK
        val itemLayouts = items.map { item ->
            val itemText = "• $item"
            StaticLayout.Builder.obtain(itemText, 0, itemText.length, textPaint, contentWidth - 40)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(4f, 1.2f)
                .setIncludePad(false)
                .build()
        }
        val itemsHeight = itemLayouts.sumOf { it.height.toDouble() }.toFloat() + (items.size * 8f)
        val headerHeight = 50f
        val totalHeight = headerHeight + itemsHeight + 15f

        y = drawSectionOnPage { startY ->
            var currentY = startY
            if (shouldStartNewPage(totalHeight)) {
                startNewPage()
                currentY = getCurrentY()
            }

            val cardRect = RectF(pageMargin, currentY - 10f, 595f - pageMargin, currentY + totalHeight)
            getCurrentCanvas().drawRoundRect(cardRect, 8f, 8f, fillPaint)
            textPaint.isFakeBoldText = true
            textPaint.textSize = 14f
            textPaint.color = "#2E7D32".toColorInt()
            getCurrentCanvas().drawText(title, pageMargin + sectionPadding, currentY + 15f, textPaint)
            currentY += 30f
            textPaint.isFakeBoldText = false
            textPaint.textSize = 11f
            textPaint.color = Color.BLACK

            itemLayouts.forEachIndexed { index, itemLayout ->
                getCurrentCanvas().save()
                getCurrentCanvas().translate(pageMargin + sectionPadding, currentY)
                itemLayout.draw(getCurrentCanvas())
                getCurrentCanvas().restore()
                currentY += itemLayout.height + 8f
            }
            currentY + 15f
        }
    }

    // Draw sections with cards
    drawWrappedTextCard("Observed Symptoms", result.description)
    drawListCard("Recommended Treatment", result.treatment)
    drawListCard("Prevention Tips", result.prevention)

    // --- Footer (on every page) ---
    fun drawFooter() {
        val footerY = pageHeight - 50f
        textPaint.textSize = 10f
        textPaint.color = Color.GRAY
        textPaint.textAlign = android.graphics.Paint.Align.CENTER
        getCurrentCanvas().drawText("Generated by CropDoctor App | For agricultural guidance only | Page ${currentPageNumber - 1}", (595f / 2), footerY, textPaint)
        // Draw a bottom line
        getCurrentCanvas().drawLine(pageMargin, footerY + 5f, 595f - pageMargin, footerY + 5f, linePaint)
    }
    drawFooter()

    pdfDocument.finishPage(page!!)

    val file = File(context.cacheDir, "diagnosis_report.pdf")
    try {
        pdfDocument.writeTo(FileOutputStream(file))
    } catch (e: Exception) {
        e.printStackTrace()
    }
    pdfDocument.close()
    return file
}