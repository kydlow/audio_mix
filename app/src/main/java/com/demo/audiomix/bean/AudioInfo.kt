package com.demo.audiomix.bean
// WAV 文件数据类，包含 PCM 数据、采样率和声道数
data class AudioInfo(val pcmData: ByteArray, val sampleRate: Int, val channels: Int)