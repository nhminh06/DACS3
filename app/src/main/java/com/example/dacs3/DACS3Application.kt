package com.example.dacs3

import android.app.Application
import com.cloudinary.android.MediaManager

class DACS3Application : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = mapOf(
            "cloud_name" to "dsh6wv90k",
            "secure" to true
        )
        MediaManager.init(this, config)
    }
}
