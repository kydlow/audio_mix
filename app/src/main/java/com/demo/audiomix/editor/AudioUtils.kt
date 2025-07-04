package com.demo.audiomix.editor

import android.content.Context
import android.util.Log
import com.demo.audiomix.editor.mix.MixingStrategy
import com.demo.audiomix.editor.reader.AudioReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File

// 音频处理工具类，负责将两个WAV音频文件转换为PCM格式并进行混音
class AudioUtils(private val context: Context) {
    // 将两个WAV资源文件转换为PCM格式，混音后保存为WAV文件，并返回混音后的PCM数据
    // instrumentalResId: 伴奏音频资源ID
    // vocalsResId: 人声音频资源ID
    // outputWavPath: 输出WAV文件的路径
    // mixingStrategy 混音策略
    suspend fun mixAudio(
        audioResId1: Int,
        audioResId2: Int,
        outputWavPath: String,
        audio1ResIdFormat: String = "wav",
        audio2ResIdFormat: String = "wav",
        mixingStrategy: MixingStrategy
    ): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val audioReader = AudioReader(context)
            // 并行读取两个WAV文件的PCM数据
            val audioJob1 = async { audioReader.readPcmByResourceId(audioResId1, format = audio1ResIdFormat) }
            val audioJob2 = async { audioReader.readPcmByResourceId(audioResId2, format = audio2ResIdFormat) }

            // 获取WAV数据
            val audio1AudioInfo = audioJob1.await()
            val audio2AudioInfo = audioJob2.await()

            // 检查是否成功读取两个音频的PCM数据
            if (audio1AudioInfo.pcmData.isEmpty() || audio2AudioInfo.pcmData.isEmpty()) {
                Log.i("ellery","读取一个或两个WAV文件失败")
                return@withContext null
            }

            // 检查采样率和声道数是否一致
            if (audio1AudioInfo.sampleRate != audio2AudioInfo.sampleRate || audio1AudioInfo.channels != audio2AudioInfo.channels) {
                Log.i("ellery","音频格式不匹配: 采样率或声道数不同")
                return@withContext null
            }
            Log.i("ellery","采样率 = " + audio1AudioInfo.sampleRate + " 声道数 " + audio1AudioInfo.channels )
            // 混音处理
            val mixedPcm = mixingStrategy.applyMix(audio1AudioInfo.pcmData,audio2AudioInfo.pcmData)

            // 保存混音后的数据为WAV文件
            val outputFile = File(outputWavPath)
            outputFile.parentFile?.mkdirs() // 创建输出文件目录
            WavUtils.writePcmToWav(mixedPcm, outputFile,audio1AudioInfo.sampleRate,audio1AudioInfo.channels )

