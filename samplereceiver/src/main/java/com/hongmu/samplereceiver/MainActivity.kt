package com.hongmu.samplereceiver

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hongmu.samplereceiver.databinding.ActivityMainBinding
import com.hongmu.samplereceiver.db.LogDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val logDatabase by lazy { LogDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val adapter = MainAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.recyclerView.adapter = adapter

        binding.btnStartStop.setOnClickListener {
            val intent = Intent(this, MyService::class.java)
            if(MyService.isRunning) {
                stopService(intent)
                MyService.isRunning = false
            } else {
                ContextCompat.startForegroundService(this, intent)
                MyService.isRunning = true
            }
            updateUI()
        }

        logDatabase.logDao().getAll().observe(this) {
            adapter.list = it
            binding.recyclerView.smoothScrollToPosition(it.size - 1)
        }
        updateUI()
    }

    private fun updateUI() {
        binding.btnStartStop.text = if(MyService.isRunning) "중지" else "시작"
        binding.progressBar.visibility = if(MyService.isRunning) View.VISIBLE else View.GONE
    }
}