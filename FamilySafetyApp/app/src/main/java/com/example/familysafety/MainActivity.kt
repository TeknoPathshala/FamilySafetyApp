package com.example.familysafety

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private val exec=Executors.newSingleThreadExecutor()
    private fun tv(t:String,size:Float=16f)=TextView(this).apply{ text=t;textSize=size;setPadding(0,12,0,12) }
    private fun btn(t:String, click:()->Unit)=Button(this).apply{text=t;setOnClickListener{click()}}
    override fun onCreate(b:Bundle?){super.onCreate(b);showHome()}
    private fun showHome(){
        root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(32,28,32,20)}
        root.addView(tv("Family Safety",28f)); root.addView(tv("Transparent family location sharing\nCamera and microphone are not accessed by this app."))
        if(AppConfig.apiKey(this).isBlank()) root.addView(btn("1. Configure Firebase"){configure()})
        if(AppConfig.uid(this).isBlank()) root.addView(btn("2. Sign in / Create account"){auth()})
        if(AppConfig.uid(this).isNotBlank()){
            root.addView(tv("Account: ${AppConfig.email(this)}"))
            root.addView(btn("3. Set family code / name"){profile()})
            root.addView(tv("Family: ${AppConfig.familyCode(this).ifBlank{"Not set"}}"))
            root.addView(btn(if(AppConfig.sharing(this)) "Stop location sharing" else "Start location sharing"){
                if(AppConfig.sharing(this)) { stopService(Intent(this,LocationService::class.java)); AppConfig.setSharing(this,false); showHome() }
                else startSharing()
            })
            root.addView(btn("View family locations"){members()})
            root.addView(btn("Log out / reset"){AppConfig.clearAuth(this);showHome()})
        }
        root.addView(tv("\nSharing is always visible. Android shows a persistent notification while location sharing is active. Each device can stop sharing at any time."))
        setContentView(ScrollView(this).apply{addView(root)})
    }
    private fun configure(){
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(32,32,32,32)}; box.addView(tv("Firebase setup",26f)); val key=EditText(this);key.hint="Firebase Web API key";val project=EditText(this);project.hint="Firebase project ID";box.addView(key);box.addView(project);box.addView(btn("Save"){AppConfig.saveFirebase(this,key.text.toString().trim(),project.text.toString().trim());showHome()});setContentView(box)
    }
    private fun auth(){
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(32,32,32,32)};box.addView(tv("Account",26f));val e=EditText(this);e.hint="Email";val p=EditText(this);p.hint="Password";p.inputType=129;box.addView(e);box.addView(p);box.addView(btn("Sign in"){doAuth(e.text.toString(),p.text.toString(),false)});box.addView(btn("Create account"){doAuth(e.text.toString(),p.text.toString(),true)});setContentView(box)
    }
    private fun doAuth(e:String,p:String,create:Boolean){ if(e.isBlank()||p.length<6){toast("Enter email and password (6+ characters)");return}; exec.execute{try{val r=if(create)FirebaseApi.signUp(this,e,p)else FirebaseApi.signIn(this,e,p);runOnUiThread{AppConfig.saveAuth(this,e,r.getString("localId"),r.getString("idToken"));showHome()}}catch(x:Exception){runOnUiThread{toast("Firebase error: ${x.message?.take(180)}")}}} }
    private fun profile(){
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(32,32,32,32)};box.addView(tv("Family setup",26f));val n=EditText(this);n.hint="Your name";n.setText(AppConfig.name(this));val c=EditText(this);c.hint="Family code (e.g. ALI2026)";c.setText(AppConfig.familyCode(this));box.addView(n);box.addView(c);box.addView(tv("Use the same family code on each family member's phone. It is a shared code, not a secret password."));box.addView(btn("Save"){AppConfig.saveProfile(this,n.text.toString(),c.text.toString().trim().uppercase());showHome()});setContentView(box)
    }
    private fun startSharing(){
        if(AppConfig.familyCode(this).isBlank()||AppConfig.name(this).isBlank()){toast("Set your name and family code first");return}
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),55);return}
        AppConfig.setSharing(this,true);startForegroundService(Intent(this,LocationService::class.java));showHome()
    }
    override fun onRequestPermissionsResult(r:Int,p:Array<out String>,g:IntArray){super.onRequestPermissionsResult(r,p,g);if(r==55&&g.isNotEmpty()&&g[0]==PackageManager.PERMISSION_GRANTED)startSharing()else toast("Location permission is required for sharing")}
    private fun members(){
        if(AppConfig.familyCode(this).isBlank()){toast("Set family code first");return};exec.execute{try{val list=FirebaseApi.listMembers(this,AppConfig.idToken(this),AppConfig.familyCode(this));runOnUiThread{val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;padding=28};box.addView(tv("Family locations",26f));if(list.isEmpty())box.addView(tv("No family members have shared a location yet."));list.forEach{o->val name=o.optString("name","Member");val lat=o.optDouble("lat",Double.NaN);val lon=o.optDouble("lon",Double.NaN);val sharing=o.optBoolean("sharing",false);box.addView(tv("$name — ${if(sharing)"Sharing ON" else "Sharing OFF"}\n${if(lat.isNaN())"No location" else "$lat, $lon"}"));if(!lat.isNaN())box.addView(btn("Open in Google Maps"){startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lon?q=$lat,$lon")))})};box.addView(btn("Refresh"){members()});box.addView(btn("Back"){showHome()});setContentView(ScrollView(this).apply{addView(box)})}}catch(x:Exception){runOnUiThread{toast("Could not load members: ${x.message?.take(180)}")}}}
    }
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
