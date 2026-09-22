package com.example.familysafety

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.IBinder
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.concurrent.Executors

class LocationService : Service() {
    private lateinit var client: FusedLocationProviderClient
    private lateinit var callback: LocationCallback
    private val exec = Executors.newSingleThreadExecutor()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val notification = Notification.Builder(this, "FAMILY_SAFETY_CHANNEL")
            .setContentTitle("Family Safety Pro")
            .setContentText("Sharing live location with your family group")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)

        client = LocationServices.getFusedLocationProviderClient(this)
        
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15000L)
            .setMinUpdateIntervalMillis(10000L)
            .build()

        callback = object : LocationCallback() {
            override fun onLocationResult(res: LocationResult) {
                val loc = res.lastLocation ?: return
                exec.execute {
                    try {
                        val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                        val battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                        
                        FirebaseApi.writeMember(
                            this@LocationService,
                            AppConfig.idToken(this@LocationService),
                            AppConfig.familyCode(this@LocationService),
                            AppConfig.uid(this@LocationService),
                            AppConfig.name(this@LocationService),
                            loc.latitude,
                            loc.longitude,
                            battery,
                            true
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        try {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::client.isInitialized && ::callback.isInitialized) {
            client.removeLocationUpdates(callback)
        }
        
        exec.execute {
            try {
                val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                
                FirebaseApi.writeMember(
                    this,
                    AppConfig.idToken(this),
                    AppConfig.familyCode(this),
                    AppConfig.uid(this),
                    AppConfig.name(this),
                    0.0,
                    0.0,
                    battery,
                    false
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "FAMILY_SAFETY_CHANNEL",
            "Family Location Sharing",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
}
