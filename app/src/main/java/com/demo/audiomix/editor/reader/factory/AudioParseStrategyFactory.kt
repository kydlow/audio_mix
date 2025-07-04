package com.demo.audiomix.editor.reader.factory

import com.demo.audiomix.editor.reader.strategy.AudioParseStrategy
import com.demo.audiomix.editor.reader.strategy.impl.WavParseStrategy

object AudioParseStrategyFactory {
    private val strategies = mapOf(
        "wav" to WavParseStrategy
        // 添加其他格式支持，如 "aac" to AacParseStrategy
    )

    fun getStrategy(format: String): AudioParseStrategy {
        return strategies[format.lowercase()]
            ?: throw IllegalArgumentException("Unsupported audio format: $format")
    }

    // 根据文件路径推断格式
    fun getStrategyForFile(filePath: String): AudioParseStrategy {
        val extension = filePath.substringAfterLast(".", "").lowercase()
        return getStrategy(extension)
    }
}