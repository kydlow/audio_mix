package com.demo.audiomix.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var pcmData: ByteArray = byteArrayOf() // PCM数据
    private var progress: Float = 0f // 播放进度（0到1）
    private var downsampledData: FloatArray = floatArrayOf()
    private var downsamplingFactor: Int = 1
    private var needsDownsampling: Boolean = true // 标记以避免冗余下采样


    private val waveformPaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 2f
        isAntiAlias = true
    }
    private val progressPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 2f
    }
    private val centerLinePaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 1f
    }

    private var mediaPlayer: MediaPlayer? = null
    private val progressHandler = Handler(Looper.getMainLooper())
    private val progressUpdater = object : Runnable {
        override fun run() {
            updateProgressFromMediaPlayer()
            progressHandler.postDelayed(this, 100)
        }
    }

    fun setMediaPlayer(player: MediaPlayer) {
        mediaPlayer = player
        startProgressUpdates()
    }

    fun startProgressUpdates() {
        progressHandler.post(progressUpdater)
    }

    fun stopProgressUpdates() {
        progressHandler.removeCallbacks(progressUpdater)
    }

    private fun updateProgressFromMediaPlayer() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                val duration = player.duration.toFloat()
                if (duration > 0) {
                    val progress = player.currentPosition.toFloat() / duration
                    setProgress(progress)
                }
            }
        }
    }

    // 设置PCM数据
    fun setPcmData(data: ByteArray) {
        pcmData = data
        Log.d("ellery", "PCM data size: ${pcmData.size}, first 20 bytes: ${
            pcmData.take(20).joinToString()
        }")
        invalidate()
    }


    // 设置播放进度（0到1）
    fun setProgress(progress: Float) {
        this.progress = progress.coerceIn(0f, 1f)
        needsDownsampling = true // Mark for downsampling on next draw
        invalidate()
    }

    private fun downsampleData() {
        if (!needsDownsampling || pcmData.isEmpty() || width == 0) {
            downsampledData = floatArrayOf()
            return
        }

        val sampleCount = pcmData.size / 2
        val targetPoints = (width * 2).coerceAtLeast(1) // 2 points per pixel
        downsamplingFactor = (sampleCount / targetPoints).coerceAtLeast(1)

        val newSize = sampleCount / downsamplingFactor
        downsampledData = FloatArray(newSize)

        for (i in 0 until newSize) {
            val startIdx = i * downsamplingFactor * 2
            if (startIdx + 1 >= pcmData.size) break

            val byte1 = pcmData[startIdx].toInt()
            val byte2 = pcmData[startIdx + 1].toInt()
            val sample = (byte2 shl 8) or (byte1 and 0xFF)
            downsampledData[i] = (sample / 32768f).coerceIn(-1f, 1f) // Normalize and clamp
        }

        needsDownsampling = false // 更新完成
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return
// 如果需要，执行下采样
        if (needsDownsampling) {
            downsampleData()
        }

        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2

        // 绘制中心线
        canvas.drawLine(0f, centerY, width, centerY, centerLinePaint)

        if (downsampledData.isEmpty()) {
            Log.i("ellery", "downsampledData is empty")
            return
        }

        // 计算要显示的样本数
        val sampleCount = downsampledData.size
        if (sampleCount == 0) {
            Log.i("ellery", "sampleCount is 0")
            return
        }

        // 计算每个样本对应的宽度
        val sampleWidth = width / sampleCount

        // 绘制波形
        for (i in 0 until sampleCount - 1) {
            val normalizedSample = downsampledData[i] // Already normalized in [-1, 1]
            val nextNormalizedSample = downsampledData[i + 1]

            val x1 = i * sampleWidth
            val x2 = (i + 1) * sampleWidth
            val y1 = centerY - (normalizedSample * centerY)
            val y2 = centerY - (nextNormalizedSample * centerY)

            canvas.drawLine(x1, y1, x2, y2, waveformPaint)

//            Log.i("ellery", "sample[$i]= $normalizedSample")
        }

        // 绘制进度线
        val progressX = width * progress
        canvas.drawLine(progressX, 0f, progressX, height, progressPaint)
    }

    //完成视图后调用此函数以防止内存泄漏
    fun release() {
        stopProgressUpdates()
        mediaPlayer = null
    }
}