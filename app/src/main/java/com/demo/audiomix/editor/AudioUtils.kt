package com.demo.audiomix.editor

import android.content.Context
import android.util.Log
import com.demo.audiomix.editor.mix.MixingStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

// 音频处理工具类，负责将两个WAV音频文件转换为PCM格式并进行混音
class AudioUtils(private val context: Context) {
    // 将两个WAV资源文件转换为PCM格式，混音后保存为WAV文件，并返回混音后的PCM数据
    // instrumentalResId: 伴奏音频资源ID
    // vocalsResId: 人声音频资源ID
    // outputWavPath: 输出WAV文件的路径
    // mixingStrategy 混音策略
    suspend fun convertAndMixWavToPcm(
        instrumentalResId: Int,
        vocalsResId: Int,
        outputWavPath: String,
        mixingStrategy: MixingStrategy
    ): ByteArray? = withContext(Dispatchers.IO) {
        try {
            // 并行读取两个WAV文件的PCM数据
            val instrumentalJob = async { AudioProcessor(context).readWavToPcmByResourceId(instrumentalResId) }
            val vocalsJob = async { AudioProcessor(context).readWavToPcmByResourceId(vocalsResId) }

            // 获取WAV数据
            val instrumentalWav = instrumentalJob.await()
            val vocalsWav = vocalsJob.await()

            // 检查是否成功读取两个音频的PCM数据
            if (instrumentalWav.pcmData.isEmpty() || vocalsWav.pcmData.isEmpty()) {
                Log.i("ellery","读取一个或两个WAV文件失败")
                return@withContext null
            }

            // 检查采样率和声道数是否一致
            if (instrumentalWav.sampleRate != vocalsWav.sampleRate || instrumentalWav.channels != vocalsWav.channels) {
                Log.i("ellery","音频格式不匹配: 采样率或声道数不同")
                return@withContext null
            }
            Log.i("ellery","采样率 = " + instrumentalWav.sampleRate + "声道数" + instrumentalWav.channels )
            // 混音处理
            val mixedPcm = mixingStrategy.applyMix(instrumentalWav.pcmData,vocalsWav.pcmData)

            // 保存混音后的数据为WAV文件
            val outputFile = File(outputWavPath)
            outputFile.parentFile?.mkdirs() // 创建输出文件目录
            WavUtils.writePcmToWav(mixedPcm, outputFile,instrumentalWav.sampleRate,instrumentalWav.channels )

            Log.i("ellery","混音和WAV转换完成，输出路径: $outputWavPath")
            mixedPcm // 返回混音后的PCM数据
        } catch (e: Exception) {
            Log.i("ellery","转换或混音过程中出错: ${e.message}")
            null
        }
    }


}
