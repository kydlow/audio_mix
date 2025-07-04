package com.demo.audiomix.editor

import android.util.Log
import com.demo.audiomix.bean.AudioInfo
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * wav处理工具
 */
object WavUtils {
    private const val TAG = "WavUtils"
    private const val WAV_HEADER_SIZE = 44
    private const val BUFFER_SIZE = 8192 // 增大缓冲区以提高性能



    // 写入 PCM 数据到 WAV 文件
    fun writePcmToWav(pcmData: ByteArray, outputFile: File, sampleRate: Int, channels: Int) {
        try {
            if (pcmData.isEmpty()) {
                Log.e(TAG, "PCM data is empty, cannot write WAV file")
                return
            }
            if (sampleRate <= 0 || channels <= 0) {
                Log.e(TAG, "Invalid WAV parameters: sampleRate=$sampleRate, channels=$channels")
                return
            }

            FileOutputStream(outputFile).use { out ->
                val totalAudioLen = pcmData.size
                val totalDataLen = totalAudioLen + 36
                val byteRate = sampleRate * channels * 2 // 16-bit PCM

                // 写入 RIFF 头部
                out.write("RIFF".toByteArray())
                out.write(intToByteArray(totalDataLen))
                out.write("WAVE".toByteArray())

                // 写入 fmt 子块
                out.write("fmt ".toByteArray())
                out.write(intToByteArray(16)) // Subchunk1Size (PCM = 16)
                out.write(shortToByteArray(1)) // AudioFormat (PCM = 1)
                out.write(shortToByteArray(channels.toShort()))
                out.write(intToByteArray(sampleRate))
                out.write(intToByteArray(byteRate))
                out.write(shortToByteArray((channels * 2).toShort())) // BlockAlign
                out.write(shortToByteArray(16)) // BitsPerSample

                // 写入 data 子块
                out.write("data".toByteArray())
                out.write(intToByteArray(totalAudioLen))
                out.write(pcmData)
            }
            Log.i(TAG, "WAV file written successfully: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error writing WAV file: ${e.message}", e)
        }
    }

    // 验证 WAV 文件头部
    private fun validateWavHeader(header: ByteArray): Boolean {
        return header.size == WAV_HEADER_SIZE &&
                String(header, 0, 4) == "RIFF" &&
                String(header, 8, 4) == "WAVE" &&
                String(header, 12, 4) == "fmt "
    }

    // 读取 PCM 数据
    private fun readPcmData(inputStream: InputStream): ByteArray {
        val buffer = ByteArray(BUFFER_SIZE)
        val outputStream = ByteArrayOutputStream()
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        return outputStream.toByteArray()
    }

    // 从字节数组中读取 2 字节（小端序）
    private fun readShort(data: ByteArray, offset: Int): Int {
        return (data[offset].toInt() and 0xFF) or
                (data[offset + 1].toInt() shl 8)
    }

    // 从字节数组中读取 4 字节（小端序）
    private fun readInt(data: ByteArray, offset: Int): Int {
        return (data[offset].toUByte().toInt() and 0xFF) or
                (data[offset + 1].toUByte().toInt() shl 8) or
                (data[offset + 2].toUByte().toInt() shl 16) or
                (data[offset + 3].toUByte().toInt() shl 24)
    }

    // 将整数转换为 4 字节数组（小端序）
    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    // 将短整数转换为 2 字节数组（小端序）
    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }
}