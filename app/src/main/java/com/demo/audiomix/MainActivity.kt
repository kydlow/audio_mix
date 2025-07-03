package com.demo.audiomix

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.demo.audiomix.activity.AvgMixActivity
import com.demo.audiomix.activity.NLMSMixActivity
import com.demo.audiomix.activity.WavPlayActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_main)

        findViewById<View>(R.id.btnSource).setOnClickListener {
            startActivity(Intent(this@MainActivity, WavPlayActivity::class.java))
        }

        findViewById<View>(R.id.btnAvgMix).setOnClickListener {
            startActivity(Intent(this@MainActivity, AvgMixActivity::class.java))
        }

        findViewById<View>(R.id.btnNLMSMix).setOnClickListener {
            startActivity(Intent(this@MainActivity, NLMSMixActivity::class.java))
        }
    }
}