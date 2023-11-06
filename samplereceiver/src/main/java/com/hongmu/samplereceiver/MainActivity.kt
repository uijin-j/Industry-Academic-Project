package com.hongmu.samplereceiver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.hongmu.samplereceiver.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            val intent = Intent(this, MyService::class.java)
            ContextCompat.startForegroundService(this, intent)
        }

        binding.btnStop.setOnClickListener {
            val intent = Intent(this, MyService::class.java)
            stopService(intent)
        }

    }
}