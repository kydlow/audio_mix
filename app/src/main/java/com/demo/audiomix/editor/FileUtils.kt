package com.demo.audiomix.editor

import android.content.Context
import android.media.MediaMetadataRetriever
import java.io.IOException

object FileUtils {
    /**
     * 获取raw的音频长度
     */
    fun getRawAudioDuration(context: Context, rawResId: Int): Long {
        val retriever = MediaMetadataRetriever()
        try {
            // 获取 raw 资源的 FileDescriptor
            val resources = context.resources
            val fd = resources.openRawResourceFd(rawResId)
            retriever.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            return durationStr?.toLongOrNull()?.div(1000) ?: 0L // Convert milliseconds to seconds
        } catch (e: IOException) {
            throw IOException("Failed to read raw audio resource: ${e.message}")
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid raw resource ID: ${e.message}")
        } finally {
            retriever.release()
        }
    }
}