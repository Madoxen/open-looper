package com.example.openlooper

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.osmdroid.util.GeoPoint

typealias LocationChangedListener = (Location) -> Unit

class LocationRecorderService : Service(), LocationListener {

    private lateinit var locationManager: LocationManager
    private var binder: IBinder? =
        LocationRecorderBinder()        // interface for clients that bind
    private val onLocationChangedCallbacks: MutableList<LocationChangedListener> = mutableListOf()
    private val recordedRoute: MutableList<GeoPoint> = mutableListOf()
    var isRecording: Boolean = false
        private set


    override fun onCreate() {
        // The service is being created
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // The service is starting, due to a call to startService()
        //Start requesting location updates

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5f, this)
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // A client is binding to the service with bindService()
        return binder
    }


    override fun onDestroy() {
        // The service is no longer used and is being destroyed
        stopForeground(true)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "OpenLooper Channel"
            val descriptionText = "This is open looper channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d("OpenLooperLRS", location.latitude.toString() + " " + location.longitude.toString())

        if (isRecording)
            recordedRoute.add(GeoPoint(location.latitude, location.longitude))

        //Call every subscriber
        onLocationChangedCallbacks.forEach {
            it(location)
        }
    }


    fun addLocationChangedListener(listener: LocationChangedListener) {
        onLocationChangedCallbacks.add(listener)
    }

    fun removeLocationChangedListener(listener: LocationChangedListener) {
        onLocationChangedCallbacks.remove(listener)
    }

    fun startRecording() {
        isRecording = true
        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.sym_def_app_icon)
            .setContentTitle("OpenLooper")
            .setContentText("Recording your new favourite route!").build()
        startForeground(1337, notification)
    }

    fun stopRecording() {
        isRecording = false
        stopForeground(true)
        stopSelf()
    }

    fun resetRecording() {
        recordedRoute.clear()
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocationRecorderBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): LocationRecorderService = this@LocationRecorderService
    }

    fun getRecordedRoute(): List<GeoPoint> {
        return recordedRoute
    }
}