package com.example.openlooper


import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat

const val REQUEST_LOCATION_CODE = 1000
const val NOTIFICATION_CODE = "1337"
const val NOTIFICATION_CHANNEL_ID = "OpenLooperChannel"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

}