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
import com.demo.audiomix.editor.mix.impl.AvgMixingStrategy
import com.demo.audiomix.view.WaveformView
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


class ConcatActivity : AppCompatActivity() {
    private lateinit var audioTrack: AudioTrack
    private lateinit var waveformView: WaveformView
    private lateinit var playButton: Button
    private lateinit var concatButton: Button
    private var pcmData: ByteArray? = null
    private lateinit var outputWavPath: String
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.layout_concat_test)

        waveformView = findViewById(R.id.waveform_view)
        playButton = findViewById(R.id.btnPlay)
        concatButton = findViewById(R.id.btnConcatAudio)

        val audioUtils = AudioUtils(this)
        outputWavPath = "${filesDir}/concat.wav"

        concatButton.setOnClickListener {
            lifecycleScope.launch {
                pcmData = audioUtils.concatenateAudio(
                    audioResId1 = R.raw.concat1,
                    audioResId2 = R.raw.concat2,
                    outputWavPath = outputWavPath,
                )
                if (pcmData != null) {
                    waveformView.setPcmData(pcmData!!)
                    Toast.makeText(this@ConcatActivity, "Audio concat successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ConcatActivity, "Failed to concat audio", Toast.LENGTH_SHORT).show()
                }
            }
        }

        playButton.setOnClickListener {
            if(pcmData == null){
                Toast.makeText(this@ConcatActivity,"pcm is null",Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@ConcatActivity, "Playback completed", Toast.LENGTH_SHORT).show()
                }
                setOnErrorListener { _, what, extra ->
                    Toast.makeText(this@ConcatActivity, "Error playing audio: $what, $extra", Toast.LENGTH_SHORT).show()
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
