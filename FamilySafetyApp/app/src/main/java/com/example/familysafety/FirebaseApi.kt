package com.example.familysafety

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object FirebaseApi {
    private fun request(url: String, method: String, body: String?, token: String? = null): JSONObject {
        val c = URL(url).openConnection() as HttpURLConnection
        c.requestMethod = method
        c.connectTimeout = 12000; c.readTimeout = 12000
        c.setRequestProperty("Content-Type", "application/json")
        if (!token.isNullOrBlank()) c.setRequestProperty("Authorization", "Bearer $token")
        if (body != null) { c.doOutput = true; c.outputStream.use { it.write(body.toByteArray()) } }
        val stream = if (c.responseCode in 200..299) c.inputStream else c.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() } ?: "{}"
        if (c.responseCode !in 200..299) throw Exception(text)
        return JSONObject(text)
    }

    fun signIn(c: Context, email: String, password: String): JSONObject {
        val key = AppConfig.apiKey(c); require(key.isNotBlank()) { "Firebase API key missing" }
        return request("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$key", "POST", JSONObject().put("email", email).put("password", password).put("returnSecureToken", true).toString())
    }
    fun signUp(c: Context, email: String, password: String): JSONObject {
        val key = AppConfig.apiKey(c); require(key.isNotBlank()) { "Firebase API key missing" }
        return request("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$key", "POST", JSONObject().put("email", email).put("password", password).put("returnSecureToken", true).toString())
    }
    private fun docUrl(c: Context, path: String) = "https://firestore.googleapis.com/v1/projects/${AppConfig.projectId(c)}/databases/(default)/documents/$path"
    fun writeMember(c: Context, token: String, code: String, uid: String, fields: JSONObject) {
        val fs = JSONObject().put("fields", JSONObject())
        val out = JSONObject()
        fields.keys().forEach { k ->
            val v = fields.get(k)
            out.put(k, when (v) {
                is Boolean -> JSONObject().put("booleanValue", v)
                is Number -> JSONObject().put("doubleValue", v.toDouble())
                else -> JSONObject().put("stringValue", v.toString())
            })
        }
        fs.put("fields", out)
        request(docUrl(c, "families/$code/members/$uid"), "PATCH", fs.toString(), token)
    }
    fun listMembers(c: Context, token: String, code: String): List<JSONObject> {
        val r = request(docUrl(c, "families/$code/members"), "GET", null, token)
        val arr = r.optJSONArray("documents") ?: return emptyList()
        val list = mutableListOf<JSONObject>()
        for (i in 0 until arr.length()) {
            val d = arr.getJSONObject(i); val f = d.optJSONObject("fields") ?: JSONObject(); val o = JSONObject()
            f.keys().forEach { k -> val x=f.getJSONObject(k); o.put(k, x.opt("stringValue") ?: x.opt("doubleValue") ?: x.opt("booleanValue")) }
            o.put("id", d.optString("name").substringAfterLast('/')); list.add(o)
        }
        return list
    }
}
