package com.demo.audiomix.editor

import android.content.Context
import android.util.Log
import com.demo.audiomix.bean.AudioInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *
 */
class AudioProcessor(private val context: Context) {

    suspend fun readWavToPcmByResourceId(resId: Int): AudioInfo = withContext(Dispatchers.IO) {
        try {
            context.resources.openRawResource(resId).use { inputStream ->
                WavUtils.readWavToPcm(inputStream)
            }
        } catch (e: Exception) {
            Log.e("ellery", "Failed to read resource $resId: ${e.message}", e)
            AudioInfo(byteArrayOf(), 0, 0)
        }
    }
}