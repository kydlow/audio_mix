package com.demo.audiomix.editor.mix.impl

import com.demo.audiomix.editor.mix.MixingStrategy

class NLMSMixingStrategy : MixingStrategy {
    // 混音两个PCM数据
    // pcm1: 第一个PCM数据（通常为伴奏）
    // pcm2: 第二个PCM数据（通常为人声）
    // 返回: 混音后的PCM数据
    override fun applyMix(pcm1: ByteArray, pcm2: ByteArray): ByteArray {
        val length = maxOf(pcm1.size, pcm2.size) // 取较长的音频长度
        val result = ByteArray(length)
        val minLength = minOf(pcm1.size, pcm2.size) // 取较短的音频长度用于混音
        var factor = 1.0f // 衰减因子，初始值为1
        val maxValue = Short.MAX_VALUE.toFloat() // 32767
        val minValue = Short.MIN_VALUE.toFloat() // -32768

        // 每2字节处理一个样本（16位PCM）
        for (i in 0 until minLength step 2) {
            // 将两个字节转换为一个16位样本（小端序）
            val sample1 = if (i + 1 < pcm1.size) {
                ((pcm1[i].toInt() and 0xFF) or (pcm1[i + 1].toInt() shl 8)).toShort()
            } else 0.toShort()
            val sample2 = if (i + 1 < pcm2.size) {
                ((pcm2[i].toInt() and 0xFF) or (pcm2[i + 1].toInt() shl 8)).toShort()
            } else 0.toShort()

            // 计算混音值并应用衰减因子
            var mix = (sample1.toFloat() + sample2.toFloat()) * factor

            // 处理溢出情况
            if (mix > maxValue) {
                factor = maxValue / mix
                mix = maxValue
            } else if (mix < minValue) {
                factor = minValue / mix
                mix = minValue
            }

            // 如果衰减因子小于1，逐渐恢复
            if (factor < 1.0f) {
                factor += (1.0f - factor) / 32.0f // 步长调整
            }

            // 将混音后的样本转换回字节数组
            val clampedSample = mix.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            result[i] = (clampedSample.toInt() and 0xFF).toByte()
            result[i + 1] = (clampedSample.toInt() shr 8).toByte()
        }

        // 处理剩余部分（如果一个音频比另一个长）
        if (minLength < length) {
            val source = if (pcm1.size > pcm2.size) pcm1 else pcm2
            for (i in minLength until length) {
                result[i] = source[i]
            }
        }

        return result
    }
}