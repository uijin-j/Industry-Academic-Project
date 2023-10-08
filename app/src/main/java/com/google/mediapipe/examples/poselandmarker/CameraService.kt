package com.google.mediapipe.examples.poselandmarker

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService

class CameraService : LifecycleService() {

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }
}