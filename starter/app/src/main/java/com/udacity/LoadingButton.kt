package com.udacity

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatButton
import kotlinx.coroutines.*
import kotlin.math.max
import kotlin.math.min

@SuppressLint("UseCompatLoadingForDrawables")
class LoadingButton(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var isLoading = false
    private var animationDuration = 3000L
    private val circleRadius = resources.getDimensionPixelSize(R.dimen.circle_radius).toFloat()

    private val circlePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.yellow)
        style = Paint.Style.FILL
    }
    private val squarePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        style = Paint.Style.FILL_AND_STROKE
    }
    private val textPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        isAntiAlias = true
        strokeWidth = resources.getDimension(R.dimen.strokeWidth)
        textSize = resources.getDimension(R.dimen.textSize)
    }

    var textLoading: String = "Loading"

    private var circleAngle = 0f
    private var progress = 0f
    private val valueAnimator = ValueAnimator.ofFloat(0f, 1f)

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingButton)
        try {
            with(typedArray) {
                textLoading = typedArray.getString(R.styleable.LoadingButton_text_loading)
                    ?: "Loading"
                background = getDrawable(R.styleable.LoadingButton_background_color)
            }
        } finally {
            typedArray.recycle()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (isLoading) {
            canvas?.drawRect(0f, 0f, width * progress, height.toFloat(), squarePaint)
            drawProgressCircle(canvas)
            drawTextDownLoad(textLoading, canvas)
        } else {
            drawTextDownLoad(context.getString(R.string.download_text), canvas)
        }
    }

    private fun drawProgressCircle(canvas: Canvas?) {
        val centerX = (width) / 2f + 200
        val centerY = height / 2f + 12
        val sweepAngle = max(0f, min(360f, circleAngle))
        canvas?.drawArc(
            centerX - circleRadius,
            centerY - circleRadius,
            centerX + circleRadius,
            centerY + circleRadius,
            0f,
            sweepAngle,
            true,
            circlePaint
        )
    }

    private fun drawTextDownLoad(text: String, canvas: Canvas?) {
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        canvas?.drawText(
            text,
            ((width - textBounds.width()) / 2).toFloat(),
            ((height + textBounds.height()) / 2).toFloat(),
            textPaint
        )
    }

    fun finishDownloading(onFinishListener: (() -> Unit)? = null) {
        valueAnimator.cancel()
        isLoading = false
        isEnabled = true
        circleAngle = 0f
        progress = 0f
        onFinishListener?.invoke()
        invalidate()
    }

    fun startDownload() {
        if (isLoading) return
        isLoading = true
        isEnabled = false
        valueAnimator.duration = 4000
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Float
            circleAngle = animatedValue * 360f
            progress = animatedValue
            invalidate()
        }
        valueAnimator.start()
    }
}