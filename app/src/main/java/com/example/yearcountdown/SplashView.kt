package com.example.yearcountdown

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class SplashView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var glowRadius = 0f
    private var alpha = 255
    private var animationProgress = 0f
    private var onAnimationComplete: (() -> Unit)? = null

    init {
        textPaint.textSize = 180f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.CENTER
    }

    fun startAnimation(onComplete: () -> Unit) {
        onAnimationComplete = onComplete

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 3000
        animator.interpolator = AccelerateDecelerateInterpolator()

        animator.addUpdateListener { animation ->
            animationProgress = animation.animatedValue as Float

            // Glow effect: grow then shrink
            glowRadius = if (animationProgress < 0.6f) {
                (animationProgress / 0.6f) * 50f
            } else {
                ((1f - animationProgress) / 0.4f) * 50f
            }

            // Fade out after 70%
            alpha = if (animationProgress < 0.7f) {
                255
            } else {
                ((1f - (animationProgress - 0.7f) / 0.3f) * 255).toInt()
            }

            invalidate()

            // Complete animation
            if (animationProgress >= 1f) {
                onAnimationComplete?.invoke()
            }
        }

        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(0xFF0a0a1e.toInt())

        val centerX = width / 2f
        val centerY = height / 2f

        // Draw glowing effect
        if (glowRadius > 0) {
            textPaint.color = Color.argb(
                (alpha * 0.3f).toInt(),
                255, 255, 255
            )
            textPaint.maskFilter = BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.NORMAL)
            canvas.drawText("SD", centerX, centerY + 60f, textPaint)
        }

        // Draw main text
        textPaint.color = Color.argb(alpha, 255, 255, 255)
        textPaint.maskFilter = null
        textPaint.setShadowLayer(glowRadius / 2f, 0f, 0f, Color.argb(alpha, 100, 200, 255))
        canvas.drawText("SD", centerX, centerY + 60f, textPaint)
    }
}