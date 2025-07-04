package com.demo.audiomix.utils

object FormatUtils{

    fun secondsToTimeFormat(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }

    fun timeFormatToSeconds(time: String?): Long {
        val parts = time?.split(":")
        if (parts?.size != 3) return -1L

        val hours = parts[0].toLongOrNull() ?: return -1L
        val minutes = parts[1].toLongOrNull() ?: return -1L
        val seconds = parts[2].toLongOrNull() ?: return -1L

        if (hours < 0 || minutes !in 0..59 || seconds !in 0..59) {
            return -1L
        }

        return hours * 3600 + minutes * 60 + seconds
    }
}