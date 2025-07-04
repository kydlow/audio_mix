package com.demo.audiomix.activity

import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.demo.audiomix.R
import com.demo.audiomix.editor.AudioUtils
import com.demo.audiomix.editor.FileUtils
import com.demo.audiomix.utils.FormatUtils
import com.demo.audiomix.utils.KeyboardUtils
import com.demo.audiomix.view.WaveformView
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


class ClipActivity : AppCompatActivity() {
    private lateinit var audioTrack: AudioTrack
    private lateinit var waveformView: WaveformView
    private lateinit var playButton: Button
    private lateinit var btnClipAudio: Button
    private lateinit var startTime: EditText
    private lateinit var endTime: EditText
    private var pcmData: ByteArray? = null
    private lateinit var outputWavPath: String
    private var mediaPlayer: MediaPlayer? = null
    private var totalDuration:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.layout_clip_test)

        waveformView = findViewById(R.id.waveform_view)
        playButton = findViewById(R.id.btnPlay)
        btnClipAudio = findViewById(R.id.btnClipAudio)
        startTime = findViewById(R.id.tvStartTime)
        endTime = findViewById(R.id.tvEndTime)

        val audioUtils = AudioUtils(this)
        outputWavPath = "${filesDir}/clip.wav"


        totalDuration = FileUtils.getRawAudioDuration(this@ClipActivity,R.raw.song).toInt()
        endTime.setText(FormatUtils.secondsToTimeFormat(totalDuration.toLong()))


        btnClipAudio.setOnClickListener {
            lifecycleScope.launch {
                val startTime = FormatUtils.timeFormatToSeconds(startTime.text.toString())
                val endTime = FormatUtils.timeFormatToSeconds(endTime.text.toString())

                if(startTime == -1L || endTime == -1L){
                    Toast.makeText(this@ClipActivity,"startTime or endTime format error",Toast.LENGTH_SHORT).show()
                    return@launch
                }

                pcmData = audioUtils.clipAudio(
                    audioResId1 = R.raw.song,
                    outputWavPath = outputWavPath,
                    startTime = startTime,//传入秒
                    endTime = endTime,//传入秒
                    fadeInDuration = 0.0,
                    fadeOutDuration = 0.0
                )
                if (pcmData != null) {
                    waveformView.setPcmData(pcmData!!)
                    Toast.makeText(this@ClipActivity, "Audio clicp successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ClipActivity, "Failed to clip audio", Toast.LENGTH_SHORT).show()
                }
            }
        }

        playButton.setOnClickListener {
            if(pcmData == null){
                Toast.makeText(this@ClipActivity,"pcm is null",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(mediaPlayer == null){
                playWavFile(outputWavPath)
                playButton.text = "暂停"
            }else{
                if(mediaPlayer?.isPlaying == false){
                    mediaPlayer?.start()
                    waveformView.startProgressUpdates()
                    playButton.text = "暂停"
                }else {
                    mediaPlayer?.pause()
                    waveformView.stopProgressUpdates()
                    playButton.text = "播放"
                }
            }
        }
    }


    private fun playWavFile(filePath: String) {
        // Release any existing MediaPlayer instance
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()
        waveformView.setMediaPlayer(mediaPlayer!!)

        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(this, "WAV file not found", Toast.LENGTH_SHORT).show()
                return
            }

            mediaPlayer?.apply {
                setDataSource(filePath)
                prepare()
                start()
                waveformView.startProgressUpdates() //开始进度刷新
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                    Toast.makeText(this@ClipActivity, "Playback completed", Toast.LENGTH_SHORT).show()
                }
                setOnErrorListener { _, what, extra ->
                    Toast.makeText(this@ClipActivity, "Error playing audio: $what, $extra", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Error playing WAV file: ${e.message}", Toast.LENGTH_SHORT).show()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        waveformView.release()
        mediaPlayer?.release()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                //获取当前获得焦点的View
                val view = currentFocus
                //调用方法判断是否需要隐藏键盘
                KeyboardUtils.hideKeyboard(ev, view, this)
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}