            Log.i("ellery","混音和WAV转换完成，输出路径: $outputWavPath")
            mixedPcm // 返回混音后的PCM数据
        } catch (e: Exception) {
            Log.i("ellery","转换或混音过程中出错: ${e.message}")
            null
        }
    }



    suspend fun concatenateAudio(
        audioResId1: Int,
        audioResId2: Int,
        outputWavPath: String,
        audio1ResIdFormat: String = "wav",
        audio2ResIdFormat: String = "wav"
    ): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val audioReader = AudioReader(context)
            // 并行读取两个WAV文件的PCM数据
            val audio1Job = async { audioReader.readPcmByResourceId(audioResId1, format = audio1ResIdFormat) }
            val audio2Job = async { audioReader.readPcmByResourceId(audioResId2, format = audio2ResIdFormat) }

            // 获取WAV数据
            val audio1AudioInfo = audio1Job.await()
            val audio2AudioInfo = audio2Job.await()

            // 检查是否成功读取两个音频的PCM数据
            if (audio1AudioInfo.pcmData.isEmpty() || audio2AudioInfo.pcmData.isEmpty()) {
                Log.i("ellery","读取一个或两个WAV文件失败")
                return@withContext null
            }

            // 检查采样率和声道数是否一致
            if (audio1AudioInfo.sampleRate != audio2AudioInfo.sampleRate || audio1AudioInfo.channels != audio2AudioInfo.channels) {
                Log.i("ellery","音频格式不匹配: 采样率或声道数不同")
                return@withContext null
            }
            Log.i("ellery","采样率 = " + audio1AudioInfo.sampleRate + " 声道数 " + audio1AudioInfo.channels )
            // 合并处理
            val concatenatedData = ByteArray(audio1AudioInfo.pcmData.size + audio2AudioInfo.pcmData.size)
            System.arraycopy(audio1AudioInfo.pcmData, 0, concatenatedData, 0, audio1AudioInfo.pcmData.size)
            System.arraycopy(audio2AudioInfo.pcmData, 0, concatenatedData, audio1AudioInfo.pcmData.size, audio2AudioInfo.pcmData.size)

            // 保存混音后的数据为WAV文件
            val outputFile = File(outputWavPath)
            outputFile.parentFile?.mkdirs() // 创建输出文件目录
            WavUtils.writePcmToWav(concatenatedData, outputFile,audio1AudioInfo.sampleRate,audio1AudioInfo.channels )

            Log.i("ellery","拼接和WAV转换完成，输出路径: $outputWavPath")
            concatenatedData // 返回混音后的PCM数据
        } catch (e: Exception) {
            Log.i("ellery","转换或拼接过程中出错: ${e.message}")
            null
        }
    }

    suspend fun clipAudio(
        audioResId1: Int,
        outputWavPath: String,
        audio1ResIdFormat: String = "wav",
        startTime: Long, // 起始时间（秒）
        endTime: Long,   // 结束时间（秒）
        fadeInDuration: Double = 0.0,  // 淡入时长（秒）
        fadeOutDuration: Double = 0.0  // 淡出时长（秒）
    ): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val audioReader = AudioReader(context)
            // 并行读取两个WAV文件的PCM数据
            val audio1Job = async { audioReader.readPcmByResourceId(audioResId1, format = audio1ResIdFormat) }
            // 获取WAV数据
            val audio1AudioInfo = audio1Job.await()

            // 检查是否成功读取两个音频的PCM数据
            if (audio1AudioInfo.pcmData.isEmpty()) {
                Log.i("ellery","读取WAV文件失败")
                return@withContext null
            }

            Log.i("ellery","采样率 = " + audio1AudioInfo.sampleRate + " 声道数 " + audio1AudioInfo.channels + " 位深度 " + audio1AudioInfo.bytePerSample )
            //startbyte = t(start-s) * sample * channel * bitsPerSample
            //startbyte = t(end-s) * sample * channel * bitsPerSample
            // 计算样本索引
            val startSample = (startTime * audio1AudioInfo.sampleRate).toLong().coerceAtLeast(0)
            val endSample = (endTime * audio1AudioInfo.sampleRate).toLong()

            Log.i("ellery","pcm size = ${audio1AudioInfo.pcmData.size} " )

            val totalSamples = endSample - startSample
            if (totalSamples <= 0) throw IllegalArgumentException("Invalid time range")

            // 计算字节偏移
            val startByte = startTime * audio1AudioInfo.sampleRate * audio1AudioInfo.bytePerSample/8 * audio1AudioInfo.channels


            Log.i("ellery","startByte = $startByte " )

            // 提取剪辑数据
            val clipData = ByteArray((totalSamples).toInt() * audio1AudioInfo.bytePerSample/8 * audio1AudioInfo.channels)
            System.arraycopy(audio1AudioInfo.pcmData, startByte.toInt(), clipData, 0, clipData.size)

            // 保存混音后的数据为WAV文件
            val outputFile = File(outputWavPath)
            outputFile.parentFile?.mkdirs() // 创建输出文件目录
            WavUtils.writePcmToWav(clipData, outputFile,audio1AudioInfo.sampleRate,audio1AudioInfo.channels )

            Log.i("ellery","剪辑和WAV转换完成，输出路径: $outputWavPath")
            clipData // 返回混音后的PCM数据
        } catch (e: Exception) {
            Log.i("ellery","转换或剪辑过程中出错: ${e.message}")
            null
        }
    }
}
