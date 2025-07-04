package com.demo.audiomix.editor.reader.strategy

import com.demo.audiomix.bean.AudioInfo
import java.io.InputStream

interface AudioParseStrategy {
    // 解析输入流并返回 PCM 数据及元信息
    suspend fun parse(inputStream: InputStream): AudioInfo
    // 支持的格式扩展名
    val supportedFormat: String
}