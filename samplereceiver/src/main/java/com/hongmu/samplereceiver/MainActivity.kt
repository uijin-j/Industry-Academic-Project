package com.hongmu.samplereceiver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import androidx.core.content.ContextCompat
import com.hongmu.samplereceiver.databinding.ActivityMainBinding
import com.hongmu.samplereceiver.db.LogDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val logDatabase by lazy { LogDatabase.getInstance(this) }
    private val list = mutableListOf<Map<String, String>>()
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val adapter = object : SimpleAdapter(this, list, android.R.layout.simple_list_item_2, arrayOf("type", "date"), intArrayOf(android.R.id.text1, android.R.id.text2)) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val view =  super.getView(position, convertView, parent)
                val item = list[position]
                view.setBackgroundColor(ContextCompat.getColor(this@MainActivity, when(item.getOrDefault("type", "")) {
                    "2" -> R.color.yellow
                    "3" -> R.color.orange
                    "4" -> R.color.red
                    else -> R.color.white
                }))

                return view
            }
        }
        binding.listview.adapter = adapter

        binding.btnStart.setOnClickListener {
            val intent = Intent(this, MyService::class.java)
            ContextCompat.startForegroundService(this, intent)
            MyService.isRunning = true
            updateUI()
        }

        binding.btnStop.setOnClickListener {
            val intent = Intent(this, MyService::class.java)
            stopService(intent)
            MyService.isRunning = false
            updateUI()
        }

        logDatabase.logDao().getAll().observe(this) {
            list.clear()
            it.forEach { log ->
                list.add(hashMapOf(
                    "type" to log.type.toString(),
                    "date" to format.format(Date(log.date))
                ))
            }
            adapter.notifyDataSetChanged()
        }
        updateUI()
    }

    private fun updateUI() {
        binding.btnStart.isEnabled = !MyService.isRunning
        binding.btnStop.isEnabled = MyService.isRunning
        binding.progressBar.visibility = if(MyService.isRunning) View.VISIBLE else View.GONE
    }
}