package com.demo.audiomix.editor.reader.strategy.impl

import android.util.Log
import com.demo.audiomix.bean.AudioInfo
import com.demo.audiomix.editor.reader.strategy.AudioParseStrategy
import java.io.ByteArrayOutputStream
import java.io.InputStream

object WavParseStrategy : AudioParseStrategy {
    private const val TAG = "WavParseStrategy"
    private const val WAV_HEADER_SIZE = 44
    private const val BUFFER_SIZE = 8192

    override val supportedFormat: String = "wav"

    override suspend fun parse(inputStream: InputStream): AudioInfo {
        try {
            inputStream.use {
                // 读取 WAV 头部
                val header = ByteArray(WAV_HEADER_SIZE)
                if (it.read(header) != WAV_HEADER_SIZE) {
                    Log.e(TAG, "Failed to read WAV header")
                    return AudioInfo(byteArrayOf(), 0, 0,0, supportedFormat)
                }

                // 验证 WAV 头部
                if (!validateWavHeader(header)) {
                    Log.e(TAG, "Invalid WAV file format")
                    return AudioInfo(byteArrayOf(), 0, 0,0, supportedFormat)
                }

                // 解析采样率和声道数
                val channels = readShort(header, 22)
                val sampleRate = readInt(header, 24)

                // 	每样本bit数
                val bytePerSample = readShort(header, 34)

                // 读取 PCM 数据
                val pcmData = readPcmData(it)
                return AudioInfo(pcmData, sampleRate, channels,bytePerSample, supportedFormat)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing WAV: ${e.message}", e)
            return AudioInfo(byteArrayOf(), 0, 0,0, supportedFormat)
        }
    }

    /**
     * 验证header是否wav格式
     */
    private fun validateWavHeader(header: ByteArray): Boolean {
        return header.size == WAV_HEADER_SIZE &&
                String(header, 0, 4) == "RIFF" &&
                String(header, 8, 4) == "WAVE" &&
                String(header, 12, 4) == "fmt "
    }

    /**
     * 读取pcm数据
     */
    private fun readPcmData(inputStream: InputStream): ByteArray {
        val buffer = ByteArray(BUFFER_SIZE)
        val outputStream = ByteArrayOutputStream()
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        return outputStream.toByteArray()
    }

    private fun readShort(data: ByteArray, offset: Int): Int {
        return (data[offset].toInt() and 0xFF) or
                (data[offset + 1].toInt() shl 8)
    }

    private fun readInt(data: ByteArray, offset: Int): Int {
        return (data[offset].toUByte().toInt() and 0xFF) or
                (data[offset + 1].toUByte().toInt() shl 8) or
                (data[offset + 2].toUByte().toInt() shl 16) or
                (data[offset + 3].toUByte().toInt() shl 24)
    }
}