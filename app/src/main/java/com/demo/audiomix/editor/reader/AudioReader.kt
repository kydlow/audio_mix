package com.demo.audiomix.editor.reader

import android.content.Context
import android.util.Log
import com.demo.audiomix.bean.AudioInfo
import com.demo.audiomix.editor.reader.factory.AudioParseStrategyFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

class AudioReader(private val context: Context) {

    suspend fun readPcmByResourceId(resId: Int, format: String = "wav"): AudioInfo =
        withContext(Dispatchers.IO) {
            try {
                val strategy = AudioParseStrategyFactory.getStrategy(format)
                context.resources.openRawResource(resId).use { inputStream ->
                    strategy.parse(inputStream)
                }
            } catch (e: Exception) {
                Log.e("AudioReader", "Failed to read resource $resId (format: $format): ${e.message}", e)
                AudioInfo(byteArrayOf(), 0, 0,0, format)
            }
        }

    suspend fun readPcmByFile(file: File): AudioInfo = withContext(Dispatchers.IO) {
        try {
            val strategy = AudioParseStrategyFactory.getStrategyForFile(file.absolutePath)
            FileInputStream(file).use { inputStream ->
                strategy.parse(inputStream)
            }
        } catch (e: Exception) {
            Log.e("AudioReader", "Failed to read file ${file.absolutePath}: ${e.message}", e)
            AudioInfo(byteArrayOf(), 0, 0, 0,"unknown")
        }
    }

    suspend fun readPcmFromAssests(path: String): AudioInfo = withContext(Dispatchers.IO) {
        try {
            val strategy = AudioParseStrategyFactory.getStrategyForFile(path)
            context.assets.open(path).use { inputStream ->
                strategy.parse(inputStream)
            }
        } catch (e: Exception) {
            Log.e("AudioReader", "Failed to read asset $path: ${e.message}", e)
            AudioInfo(byteArrayOf(), 0, 0, 0,"unknown")
        }
    }
}