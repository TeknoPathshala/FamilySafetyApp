package com.example.familysafety

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private val exec = Executors.newSingleThreadExecutor()

    override fun onCreate(b: Bundle?) { super.onCreate(b); showHome() }

    private fun showHome() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        // Header Section
        val title = TextView(this).apply {
            text = "🛡️ Family Safety Pro"
            textSize = 26f
            setTextColor(0xFF1E88E5.toInt())
            setPadding(0, 0, 0, 10)
        }
        val subtitle = TextView(this).apply {
            text = "Transparent Family Protection & Emergency Tracking"
            textSize = 14f
            setTextColor(0xFF757575.toInt())
            setPadding(0, 0, 0, 30)
        }
        root.addView(title)
        root.addView(subtitle)

        if (AppConfig.apiKey(this).isBlank()) root.addView(createCard("1. Setup Firebase", "Connect your database to sync family members.") { configure() })
        if (AppConfig.uid(this).isBlank()) root.addView(createCard("2. Account Login", "Sign in or register a new user account.") { auth() })

        if (AppConfig.uid(this).isNotBlank()) {
            root.addView(createCard("User Account", AppConfig.email(this)) {})
            root.addView(createCard("3. Family Group Code", "Code: ${AppConfig.familyCode(this).ifBlank { "Not set" }}") { profile() })

            // Camera Access Switch Card
            val cameraCard = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(30, 25, 30, 25)
                setBackgroundColor(0xFFF5F5F5.toInt())
                gravity = Gravity.CENTER_VERTICAL
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 0, 0, 20)
                layoutParams = params
            }

            val camText = TextView(this).apply {
                text = "📷 Allow Camera Access\n(For Emergency Snapshots)"
                textSize = 14f
                setTextColor(0xFF212121.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val camSwitch = Switch(this).apply {
                isChecked = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
                    } else {
                        toast("Camera permission disabled")
                        showHome()
                    }
                }
            }

            cameraCard.addView(camText)
            cameraCard.addView(camSwitch)
            root.addView(cameraCard)

            // Emergency SOS Button
            val sosBtn = Button(this).apply {
                text = "🚨 EMERGENCY SOS BROADCAST"
                textSize = 16f
                setBackgroundColor(0xFFD32F2F.toInt())
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(20, 30, 20, 30)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 10, 0, 20)
                layoutParams = params
                setOnClickListener { sendEmergencySOS() }
            }
            root.addView(sosBtn)

            // Live Sharing Toggle Button
            val sharingActive = AppConfig.sharing(this)
            val shareBtn = Button(this).apply {
                text = if (sharingActive) "⏹ STOP LIVE SHARING" else "▶ START LIVE SHARING"
                textSize = 15f
                setBackgroundColor(if (sharingActive) 0xFFE65100.toInt() else 0xFF2E7D32.toInt())
                setTextColor(0xFFFFFFFF.toInt())
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 0, 0, 20)
                layoutParams = params
                setOnClickListener {
                    if (AppConfig.sharing(this@MainActivity)) {
                        stopService(Intent(this@MainActivity, LocationService::class.java))
                        AppConfig.setSharing(this@MainActivity, false)
                        showHome()
                    } else startSharing()
                }
            }
            root.addView(shareBtn)

            root.addView(createCard("🗺️ Live Dashboard", "View locations & battery of members") { members() })
            root.addView(createCard("🚪 Logout", "Reset current session") { AppConfig.clearAuth(this@MainActivity); showHome() })
        }

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun createCard(titleText: String, subtitleText: String, onClick: () -> Unit): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 25, 30, 25)
            setBackgroundColor(0xFFF8F9FA.toInt())
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 20)
            layoutParams = params
            if (onClick != {}) setOnClickListener { onClick() }
        }

        val t = TextView(this).apply {
            text = titleText
            textSize = 16f
            setTextColor(0xFF1565C0.toInt())
        }
        val s = TextView(this).apply {
            text = subtitleText
            textSize = 13f
            setTextColor(0xFF616161.toInt())
            setPadding(0, 5, 0, 0)
        }
        card.addView(t)
        if (subtitleText.isNotEmpty()) card.addView(s)
        return card
    }

    private fun configure() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 40, 40, 40) }
        box.addView(TextView(this).apply { text = "Firebase Setup"; textSize = 22f; setTextColor(0xFF1976D2.toInt()) })
        val key = EditText(this).apply { hint = "Firebase Web API Key" }
        val project = EditText(this).apply { hint = "Firebase Project ID" }
        box.addView(key); box.addView(project)
        box.addView(Button(this).apply {
            text = "Save Configuration"
            setOnClickListener {
                AppConfig.saveFirebase(this@MainActivity, key.text.toString().trim(), project.text.toString().trim())
                showHome()
            }
        })
        setContentView(box)
    }

    private fun auth() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 40, 40, 40) }
        box.addView(TextView(this).apply { text = "Account Access"; textSize = 22f; setTextColor(0xFF1976D2.toInt()) })
        val e = EditText(this).apply { hint = "Email Address" }
        val p = EditText(this).apply { hint = "Password"; inputType = 129 }
        box.addView(e); box.addView(p)
        box.addView(Button(this).apply { text = "Sign In"; setOnClickListener { doAuth(e.text.toString(), p.text.toString(), false) } })
        box.addView(Button(this).apply { text = "Create Account"; setOnClickListener { doAuth(e.text.toString(), p.text.toString(), true) } })
        setContentView(box)
    }

    private fun doAuth(e: String, p: String, create: Boolean) {
        if (e.isBlank() || p.length < 6) { toast("Enter valid email & password (min 6 chars)"); return }
        exec.execute {
            try {
                val r = if (create) FirebaseApi.signUp(this@MainActivity, e, p) else FirebaseApi.signIn(this@MainActivity, e, p)
                runOnUiThread {
                    AppConfig.saveAuth(this@MainActivity, e, r.getString("localId"), r.getString("idToken"))
                    showHome()
                }
            } catch (x: Exception) {
                runOnUiThread { toast("Auth Error: ${x.message?.take(180)}") }
            }
        }
    }

    private fun profile() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 40, 40, 40) }
        box.addView(TextView(this).apply { text = "Family Group Setup"; textSize = 22f; setTextColor(0xFF1976D2.toInt()) })
        val n = EditText(this).apply { hint = "Your Name"; setText(AppConfig.name(this@MainActivity)) }
        val c = EditText(this).apply { hint = "Family Code (e.g. HOME2026)"; setText(AppConfig.familyCode(this@MainActivity)) }
        box.addView(n); box.addView(c)
        box.addView(Button(this).apply {
            text = "Save Profile"
            setOnClickListener {
                AppConfig.saveProfile(this@MainActivity, n.text.toString(), c.text.toString().trim().uppercase())
                showHome()
            }
        })
        setContentView(box)
    }

    private fun startSharing() {
        if (AppConfig.familyCode(this).isBlank() || AppConfig.name(this).isBlank()) {
            toast("Please set your Name and Family Code first")
            return
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 55)
            return
        }
        AppConfig.setSharing(this, true)
        startForegroundService(Intent(this, LocationService::class.java))
        showHome()
    }

    private fun sendEmergencySOS() {
        if (AppConfig.familyCode(this).isBlank()) { toast("Family Code required for SOS"); return }
        toast("Sending Emergency SOS Alert...")
        exec.execute {
            try {
                FirebaseApi.sendSOSAlert(
                    this@MainActivity,
                    AppConfig.idToken(this@MainActivity),
                    AppConfig.familyCode(this@MainActivity),
                    AppConfig.name(this@MainActivity)
                )
                runOnUiThread { toast("🚨 Emergency Alert Sent!") }
            } catch (e: Exception) {
                runOnUiThread { toast("Failed to send SOS: ${e.message}") }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 55 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSharing()
        } else if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toast("Camera Access Granted! 📷")
            } else {
                toast("Camera Access Denied!")
            }
            showHome()
        }
    }

    private fun members() {
        if (AppConfig.familyCode(this).isBlank()) { toast("Set Family Code first"); return }
        exec.execute {
            try {
                val list = FirebaseApi.listMembers(this@MainActivity, AppConfig.idToken(this@MainActivity), AppConfig.familyCode(this@MainActivity))
                runOnUiThread {
                    val box = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.VERTICAL; setPadding(30, 30, 30, 30) }
                    box.addView(TextView(this@MainActivity).apply { text = "📍 Live Dashboard"; textSize = 22f; setTextColor(0xFF1976D2.toInt()) })

                    if (list.isEmpty()) box.addView(TextView(this@MainActivity).apply { text = "No active family members found." })

                    list.forEach { o ->
                        val name = o.optString("name", "Member")
                        val lat = o.optDouble("lat", Double.NaN)
                        val lon = o.optDouble("lon", Double.NaN)
                        val battery = o.optInt("battery", -1)
                        val sharing = o.optBoolean("sharing", false)

                        val detailCard = LinearLayout(this@MainActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(25, 20, 25, 20)
                            setBackgroundColor(0xFFECEFF1.toInt())
                            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            params.setMargins(0, 10, 0, 10)
                            layoutParams = params
                        }

                        val memberInfo = "$name — ${if (sharing) "🟢 Sharing ON" else "🔴 Sharing OFF"}\n🔋 Battery: ${if (battery != -1) "$battery%" else "N/A"}"
                        detailCard.addView(TextView(this@MainActivity).apply { text = memberInfo; textSize = 15f; setTextColor(0xFF263238.toInt()) })

                        if (!lat.isNaN()) {
                            detailCard.addView(Button(this@MainActivity).apply {
                                text = "🗺️ Open Location in Maps"
                                setOnClickListener {
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lon?q=$lat,$lon($name)")))
                                }
                            })
                        }
                        box.addView(detailCard)
                    }

                    box.addView(Button(this@MainActivity).apply { text = "🔄 Refresh"; setOnClickListener { members() } })
                    box.addView(Button(this@MainActivity).apply { text = "⬅️ Back"; setOnClickListener { showHome() } })
                    setContentView(ScrollView(this@MainActivity).apply { addView(box) })
                }
            } catch (x: Exception) {
                runOnUiThread { toast("Dashboard Error: ${x.message?.take(180)}") }
            }
        }
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
}
