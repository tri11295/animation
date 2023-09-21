package com.udacity

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

    private var circleAngle = 0f
    private var progress = 0f

    private var downloadJob: Job? = null

    init {
        background = context.getDrawable(R.color.colorPrimary)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (isLoading) {
            canvas?.drawRect(0f, 0f, width * progress, height.toFloat(), squarePaint)
            drawProgressCircle(canvas)
            drawTextDownLoad(context.getString(R.string.downloading_text), canvas)
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

    private fun finishDownloading(onFinishListener: (() -> Unit)? = null) {
        downloadJob?.cancel()
        isLoading = false
        isEnabled = true
        circleAngle = 0f
        progress = 0f
        onFinishListener?.invoke()
        invalidate()
    }

    fun startDownloadWithTime(time: Long, onFinishListener: (() -> Unit)? = null) {
        if (isLoading) return
        animationDuration = time
        isLoading = true
        isEnabled = false
        downloadJob = CoroutineScope(Dispatchers.Main).launch {
            val startTime = System.currentTimeMillis()
            while (circleAngle < 360f && progress < 1) {
                val elapsedTime = System.currentTimeMillis() - startTime
                circleAngle = (elapsedTime.toFloat() / animationDuration) * 360f
                progress = (elapsedTime.toFloat() / animationDuration)
                invalidate()
                delay(10) // Delay to achieve smooth animation
            }
            finishDownloading(onFinishListener)
        }
    }
}