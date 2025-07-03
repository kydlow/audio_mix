package com.demo.audiomix.editor.mix.impl

import com.demo.audiomix.editor.mix.MixingStrategy

class AvgMixingStrategy : MixingStrategy {
    // 混音两个PCM数据
    // pcm1: 第一个PCM数据（通常为伴奏）
    // pcm2: 第二个PCM数据（通常为人声）
    // 返回: 混音后的PCM数据
    override fun applyMix(pcm1: ByteArray, pcm2: ByteArray): ByteArray {
        val length = maxOf(pcm1.size, pcm2.size) // 取较长的音频长度
        val result = ByteArray(length)
        val minLength = minOf(pcm1.size, pcm2.size) // 取较短的音频长度用于混音
        // 每2字节处理一个样本（16位PCM）
        // 每2字节处理一个样本（16位PCM）
        for (i in 0 until length step 2) {
            if (i < minLength) {
                // 对于重叠部分，进行混音
                // 将两个字节转换为一个16位样本（小端序）一个short为2个byte
                val sample1 = if (i + 1 < pcm1.size) {
                    ((pcm1[i].toInt() and 0xFF) or (pcm1[i + 1].toInt() shl 8)).toShort()
                } else 0.toShort()
                val sample2 = if (i + 1 < pcm2.size) {
                    ((pcm2[i].toInt() and 0xFF) or (pcm2[i + 1].toInt() shl 8)).toShort()
                } else 0.toShort()
                // 计算两个样本的平均值
                val mixedSample = (sample1.toInt() + sample2.toInt()) / 2
                // 防止样本值溢出，限制在Short的范围内
                val clampedSample = when {
                    mixedSample > Short.MAX_VALUE -> Short.MAX_VALUE
                    mixedSample < Short.MIN_VALUE -> Short.MIN_VALUE
                    else -> mixedSample.toShort()
                }
                // 将混音后的样本转换回字节数组
                result[i] = (clampedSample.toInt() and 0xFF).toByte()
                result[i + 1] = (clampedSample.toInt() shr 8).toByte()
            } else {
                // 对于超出较短音频的部分，直接复制较长音频的数据
                val source = if (pcm1.size > pcm2.size) pcm1 else pcm2
                result[i] = source[i]
                if (i + 1 < length) {
                    result[i + 1] = source[i + 1]
                }
            }
        }
        return result
    }
}