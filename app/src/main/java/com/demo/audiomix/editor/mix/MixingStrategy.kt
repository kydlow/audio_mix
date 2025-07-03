package com.demo.audiomix.editor.mix

interface MixingStrategy {
    // 应用混音方法到输入音频数据
    fun applyMix(pcm1: ByteArray, pcm2: ByteArray): ByteArray
}