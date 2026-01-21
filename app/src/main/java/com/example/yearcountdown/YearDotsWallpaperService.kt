package com.example.yearcountdown

import android.content.SharedPreferences
import android.graphics.*
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.util.*

class YearDotsWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return YearDotsEngine()
    }

    inner class YearDotsEngine : Engine() {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val bgPaint = Paint()

        private var prefs: SharedPreferences? = null
        private var dotColor = Color.WHITE
        private var horizontalAlign = 1 // 0=left, 1=center, 2=right
        private var verticalAlign = 1 // 0=top, 1=center, 2=bottom

        private val gridSize = 19
        private val totalDots = 365

        private val handler = android.os.Handler()
        private val drawRunner = Runnable { draw() }

        init {
            paint.style = Paint.Style.FILL
            textPaint.color = Color.WHITE
            textPaint.textSize = 24f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            progressPaint.style = Paint.Style.FILL
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            prefs = getSharedPreferences("wallpaper_prefs", MODE_PRIVATE)
            loadPreferences()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                loadPreferences()
                draw()
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            draw()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunner)
        }

        private fun loadPreferences() {
            prefs?.let {
                dotColor = it.getInt("dot_color", Color.WHITE)
                horizontalAlign = it.getInt("horizontal_align", 1)
                verticalAlign = it.getInt("vertical_align", 1)
            }
        }

        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null

            try {
                canvas = holder.lockCanvas()
                canvas?.let {
                    drawWallpaper(it)
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            handler.removeCallbacks(drawRunner)
            // Redraw every hour to update progress
            handler.postDelayed(drawRunner, 3600000)
        }

        private fun drawWallpaper(canvas: Canvas) {
            val width = canvas.width
            val height = canvas.height

            // Background
            canvas.drawColor(0xFF1a1a2e.toInt())

            val calendar = Calendar.getInstance()
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val totalDaysInYear = calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
            val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
            val year = calendar.get(Calendar.YEAR)

            // Draw year text
            textPaint.textSize = 48f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(year.toString(), width / 2f, 100f, textPaint)

            // Draw dots matrix
            val matrixBounds = drawDotsMatrix(canvas, width, height, dayOfYear)

            // Draw progress bars
            drawSideProgressBars(canvas, matrixBounds, dayOfYear, totalDaysInYear, currentWeek)
        }

        private fun drawDotsMatrix(canvas: Canvas, width: Int, height: Int, dayOfYear: Int): RectF {
            val dotRadius = 8f
            val spacing = 30f
            val matrixWidth = gridSize * spacing
            val matrixHeight = gridSize * spacing

            val startX = when (horizontalAlign) {
                0 -> 100f
                2 -> width - matrixWidth - 100f
                else -> (width - matrixWidth) / 2f
            }

            val startY = when (verticalAlign) {
                0 -> 200f
                2 -> height - matrixHeight - 300f
                else -> (height - matrixHeight) / 2f + 50f
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

            val leftX = matrixBounds.left - 80f
            drawVerticalProgressBar(
                canvas, leftX, matrixBounds.top, barThickness, barLength,
                dayOfYear.toFloat() / totalDays, "DAY", "$dayOfYear/$totalDays"
            )

            val rightX = matrixBounds.right + 40f
            val totalWeeks = 52
            drawVerticalProgressBar(
                canvas, rightX, matrixBounds.top, barThickness, barLength,
                currentWeek.toFloat() / totalWeeks, "WEEK", "$currentWeek/$totalWeeks"
            )
        }

        private fun drawVerticalProgressBar(
            canvas: Canvas, x: Float, y: Float, thickness: Float, length: Float,
            progress: Float, label: String, value: String
        ) {
            progressPaint.color = Color.argb(100, 255, 255, 255)
            canvas.drawRoundRect(x, y, x + thickness, y + length, 20f, 20f, progressPaint)

            progressPaint.color = dotColor
            val progressHeight = length * progress
            canvas.drawRoundRect(
                x, y + length - progressHeight, x + thickness, y + length, 20f, 20f, progressPaint
            )

            textPaint.textSize = 18f
            textPaint.color = Color.WHITE
            textPaint.textAlign = Paint.Align.CENTER
            canvas.save()
            canvas.rotate(-90f, x + thickness / 2, y - 20f)
            canvas.drawText(label, x + thickness / 2, y - 20f, textPaint)
            canvas.restore()

            textPaint.textSize = 16f
            canvas.save()
            canvas.rotate(-90f, x + thickness / 2, y + length + 40f)
            canvas.drawText(value, x + thickness / 2, y + length + 40f, textPaint)
            canvas.restore()
        }
    }
}