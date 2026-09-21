package com.example.familysafety

import android.app.*
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class LocationService : Service(), LocationListener {
    private lateinit var lm: LocationManager
    private val exec = Executors.newSingleThreadExecutor()
    override fun onCreate() {
        super.onCreate()
        val ch = NotificationChannel("location", "Location sharing", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        startForeground(10, Notification.Builder(this, "location").setSmallIcon(android.R.drawable.ic_menu_mylocation).setContentTitle("Family Safety").setContentText("Location sharing is ON").setOngoing(true).build())
        lm = getSystemService(LOCATION_SERVICE) as LocationManager
        try { lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000L, 20f, this) } catch (_: SecurityException) {}
        try { lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000L, 20f, this) } catch (_: SecurityException) {}
    }
    override fun onLocationChanged(location: Location) {
        val token=AppConfig.idToken(this); val code=AppConfig.familyCode(this); val uid=AppConfig.uid(this)
        if (token.isBlank() || code.isBlank() || uid.isBlank()) return
        val fields=JSONObject().put("name",AppConfig.name(this)).put("sharing",true).put("lat",location.latitude).put("lon",location.longitude).put("updated",SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(Date()))
        exec.execute { try { FirebaseApi.writeMember(this,token,code,uid,fields) } catch (_:Exception) {} }
    }
    override fun onDestroy() { try { lm.removeUpdates(this) } catch (_:Exception) {}; AppConfig.setSharing(this,false); exec.shutdownNow(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
