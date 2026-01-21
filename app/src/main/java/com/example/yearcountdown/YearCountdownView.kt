package com.example.yearcountdown

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.*

class YearCountdownView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class Align { START, CENTER, END }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var backgroundBitmap: Bitmap? = null
    private val bgPaint = Paint()

    var dotColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    var horizontalAlign = Align.CENTER
        set(value) {
            field = value
            invalidate()
        }

    var verticalAlign = Align.CENTER
        set(value) {
            field = value
            invalidate()
        }

    private val gridSize = 19 // 19x19 = 361, close to 365
    private val totalDots = 365

    init {
        paint.style = Paint.Style.FILL
        textPaint.color = Color.WHITE
        textPaint.textSize = 24f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        progressPaint.style = Paint.Style.FILL
    }

    fun setBackgroundBitmap(bitmap: Bitmap?) {
        backgroundBitmap = bitmap
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background
        backgroundBitmap?.let {
            val scaledBitmap = Bitmap.createScaledBitmap(it, width, height, true)
            canvas.drawBitmap(scaledBitmap, 0f, 0f, bgPaint)
        } ?: run {
            canvas.drawColor(0xFF1a1a2e.toInt())
        }

        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val isLeapYear = calendar.getActualMaximum(Calendar.DAY_OF_YEAR) == 366
        val totalDaysInYear = if (isLeapYear) 366 else 365
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)

        // Draw dots matrix and get its bounds
        val matrixBounds = drawDotsMatrix(canvas, dayOfYear)

        // Draw progress bars on sides
        drawSideProgressBars(canvas, matrixBounds, dayOfYear, totalDaysInYear, currentWeek)
    }

    private fun drawDotsMatrix(canvas: Canvas, dayOfYear: Int): RectF {
        val dotRadius = 8f
        val spacing = 30f
        val matrixWidth = gridSize * spacing
        val matrixHeight = gridSize * spacing

        val startX = when (horizontalAlign) {
            Align.START -> 100f
            Align.CENTER -> (width - matrixWidth) / 2f
            Align.END -> width - matrixWidth - 100f
        }

        val startY = when (verticalAlign) {
            Align.START -> 200f
            Align.CENTER -> (height - matrixHeight) / 2f + 50f
            Align.END -> height - matrixHeight - 300f
        }

        var dotIndex = 0
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (dotIndex >= totalDots) break

                dotIndex++
                val x = startX + col * spacing
                val y = startY + row * spacing

                paint.color = if (dotIndex <= dayOfYear) {
                    dotColor
                } else {
                    Color.argb(100, 255, 255, 255)
                }

                canvas.drawCircle(x, y, dotRadius, paint)
            }
            if (dotIndex >= totalDots) break
        }

        return RectF(startX, startY, startX + matrixWidth, startY + matrixHeight)
    }

    private fun drawSideProgressBars(
        canvas: Canvas,
        matrixBounds: RectF,
        dayOfYear: Int,
        totalDays: Int,
        currentWeek: Int
    ) {
        val barThickness = 40f
        val barLength = matrixBounds.height()
        val calendar = Calendar.getInstance()

        // --- Left side: 24-Hour Progress ---
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val dayProgress = (hour * 60f + minute) / (24f * 60f)

        val leftX = matrixBounds.left - 80f
        drawVerticalProgressBar(
            canvas, leftX, matrixBounds.top, barThickness, barLength,
            dayProgress, "DAY (24h)", "$hour: ${String.format("%02d", minute)}"
        )

        // --- Right side: 7-Day Week Progress ---
        // Note: Calendar.DAY_OF_WEEK starts at 1 (Sunday)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val weekProgress = dayOfWeek.toFloat() / 7f
        val dayName = when(dayOfWeek) {
            Calendar.SUNDAY -> "SUN"
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            else -> "SAT"
        }

        val rightX = matrixBounds.right + 40f
        drawVerticalProgressBar(
            canvas, rightX, matrixBounds.top, barThickness, barLength,
            weekProgress, "WEEK (7d)", dayName
        )
    }

    private fun drawVerticalProgressBar(
        canvas: Canvas,
        x: Float,
        y: Float,
        thickness: Float,
        length: Float,
        progress: Float,
        label: String,
        value: String
    ) {
        // Background
        progressPaint.color = Color.argb(100, 255, 255, 255)
        canvas.drawRoundRect(x, y, x + thickness, y + length, 20f, 20f, progressPaint)

        // Progress (fill from bottom to top)
        progressPaint.color = dotColor
        val progressHeight = length * progress
        canvas.drawRoundRect(
            x,
            y + length - progressHeight,
            x + thickness,
            y + length,
            20f,
            20f,
            progressPaint
        )

        // Label on top
        textPaint.textSize = 18f
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        canvas.save()
        canvas.rotate(-90f, x + thickness / 2, y - 20f)
        canvas.drawText(label, x + thickness / 2, y - 20f, textPaint)
        canvas.restore()

        // Value on bottom
        textPaint.textSize = 16f
        canvas.save()
        canvas.rotate(-90f, x + thickness / 2, y + length + 40f)
        canvas.drawText(value, x + thickness / 2, y + length + 40f, textPaint)
        canvas.restore()
    }
}
