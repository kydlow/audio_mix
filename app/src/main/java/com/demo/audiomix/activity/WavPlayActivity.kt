package com.demo.audiomix.activity

import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.demo.audiomix.editor.AudioUtils
import com.demo.audiomix.R
import com.demo.audiomix.editor.reader.AudioReader
import com.demo.audiomix.view.WaveformView
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


class WavPlayActivity : AppCompatActivity() {
    private lateinit var audioTrack: AudioTrack
    private lateinit var waveformView: WaveformView
    private lateinit var playButton: Button
    private var pcmData: ByteArray? = null
    private lateinit var outputWavPath: String
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.layout_wav_paly)

        waveformView = findViewById(R.id.waveform_view)
        playButton = findViewById(R.id.btnPlay)

        val audioReader = AudioReader(this)

        outputWavPath = "${filesDir}/mixed.wav"

        lifecycleScope.launch {
            pcmData = audioReader.readPcmByResourceId(
                resId = R.raw.song,
                format = "wav"
            ).pcmData
            if (pcmData != null) {
                waveformView.setPcmData(pcmData!!)
                Toast.makeText(this@WavPlayActivity, "Audio mixed successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@WavPlayActivity, "Failed to mix audio", Toast.LENGTH_SHORT).show()
            }
        }

        playButton.setOnClickListener {
            if(pcmData == null){
                Toast.makeText(this@WavPlayActivity,"pcm is null",Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@WavPlayActivity, "Playback completed", Toast.LENGTH_SHORT).show()
                }
                setOnErrorListener { _, what, extra ->
                    Toast.makeText(this@WavPlayActivity, "Error playing audio: $what, $extra", Toast.LENGTH_SHORT).show()
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
}
