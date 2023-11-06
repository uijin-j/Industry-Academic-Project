package com.hongmu.samplereceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getIntExtra("key_type", -1)
        Toast.makeText(context, "[SampleReceiver] $type 수신", Toast.LENGTH_SHORT).show()
    }
}