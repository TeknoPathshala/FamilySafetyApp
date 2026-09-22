package com.example.familysafety

import android.content.Context
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object FirebaseApi {

    fun signUp(context: Context, email: String, pass: String): JSONObject {
        val key = AppConfig.apiKey(context)
        val url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$key"
        val body = JSONObject().apply {
            put("email", email)
            put("password", pass)
            put("returnSecureToken", true)
        }
        return postJson(url, body.toString())
    }

    fun signIn(context: Context, email: String, pass: String): JSONObject {
        val key = AppConfig.apiKey(context)
        val url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$key"
        val body = JSONObject().apply {
            put("email", email)
            put("password", pass)
            put("returnSecureToken", true)
        }
        return postJson(url, body.toString())
    }

    fun sendSOSAlert(context: Context, idToken: String, familyCode: String, userName: String) {
        val projectId = AppConfig.projectId(context)
        val url = "https://$projectId-default-rtdb.firebaseio.com/sos/$familyCode.json?auth=$idToken"
        val body = JSONObject().apply {
            put("sender", userName)
            put("timestamp", System.currentTimeMillis())
            put("status", "EMERGENCY")
        }
        postJson(url, body.toString())
    }

    fun listMembers(context: Context, idToken: String, familyCode: String): List<JSONObject> {
        val projectId = AppConfig.projectId(context)
        val urlStr = "https://$projectId-default-rtdb.firebaseio.com/locations/$familyCode.json?auth=$idToken"
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 10000
        conn.readTimeout = 10000

        val list = mutableListOf<JSONObject>()
        if (conn.responseCode == 200) {
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            if (text.isNotBlank() && text != "null") {
                val json = JSONObject(text)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    list.add(json.getJSONObject(key))
                }
            }
        }
        return list
    }

    private fun postJson(urlStr: String, jsonBody: String): JSONObject {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestHeader("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 10000

        OutputStreamWriter(conn.outputStream).use { it.write(jsonBody) }

        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        val responseText = stream.bufferedReader().use { it.readText() }
        
        if (conn.responseCode !in 200..299) {
            throw Exception("HTTP ${conn.responseCode}: $responseText")
        }
        return JSONObject(responseText)
    }
}
